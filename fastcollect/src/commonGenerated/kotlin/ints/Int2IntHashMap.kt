@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.ArrayUtils
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.ints.MutableIntCollection
import io.github.sooniln.fastcollect.ints.MutableIntIterator
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Int to Int
 * relationships.
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
    capacity: Int = 0,
    /** The default value should be the value that is ideally least likely to occur in the map. */
    private val defaultValue: Int = Int.MIN_VALUE,
) : AbstractMutableInt2IntMap() {

    public constructor(map: Int2IntMap): this() { putAll(map) }

    public constructor(map: Map<Int, Int>): this() { putAll(map) }

    private var kvArr = EMPTY_ARRAY

    private var emptyEntry = ZERO_ENTRY

    // threshold + size == capacity (rehash once threshold <= 0, if we haven't allocated yet then threshold.inv() is
    // our initial capacity)
    private var threshold = MIN_INITIAL_CAPACITY.inv()

    override var size: Int = 0
        private set

    init {
        ensureCapacity(capacity)
    }

    override fun isDefaultValue(value: Int): Boolean = value == defaultValue

    /**
     * Ensures that the map can hold at least given number of key/value pairs without any further resizing of the
     * backing array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "Capacity must be >= 0" }
        if (kvArr === EMPTY_ARRAY) {
            threshold = min(threshold, capacity.inv())
        } else if (capacity > threshold + size) {
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
        for (slot in kvArr.indices) {
            val entry = kvArr[slot]
            if (entry != emptyEntry && entry.value() == value) return true
        }
        return false
    }

    override fun get(key: Int): Int = findSlot(key, { _, entry -> entry.value() }, { defaultValue })

    public fun getOrDefault(key: Int, default: Int): Int = findSlot(key, { _, entry -> entry.value() }, { default })

    override fun put(key: Int, value: Int): Int {
        var returnValue = ZERO
        set(key, {
            returnValue = defaultValue
            value
        }, { entry ->
            returnValue = entry.value()
            value
        })
        return returnValue
    }

    public fun putIfAbsent(key: Int, value: Int): Int {
        set(key, { value }, { entry -> return entry.value() })
        return defaultValue
    }

    override fun set(key: Int, value: Int) {
        set(key, { value }, { value })
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
        if (kvArr !== EMPTY_ARRAY) {
            threshold += size
            kvArr.fill(emptyEntry)
        }
        size = 0
    }

    private inline fun <T> findSlot(key: Int, onFind: (slot: Int, entry: Long) -> T, onFail: () -> T): T {
        val kvArr = kvArr
        val mask = kvArr.size - 1

        var slot = key.slot(mask)
        var currEntry = kvArr[slot]

        var slotDistance = 0
        while (true) {
            // checking whether the current slot distance is higher than our search distance allows us to early exit the
            // search loop - but the cost of checking is non-trivial. as a compromise between GetHit and GetMiss
            // performance we only check once every half cache line.
            var i = 0
            do {
                if (currEntry == emptyEntry) {
                    return onFail()
                } else if (currEntry.key() == key) {
                    return onFind(slot, currEntry)
                }

                slot = (slot + 1) and mask
                currEntry = kvArr[slot]
            } while (++i < HALF_CACHE_LINE_SIZE)

            slotDistance += HALF_CACHE_LINE_SIZE
            if (currEntry.key().slotDistance(slot, mask) < slotDistance) {
                return onFail()
            }
        }
    }

    private inline fun set(key: Int, onAdd: () -> Int, onReplace: (entry: Long) -> Int) {
        if (threshold <= 0) increaseCapacity()
        if (key == emptyEntry.key()) changeEmptyEntry()

        val kvArr = kvArr
        val mask = kvArr.size - 1

        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            var currEntry = kvArr[slot]
            if (currEntry.key() == key) {
                kvArr[slot] = arrayEntry(key, onReplace(currEntry))
                return
            } else if (currEntry == emptyEntry || distance > currEntry.key().slotDistance(slot, mask)) {
                var newEntry = arrayEntry(key, onAdd())

                while (currEntry != emptyEntry) {
                    kvArr[slot] = newEntry
                    newEntry = currEntry
                    slot = (slot + 1) and mask
                    currEntry = kvArr[slot]
                }

                kvArr[slot] = newEntry
                --threshold
                ++size
                return
            }

            slot = (slot + 1) and mask
            ++distance
        }
    }

    private fun removeSlot(slot: Int) {
        val kvArr = kvArr
        val mask = kvArr.size - 1

        var currSlot = slot
        var nextSlot = (currSlot + 1) and mask
        var nextEntry = kvArr[nextSlot]
        while (nextEntry != emptyEntry && nextEntry.key().slotDistance(nextSlot, mask) > 0) {
            kvArr[currSlot] = nextEntry

            currSlot = nextSlot
            nextSlot = (nextSlot + 1) and mask
            nextEntry = kvArr[nextSlot]
        }
        kvArr[currSlot] = emptyEntry
        ++threshold
        --size
    }

    override fun putAll(from: Int2IntMap) {
        if (from is Int2IntHashMap && from.size / 2 > size) {
            val old = iterator()
            resetTo(from)
            for ((key, value) in old) {
                putIfAbsent(key, value)
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

        kvArr = from.kvArr.copyOf()
        emptyEntry = from.emptyEntry
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

    private fun increaseCapacity() {
        check(threshold <= 0)
        if (threshold < 0) {
            rehash(threshold.inv())
        } else {
            rehash(size shl 1)
        }
    }

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    private fun rehash(capacity: Int) {
        check(capacity >= size)

        if (capacity == 0) {
            if (kvArr !== EMPTY_ARRAY) {
                kvArr = EMPTY_ARRAY
                emptyEntry = ZERO_ENTRY
                threshold = MIN_INITIAL_CAPACITY.inv()
            }
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1.0 else 0.9

        val newLength = arraySize(capacity, actualLoadFactor)
        if (kvArr.size == newLength) return

        val newKvArr = LongArray(newLength)
        if (emptyEntry != ZERO_ENTRY) newKvArr.fill(emptyEntry)
        val newMask = newKvArr.size - 1

        val oldKvArr = kvArr
        for (slot in oldKvArr.indices) {
            val entry = oldKvArr[slot]
            if (entry != emptyEntry) setRehashing(newKvArr, newMask, entry)
        }

        kvArr = newKvArr

        // threshold must always maintain the invariant of at least 1 slot being open
        val newCapacity = newLength / 2
        threshold = min((newCapacity * actualLoadFactor).toInt(), newCapacity - 1) - size
    }

    // we can assume key doesn't exist in array and that we never insert emptyEntry
    private fun setRehashing(kvArr: LongArray, mask: Int, entry: Long) {
        var slot = entry.key().slot(mask)
        var distance = 0
        while (true) {
            var currEntry = kvArr[slot]
            if (currEntry == emptyEntry) {
                kvArr[slot] = entry
                return
            } else if (distance > currEntry.key().slotDistance(slot, mask)) {
                var newEntry = entry
                do {
                    kvArr[slot] = newEntry
                    newEntry = currEntry
                    slot = (slot + 1) and mask
                    currEntry = kvArr[slot]
                } while (currEntry != emptyEntry)

                kvArr[slot] = newEntry
                return
            }

            slot = (slot + 1) and mask
            ++distance
        }
    }

    // changes emptyEntry to a value not currently in the map, rewriting all empty slots
    private fun changeEmptyEntry() {
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        var candidate = ZERO
        while (candidate == emptyEntry.key() || containsKey(candidate)) {
            candidate = Random.nextInt()
        }

        val oldEmptyEntry = emptyEntry
        emptyEntry = arrayEntry(candidate, 0)

        val kvArr = kvArr
        for (i in kvArr.indices) {
            if (kvArr[i] == oldEmptyEntry) kvArr[i] = emptyEntry
        }
    }

    override operator fun iterator(): MutableFastIterator<MutableInt2IntMap.MutableEntry> = FastEntryIterator()

    public fun forEach(action: (Int, Int) -> Unit) {
        val kvArr = kvArr

        var slot = kvArr.size - 1
        while (slot >= 0) {
            val entry = kvArr[slot]
            if(entry != emptyEntry) {
                action(entry.key(), entry.value())
            }
            --slot
        }
    }

    private open inner class SlotIterator {
        private val kvArr = this@Int2IntHashMap.kvArr
        private var emptyEntry = this@Int2IntHashMap.emptyEntry
        private val mask = kvArr.size - 1

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

            // if removal wrapped all the way around to our next slot then we need to adjust
            if (kvArr[slot] == emptyEntry) {
                slot = (slot - 1) and mask
            }
        }

        private fun decrement() {
            do {
                slot = (slot - 1) and mask
            } while (kvArr[slot] == emptyEntry)
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

    @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
    private fun Int.mix(mask: Int): Int {
        if ((this.toInt() or mask) == mask) {
            return this.toInt()
        } else {
            var h = hashCode()
            h = h xor (h ushr 16)
            h *= PHI
            h = h xor (h ushr 16)
            return h
        }
    }

    private fun Int.slot(mask: Int): Int = mix(mask) and mask
    private fun Int.slotDistance(slot: Int, mask: Int): Int = (slot - mix(mask)) and mask

    private fun Long.key(): Int = toInt()
    private fun Long.value(): Int = (this shr (8 * Int.SIZE_BYTES)).toInt()
    private fun arrayEntry(key: Int, value: Int): Long = (value.toLong() shl (8 * Int.SIZE_BYTES)) or (key.toUInt().toLong())

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO = 0.toInt()
        private const val ZERO_ENTRY = 0.toLong()

        private val EMPTY_ARRAY = longArrayOf(ZERO_ENTRY)

        private const val PHI = 0x9E3779B9.toInt()

        private const val MIN_INITIAL_CAPACITY = 7 // may not be zero

        private const val CACHE_LINE_SIZE = 64 / Long.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2

        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX = 2 * CACHE_LINE_SIZE

        private fun arraySize(capacity: Int, loadFactor: Double): Int {
            check(capacity >= 0)
            // array must always maintain the invariant of at least one slot remaining open
            val requiredArraySize = 2 * max((capacity / loadFactor).toInt(), capacity + 1)
            return ArrayUtils.minPowerOfTwo(requiredArraySize)
        }
    }
}
