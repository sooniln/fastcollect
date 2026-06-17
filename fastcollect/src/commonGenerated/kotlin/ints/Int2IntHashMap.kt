@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.ArrayUtils
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.ints.MutableIntCollection
import io.github.sooniln.fastcollect.ints.MutableIntIterator
import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Int to Int
 * relationships using Robin Hood hashing with two hash functions and a parallel PSL [ByteArray].
 *
 * Each PSL byte encodes both the probe sequence length and which hash function homed the element:
 * bit 7 = 0 means hash1 (identity); bit 7 = 1 means hash2 (Fibonacci mix). Bits 0–6 store PSL+1
 * (1–127 for PSL 0–126), with 0 meaning empty.
 *
 * Lookup uses two phases: phase 1 scans one cache line from hash1(K) (brute force, no early
 * termination needed); phase 2 probes from hash2(K) with Robin Hood early termination using all PSL
 * bytes, since the global Robin Hood ordering is maintained across both hash functions.
 *
 * The [keys]/[values] mutable collections exposed by this class will throw [UnsupportedOperationException] on any
 * attempt to mutate the collection, EXCEPT that [MutableIterator.remove] will work as expected. Other mutation
 * operations should be made directly on the map rather than on the sub-collections.
 *
 * The entry [iterator] exposed by this class is a [FastIterator] - clients may not allow the returned entry to escape.
 * See the [FastIterator] documentation for more information.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 */
public class Int2IntHashMap @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    /** The default value should be the value that is ideally least likely to occur in the map. */
    private val defaultValue: Int = Int.MIN_VALUE,
) : AbstractMutableInt2IntMap() {

    init {
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(map: Int2IntMap): this() { putAll(map) }

    public constructor(map: Map<Int, Int>): this() { putAll(map) }

    private var metadataArr = EMPTY_METADATA_ARRAY
    private var kvArr = EMPTY_KV_ARRAY

    // use threshold to store the initial size before we allocate anything, after that it's the size at which we rehash
    private var threshold: Int = if (capacity == 0) DEFAULT_INITIAL_CAPACITY else capacity

    override var size: Int = 0
        private set

    override fun isDefaultValue(value: Int): Boolean = value == defaultValue

    /**
     * Ensures that the map can hold at least given number of key/value pairs without any further resizing of the
     * backing array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
        if (metadataArr === EMPTY_METADATA_ARRAY) {
            threshold = max(threshold, capacity)
        } else if (capacity > threshold) {
            rehash(capacity)
        }
    }

    /**
     * Reduces the size of the backing array to the minimum required to hold the current number of elements.
     */
    public fun trimToSize() {
        rehash(size)
    }

    override fun containsKey(key: Int): Boolean = findSlot(key, { _, _ -> true }, { false })

    override fun containsValue(value: Int): Boolean {
        val kvArr = kvArr
        val metadataArr = metadataArr
        for (slot in kvArr.indices) {
            if (kvArr[slot].value() == value && metadataArr[slot] != 0.toByte()) return true
        }
        return false
    }

    override fun get(key: Int): Int = findSlot(key, { _, entry -> entry.value() }, { defaultValue })

    override fun put(key: Int, value: Int): Int {
        resizeIfNecessary()

        val metadataArr = metadataArr
        val kvArr = kvArr
        val mask = metadataArr.size - 1

        var newEntry = arrayEntry(key, value)
        var newMetadata = 1  // hash1 element, PSL=0
        var slot = key.slot1(mask)

        // hash 1 insertion
        for (i in 0 until HALF_CACHE_LINE_SIZE) {
            val currMetadata = metadataArr[slot].toInt() and 0xFF
            if (currMetadata == 0) {
                metadataArr[slot] = newMetadata.toByte()
                kvArr[slot] = newEntry
                ++size
                return defaultValue
            } else if (kvArr[slot].key() == newEntry.key()) {
                val oldValue = kvArr[slot].value()
                kvArr[slot] = newEntry
                return oldValue
            }

            if ((newMetadata and PSL_MASK) > (currMetadata and PSL_MASK)) {
                metadataArr[slot] = newMetadata.toByte()
                newMetadata = currMetadata
                val currEntry = kvArr[slot]
                kvArr[slot] = newEntry
                newEntry = currEntry
            }

            slot = (slot + 1) and mask
            ++newMetadata
        }

        // switch to hash 2 if necessary
        if ((newMetadata and HASH2_BIT) == 0) {
            slot = newEntry.key().slot2(mask)
            newMetadata = HASH2_BIT or 1
        }

        // hash 2 insertion
        while (true) {
            val currMetadata = metadataArr[slot].toInt() and 0xFF
            if (currMetadata == 0) {
                metadataArr[slot] = newMetadata.toByte()
                kvArr[slot] = newEntry
                ++size
                return defaultValue
            } else if (kvArr[slot].key() == newEntry.key()) {
                val oldValue = kvArr[slot].value()
                kvArr[slot] = newEntry
                return oldValue
            }

            if ((newMetadata and PSL_MASK) > (currMetadata and PSL_MASK)) {
                metadataArr[slot] = newMetadata.toByte()
                newMetadata = currMetadata
                val currEntry = kvArr[slot]
                kvArr[slot] = newEntry
                newEntry = currEntry
            }

            slot = (slot + 1) and mask
            ++newMetadata
        }
    }

    override fun remove(key: Int): Int {
        return findSlot(
            key,
            { slot, entry ->
                val oldValue = entry.value()
                removeSlot(slot)
                oldValue
            },
            { defaultValue })
    }

    override fun clear() {
        if (metadataArr !== EMPTY_METADATA_ARRAY) {
            metadataArr.fill(0)
        }
        size = 0
    }

    private inline fun <T> findSlot(key: Int, onFind: (slot: Int, entry: Long) -> T, onFail: () -> T): T {
        val metadataArr = metadataArr
        val kvArr = kvArr
        val mask = metadataArr.size - 1

        // Phase 1: cache-line scan from hash1(K).
        // metadata == distance: PSL+1 matches expected; implies metadata > 0 (hash1), since hash2 bytes
        // are negative as signed Int and distance is always positive.
        // An empty slot breaks early; cannot return onFail() — K may still be a hash2 element.
        var slot = key.slot1(mask)
        for (distance in 1..HALF_CACHE_LINE_SIZE) {
            if (metadataArr[slot].toInt() == distance && kvArr[slot].key() == key) {
                return onFind(slot, kvArr[slot])
            }

            slot = (slot + 1) and mask
        }

        // Phase 2: linear probe from hash2(K) with Robin Hood early termination.
        // (metadata and 0x7F) extracts PSL+1 uniformly with a single mask for both hash types.
        // metadata < 0 filters key comparisons to hash2 elements only.
        // Empty always triggers onFail() — Robin Hood ordering guarantees K absent past empty in phase 2.
        slot = key.slot2(mask)
        var currDistance = 1
        while (true) {
            val metadata = metadataArr[slot].toInt()
            val slotDistance = metadata and PSL_MASK
            if (slotDistance < currDistance) {
                return onFail()
            } else if (slotDistance == currDistance && metadata < 0 && kvArr[slot].key() == key) {
                return onFind(slot, kvArr[slot])
            }

            slot = (slot + 1) and mask
            ++currDistance
        }
    }

    private fun removeSlot(slot: Int) {
        val metadataArr = metadataArr
        val kvArr = kvArr
        val mask = metadataArr.size - 1

        var curr = slot
        var next = (curr + 1) and mask
        var nextMetadata = metadataArr[next].toInt() and 0xFF
        while ((nextMetadata and PSL_MASK) > 1) {
            metadataArr[curr] = (nextMetadata - 1).toByte()
            kvArr[curr] = kvArr[next]

            curr = next
            next = (next + 1) and mask
            nextMetadata = metadataArr[next].toInt() and 0xFF
        }

        metadataArr[curr] = 0
        --size
    }

    override fun putAll(from: Int2IntMap) {
        if (from is Int2IntHashMap && from.size / 2 > size) {
            val old = iterator()
            resetTo(from)
            for ((key, value) in old) {
                if (!containsKey(key)) set(key, value)
            }
        } else {
            ensureCapacity(max(size + (from.size / 2), from.size))
            for ((key, value) in from) {
                set(key, value)
            }
        }
    }

    override fun putAll(from: Map<out Int, Int>) {
        ensureCapacity(max(size + (from.size / 2), from.size))
        for ((key, value) in from) {
            set(key, value)
        }
    }

    private fun resetTo(from: Int2IntHashMap) {
        check(!from.isEmpty())

        metadataArr = from.metadataArr.copyOf()
        kvArr = from.kvArr.copyOf()
        size = from.size
        threshold = from.threshold
    }

    private var _keys: MutableIntSet? = null
    override val keys: MutableIntSet get() {
        return _keys ?:
            object : MutableIntSet {
                override val size: Int get() = this@Int2IntHashMap.size
                override fun contains(element: Int): Boolean = containsKey(element)
                override fun add(element: Int): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Int): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableIntIterator = KeyIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _keys = it }
    }

    private var _values: MutableIntCollection? = null
    override val values: MutableIntCollection get() {
        return _values ?:
            object : MutableIntCollection {
                override val size: Int get() = this@Int2IntHashMap.size
                override fun contains(element: Int): Boolean = containsValue(element)
                override fun add(element: Int): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Int): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableIntIterator = ValueIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _values = it }
    }

    private fun resizeIfNecessary() {
        if (metadataArr === EMPTY_METADATA_ARRAY) {
            rehash(threshold)
        } else if (size >= threshold) {
            rehash(threshold shl 1)
        }
    }

    private fun rehash(capacity: Int) {
        check(capacity >= size)

        if (capacity == 0) {
            if (metadataArr !== EMPTY_METADATA_ARRAY) {
                metadataArr = EMPTY_METADATA_ARRAY
                kvArr = EMPTY_KV_ARRAY
                threshold = DEFAULT_INITIAL_CAPACITY
            }
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else .9f

        val newLength = arraySize(capacity, actualLoadFactor)
        if (kvArr.size == newLength) return

        val newMetadataArr = ByteArray(newLength)
        val newKvArr = LongArray(newLength)
        val newMask = newLength - 1

        val oldMetadataArr = metadataArr
        val oldKvArr = kvArr
        for (slot in oldMetadataArr.indices) {
            if (oldMetadataArr[slot] != 0.toByte()) putRehashing(newMetadataArr, newKvArr, newMask, oldKvArr[slot])
        }

        kvArr = newKvArr
        metadataArr = newMetadataArr

        // threshold must always maintain the invariant of at least 1 slot being open
        val newCapacity = newLength / 2
        threshold = min((newCapacity * actualLoadFactor).toInt(), newCapacity - 1)
    }

    // assumes key doesn't exist in array
    private fun putRehashing(metadataArr: ByteArray, kvArr: LongArray, mask: Int, entry: Long) {
        var newEntry = entry
        var newMetadata = 1
        var slot = newEntry.key().slot1(mask)

        for (i in 0 until HALF_CACHE_LINE_SIZE) {
            val currMetadata = metadataArr[slot].toInt() and 0xFF
            if (currMetadata == 0) {
                metadataArr[slot] = newMetadata.toByte()
                kvArr[slot] = newEntry
                return
            }

            if ((newMetadata and PSL_MASK) > (currMetadata and PSL_MASK)) {
                metadataArr[slot] = newMetadata.toByte()
                newMetadata = currMetadata
                val currEntry = kvArr[slot]
                kvArr[slot] = newEntry
                newEntry = currEntry
            }

            slot = (slot + 1) and mask
            ++newMetadata
        }

        if ((newMetadata and HASH2_BIT) == 0) {
            slot = newEntry.key().slot2(mask)
            newMetadata = HASH2_BIT or 1
        }

        while (true) {
            val currMetadata = metadataArr[slot].toInt() and 0xFF
            if (currMetadata == 0) {
                kvArr[slot] = newEntry
                metadataArr[slot] = newMetadata.toByte()
                return
            }

            if ((newMetadata and PSL_MASK) > (currMetadata and PSL_MASK)) {
                metadataArr[slot] = newMetadata.toByte()
                newMetadata = currMetadata
                val currEntry = kvArr[slot]
                kvArr[slot] = newEntry
                newEntry = currEntry
            }

            slot = (slot + 1) and mask
            ++newMetadata
        }
    }

    override operator fun iterator(): MutableFastIterator<MutableInt2IntMap.MutableEntry> = FastEntryIterator()

    public fun forEach(action: (Int, Int) -> Unit) {
        val metadataArr = metadataArr
        val kvArr = kvArr

        var slot = metadataArr.size - 1
        while (slot >= 0) {
            if (metadataArr[slot] != 0.toByte()) {
                val entry = kvArr[slot]
                action(entry.key(), entry.value())
            }
            --slot
        }
    }

    private open inner class SlotIterator {
        private val metadataArr = this@Int2IntHashMap.metadataArr
        private val kvArr = this@Int2IntHashMap.kvArr
        private val mask = metadataArr.size - 1

        private var slotsLeft = size
        private var slot = kvArr.size
        private var previousSlot = -1

        init {
            if (slotsLeft > 0) decrement()
        }

        fun hasNext(): Boolean {
            return slotsLeft > 0
        }

        fun nextSlot() {
            if (slotsLeft <= 0) throw NoSuchElementException()
            previousSlot = slot
            if (--slotsLeft > 0) decrement()
        }

        fun key(): Int = kvArr[previousSlot].key()
        fun value(): Int = kvArr[previousSlot].value()

        fun updateValue(newValue: Int) {
            check(previousSlot != -1)
            if (kvArr !== this@Int2IntHashMap.kvArr) throw ConcurrentModificationException()
            kvArr[previousSlot] = arrayEntry(key(), newValue)
        }

        fun remove() {
            check(previousSlot != -1)
            if (kvArr !== this@Int2IntHashMap.kvArr) throw ConcurrentModificationException()

            removeSlot(previousSlot)
            previousSlot = -1

            // if backward shift moved the entry at our next slot one position left, back up to follow it
            if (metadataArr[slot] == 0.toByte()) {
                slot = (slot - 1) and mask
            }
        }

        private fun decrement() {
            do {
                slot = (slot - 1) and mask
            } while (metadataArr[slot] == 0.toByte())
        }
    }

    private inner class KeyIterator : MutableIntIterator() {
        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()

        override fun nextInt(): Int {
            it.nextSlot()
            return it.key()
        }

        override fun remove() = it.remove()
    }

    private inner class ValueIterator : MutableIntIterator() {
        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()

        override fun nextInt(): Int {
            it.nextSlot()
            return it.value()
        }

        override fun remove() = it.remove()
    }

    private inner class FastEntryIterator: SlotIterator(), MutableFastIterator<MutableInt2IntMap.MutableEntry>, MutableInt2IntMap.MutableEntry {

        override val key: Int
            get() = key()
        override var value: Int
            get() = value()
            set(value) {
                updateValue(value)
            }

        override fun next(): MutableInt2IntMap.MutableEntry {
            nextSlot()
            return this
        }

        override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
        override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        override fun toString(): String = "$key=$value"
    }

    private fun Int.slot1(mask: Int): Int = hashCode() and mask

    private fun Int.slot2(mask: Int): Int {
        var h = hashCode()
        h = h xor (h ushr 16)
        h = h * PHI
        h = h xor (h ushr 16)
        return h and mask
    }

    private fun Long.key(): Int = toInt()
    private fun Long.value(): Int = (this shr (8 * Int.SIZE_BYTES)).toInt()
    private fun arrayEntry(key: Int, value: Int): Long = (value.toLong() shl (8 * Int.SIZE_BYTES)) or (key.toLong() and 0xFFFFFFFFL)

    internal companion object {
        private val EMPTY_METADATA_ARRAY = ByteArray(1)
        private val EMPTY_KV_ARRAY = LongArray(1)

        private const val PHI: Int = 0x9E3779B9.toInt()

        private const val HASH2_BIT = 0x80
        private const val PSL_MASK  = 0x7F

        internal const val DEFAULT_INITIAL_CAPACITY = 7

        private const val CACHE_LINE_SIZE = 64 / Long.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2

        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * CACHE_LINE_SIZE

        private fun arraySize(capacity: Int, loadFactor: Float): Int {
            check(capacity >= 0)
            // array must always maintain the invariant of at least one slot remaining open
            val requiredArraySize = 2 * max((capacity / loadFactor).toInt(), capacity + 1)
            return ArrayUtils.minPowerOfTwo(requiredArraySize)
        }
    }
}
