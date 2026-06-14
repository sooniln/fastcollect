@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.ArrayUtils
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.longs.MutableLongCollection
import io.github.sooniln.fastcollect.longs.MutableLongIterator
import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Long to Long
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
public class Long2LongHashMap @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    /** The default value should be the value that is ideally least likely to occur in the map. */
    private val defaultValue: Long = Long.MIN_VALUE,
) : AbstractMutableLong2LongMap() {

    init {
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(map: Long2LongMap): this() { putAll(map) }

    public constructor(map: Map<Long, Long>): this() { putAll(map) }

    private var kvArr = EMPTY_ARRAY

    private var emptyKey = ZERO

    // use threshold to store the initial size before we allocate anything, after that it's the size at which we rehash
    private var threshold: Int = if (capacity == 0) DEFAULT_INITIAL_CAPACITY else capacity

    override var size: Int = 0
        private set

    override fun isDefaultValue(value: Long): Boolean = value == defaultValue

    /**
     * Ensures that the map can hold at least given number of key/value pairs without any further resizing of the
     * backing array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
        if (kvArr === EMPTY_ARRAY) {
            threshold = capacity
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

    override fun containsKey(key: Long): Boolean = findSlot(key, { true }, { false })

    override fun containsValue(value: Long): Boolean {
        val kvArr = kvArr
        val emptyKey = emptyKey
        for (slot in 1..kvArr.size-1 step 2) {
            if (kvArr[slot] == value && kvArr[slot - 1] != emptyKey) return true
        }
        return false
    }

    override fun get(key: Long): Long = findSlot(key, { slot -> kvArr[slot + 1] }, { defaultValue })

    override fun put(key: Long, value: Long): Long {
        resizeIfNecessary()

        if (key == emptyKey) changeEmptyKey()

        val kvArr = kvArr
        val emptyKey = emptyKey
        val mask = kvArr.size - 1

        var newKey = key
        var newValue = value

        var slot = key.slot(mask)
        var newSlotDistance = 0
        while (true) {
            val currKey = kvArr[slot]
            if (currKey == emptyKey) {
                kvArr[slot] = newKey
                kvArr[slot + 1] = newValue
                ++size
                return defaultValue
            } else if(currKey == newKey) {
                val oldValue = kvArr[slot + 1]
                kvArr[slot + 1] = newValue
                return oldValue
            }

            val currSlotDistance = currKey.slotDistance(slot, mask)
            if (newSlotDistance > currSlotDistance) {
                val currValue = kvArr[slot + 1]
                kvArr[slot] = newKey
                kvArr[slot + 1] = newValue
                newKey = currKey
                newValue = currValue
                newSlotDistance = currSlotDistance
            }

            slot = (slot + 2) and mask
            newSlotDistance += 2
        }
    }

    override fun remove(key: Long): Long {
        return findSlot(
            key,
            { slot ->
                val oldValue = kvArr[slot + 1]
                removeSlot(slot)
                oldValue
            },
            { defaultValue })
    }

    override fun clear() {
        if (kvArr !== EMPTY_ARRAY) {
            kvArr.fill(emptyKey)
        }
        size = 0
    }

    private inline fun <T> findSlot(key: Long, onFind: (slot: Int) -> T, onFail: () -> T): T {
        val kvArr = kvArr
        val emptyKey = emptyKey
        val mask = kvArr.size - 1

        var slot = key.slot(mask)
        var currKey = kvArr[slot]

        var slotDistance = 0
        while (true) {
            // checking whether the current slot distance is higher than our search distance allows us to early exit the
            // search loop - but the cost of checking is non-trivial. as a compromise between GetHit and GetMiss
            // performance we only check once every half cache line.
            var i = 0
            do {
                if (currKey == emptyKey) {
                    return onFail()
                } else if (currKey == key) {
                    return onFind(slot)
                }

                slot = (slot + 2) and mask
                currKey = kvArr[slot]
            } while (++i < HALF_CACHE_LINE_SIZE)

            slotDistance += 2 * HALF_CACHE_LINE_SIZE
            if (currKey.slotDistance(slot, mask) < slotDistance) {
                return onFail()
            }
        }
    }

    private fun removeSlot(slot: Int) {
        val kvArr = kvArr
        val emptyKey = emptyKey
        val mask = kvArr.size - 1

        var currSlot = slot
        var nextSlot = (currSlot + 2) and mask
        var nextKey = kvArr[nextSlot]
        while (nextKey != emptyKey && nextKey.slotDistance(nextSlot, mask) > 0) {
            kvArr[currSlot] = nextKey
            kvArr[currSlot + 1] = kvArr[nextSlot + 1]

            currSlot = nextSlot
            nextSlot = (nextSlot + 2) and mask
            nextKey = kvArr[nextSlot]
        }
        kvArr[currSlot] = emptyKey
        --size
    }

    override fun putAll(from: Long2LongMap) {
        if (from is Long2LongHashMap && from.size / 2 > size) {
            val old = iterator()
            resetTo(from)
            for ((key, value) in old) {
                set(key, value)
            }
        } else {
            ensureCapacity(max(size + (from.size / 2), from.size))
            for ((key, value) in from) {
                set(key, value)
            }
        }
    }

    override fun putAll(from: Map<out Long, Long>) {
        ensureCapacity(max(size + (from.size / 2), from.size))
        for ((key, value) in from) {
            set(key, value)
        }
    }

    private fun resetTo(from: Long2LongHashMap) {
        check(!from.isEmpty())

        kvArr = from.kvArr.copyOf()
        emptyKey = from.emptyKey
        size = from.size
        threshold = from.threshold
    }

    private var _keys: MutableLongSet? = null
    override val keys: MutableLongSet get() {
        return _keys ?:
            object : MutableLongSet {
                override val size: Int get() = this@Long2LongHashMap.size
                override fun contains(element: Long): Boolean = containsKey(element)
                override fun add(element: Long): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableLongIterator = KeyIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _keys = it }
    }

    private var _values: MutableLongCollection? = null
    override val values: MutableLongCollection get() {
        return _values ?:
            object : MutableLongCollection {
                override val size: Int get() = this@Long2LongHashMap.size
                override fun contains(element: Long): Boolean = containsValue(element)
                override fun add(element: Long): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableLongIterator = ValueIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _values = it }
    }

    private fun resizeIfNecessary() {
        if (kvArr === EMPTY_ARRAY) {
            rehash(threshold)
        } else if (size >= threshold) {
            rehash(threshold shl 1)
        }
    }

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    private fun rehash(capacity: Int) {
        check(capacity >= size)

        if (capacity == 0 && kvArr !== EMPTY_ARRAY) {
            kvArr = EMPTY_ARRAY
            emptyKey = ZERO
            threshold = DEFAULT_INITIAL_CAPACITY
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else .9f

        val newLength = arraySize(capacity, actualLoadFactor)
        if (kvArr.size == newLength) return

        val newKvArr = LongArray(newLength)
        if (emptyKey != ZERO) newKvArr.fill(emptyKey)
        val newMask = newKvArr.size - 1

        val oldKvArr = kvArr
        val emptyKey = emptyKey
        for (slot in oldKvArr.indices step 2) {
            val key = oldKvArr[slot]
            if (key != emptyKey) putRehashing(newKvArr, newMask, key, oldKvArr[slot + 1])
        }

        kvArr = newKvArr

        // threshold must always maintain the invariant of at least 1 slot being open
        val newCapacity = newLength / 2
        threshold = min((newCapacity * actualLoadFactor).toInt(), newCapacity - 1)
    }

    // we can assume key doesn't exist in array and that we never insert emptyKey
    private fun putRehashing(kvArr: LongArray, mask: Int, key: Long, value: Long) {
        val emptyKey = emptyKey

        var slot = key.slot(mask)
        var newKey = key
        var newValue = value
        var newSlotDistance = 0
        while (true) {
            val currKey = kvArr[slot]
            if (currKey == emptyKey) {
                kvArr[slot] = newKey
                kvArr[slot + 1] = newValue
                return
            }

            val currSlotDistance = currKey.slotDistance(slot, mask)
            if (newSlotDistance > currSlotDistance) {
                val currValue = kvArr[slot + 1]
                kvArr[slot] = newKey
                kvArr[slot + 1] = newValue
                newKey = currKey
                newValue = currValue
                newSlotDistance = currSlotDistance
            }

            slot = (slot + 2) and mask
            newSlotDistance += 2
        }
    }

    // changes emptyKey to a value not currently in the map, rewriting all empty slots
    private fun changeEmptyKey() {
        val oldEmptyKey = emptyKey

        // TODO: should we always try zero first or is that asking for trouble?
        var candidate = ZERO
        while (candidate == oldEmptyKey || findSlot(candidate, { true }, { false })) {
            candidate = Random.nextLong()
        }

        val kvArr = kvArr
        for (i in kvArr.indices step 2) {
            if (kvArr[i] == oldEmptyKey) kvArr[i] = candidate
        }
        emptyKey = candidate
    }

    override operator fun iterator(): MutableFastIterator<MutableLong2LongMap.MutableEntry> = FastEntryIterator()

    private open inner class SlotIterator {
        private val kvArr = this@Long2LongHashMap.kvArr
        private val emptyKey = this@Long2LongHashMap.emptyKey
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

        fun key(): Long = kvArr[previousSlot]
        fun value(): Long = kvArr[previousSlot + 1]

        fun updateValue(newValue: Long) {
            check(previousSlot != -1)
            if (kvArr !== this@Long2LongHashMap.kvArr) throw ConcurrentModificationException()
            kvArr[previousSlot + 1] = newValue
        }

        fun remove() {
            check(previousSlot != -1)
            if (kvArr !== this@Long2LongHashMap.kvArr) throw ConcurrentModificationException()

            removeSlot(previousSlot)
            previousSlot = -1

            // if removal wrapped all the way around to our next slot then we need to adjust
            if (kvArr[slot] == emptyKey) {
                slot = (slot - 2) and mask
            }
        }

        private fun decrement() {
            do {
                slot = (slot - 2) and mask
            } while (kvArr[slot] == emptyKey)
        }
    }

    private inner class KeyIterator : MutableLongIterator() {
        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()

        override fun nextLong(): Long {
            it.nextSlot()
            return it.key()
        }

        override fun remove() = it.remove()
    }

    private inner class ValueIterator : MutableLongIterator() {
        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()

        override fun nextLong(): Long {
            it.nextSlot()
            return it.value()
        }

        override fun remove() = it.remove()
    }

    private inner class FastEntryIterator: SlotIterator(), MutableFastIterator<MutableLong2LongMap.MutableEntry>, MutableLong2LongMap.MutableEntry {

        override val key: Long
            get() = key()
        override var value: Long
            get() = value()
            set(value) {
                updateValue(value)
            }

        override fun next(): MutableLong2LongMap.MutableEntry {
            nextSlot()
            return this
        }

        override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
        override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        override fun toString(): String = "$key=$value"
    }

    private fun Long.slot(mask: Int): Int {
        var h = hashCode()
        h = h xor (h ushr 16)
        h = h * PHI
        h = h xor (h ushr 16)
        return h and mask and 1.inv()
    }

    private fun Long.slotDistance(slot: Int, mask: Int): Int {
        var h = hashCode()
        h = h xor (h ushr 16)
        h = h * PHI
        h = h xor (h ushr 16)
        return (slot - (h and 1.inv())) and mask
    }

    internal companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Long = 0.toLong()

        private val EMPTY_ARRAY = longArrayOf(ZERO, ZERO)

        private const val PHI: Int = 0x9E3779B9.toInt()

        internal const val DEFAULT_INITIAL_CAPACITY = 7

        private const val CACHE_LINE_SIZE = 64 / Long.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2

        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * CACHE_LINE_SIZE

        private fun arraySize(capacity: Int, loadFactor: Float): Int {
            check(capacity >= 0)
            // array must always maintain the invariant of at least one slot remaining open
            val requiredArraySize = 2 * max((capacity / loadFactor).toInt(), capacity + 1)
            return 2 * ArrayUtils.minPowerOfTwo(requiredArraySize)
        }
    }
}
