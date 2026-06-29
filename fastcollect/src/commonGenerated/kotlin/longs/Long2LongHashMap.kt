@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.ArrayUtils
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.Hash
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
    capacity: Int = 0,
    /** The default value should be the value that is ideally least likely to occur in the map. */
    private val defaultValue: Long = Long.MIN_VALUE,
) : AbstractMutableLong2LongMap() {

    public constructor(map: Long2LongMap): this() { putAll(map) }

    public constructor(map: Map<Long, Long>): this() { putAll(map) }

    private var kvArr = EMPTY_ARRAY

    private var emptyKey = ZERO

    // threshold + size == capacity (rehash once threshold <= 0, if we haven't allocated yet then threshold.inv() is
    // our initial capacity)
    private var threshold = MIN_INITIAL_CAPACITY.inv()

    override var size: Int = 0
        private set

    init {
        ensureCapacity(capacity)
    }

    override fun isDefaultValue(value: Long): Boolean = value == defaultValue

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

    override fun containsKey(key: Long): Boolean = findSlot(key, { true }, { false })

    override fun containsValue(value: Long): Boolean {
        val kvArr = kvArr
        for (slot in 1..kvArr.size-1 step 2) {
            if (kvArr[slot] == value && kvArr[slot - 1] != emptyKey) return true
        }
        return false
    }

    override fun get(key: Long): Long = findSlot(key, { slot -> kvArr[slot + 1] }, { defaultValue })

    public fun getOrDefault(key: Long, default: Long): Long = findSlot(key, { slot -> kvArr[slot + 1] }, { default })

    override fun put(key: Long, value: Long): Long {
        var returnValue = ZERO
        set(key, {
            returnValue = defaultValue
            value
        }, { slot ->
            returnValue = kvArr[slot + 1]
            value
        })
        return returnValue
    }

    public fun putIfAbsent(key: Long, value: Long): Long {
        set(key, { value }, { slot -> return kvArr[slot + 1] })
        return defaultValue
    }

    override fun set(key: Long, value: Long) {
        set(key, { value }, { value })
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
            threshold += size
            kvArr.fill(emptyKey)
        }
        size = 0
    }

    private inline fun <T> findSlot(key: Long, onFind: (slot: Int) -> T, onFail: () -> T): T {
        val kvArr = kvArr
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

    private inline fun set(key: Long, onAdd: () -> Long, onReplace: (slot: Int) -> Long) {
        if (threshold <= 0) increaseCapacity()
        if (key == emptyKey) changeEmptyKey()

        val kvArr = kvArr
        val mask = kvArr.size - 1

        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            var currKey = kvArr[slot]
            if (currKey == key) {
                kvArr[slot + 1] = onReplace(slot)
                return
            } else if (currKey == emptyKey || distance > currKey.slotDistance(slot, mask)) {
                var newKey = key
                var newValue = onAdd()

                while (currKey != emptyKey) {
                    kvArr[slot] = newKey
                    newKey = currKey
                    val currValue = kvArr[slot + 1]
                    kvArr[slot + 1] = newValue
                    newValue = currValue
                    slot = (slot + 2) and mask
                    currKey = kvArr[slot]
                }

                kvArr[slot] = newKey
                kvArr[slot + 1] = newValue
                --threshold
                ++size
                return
            }

            slot = (slot + 2) and mask
            distance += 2
        }
    }

    private fun removeSlot(slot: Int) {
        val kvArr = kvArr
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
        ++threshold
        --size
    }

    override fun putAll(from: Long2LongMap) {
        if (from is Long2LongHashMap && from.size / 2 > size) {
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

        if (capacity == 0 && kvArr !== EMPTY_ARRAY) {
            kvArr = EMPTY_ARRAY
            emptyKey = ZERO
            threshold = MIN_INITIAL_CAPACITY.inv()
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else .9f

        val newLength = arraySize(capacity, actualLoadFactor)
        if (kvArr.size == newLength) return

        val newKvArr = LongArray(newLength)
        if (emptyKey != ZERO) newKvArr.fill(emptyKey)
        val newMask = newKvArr.size - 1

        for (slot in kvArr.indices step 2) {
            val key = kvArr[slot]
            if (key != emptyKey) setRehashing(newKvArr, newMask, key, kvArr[slot + 1])
        }

        kvArr = newKvArr

        // threshold must always maintain the invariant of at least 1 slot being open
        val newCapacity = newLength / 2
        threshold = min((newCapacity * actualLoadFactor).toInt(), newCapacity - 1) - size
    }

    // we can assume key doesn't exist in array and that we never insert emptyKey
    private fun setRehashing(kvArr: LongArray, mask: Int, key: Long, value: Long) {
        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            var currKey = kvArr[slot]
            if (currKey == emptyKey) {
                kvArr[slot] = key
                kvArr[slot + 1] = value
                return
            } else if (distance > currKey.slotDistance(slot, mask)) {
                var newKey = key
                var newValue = value

                do {
                    kvArr[slot] = newKey
                    newKey = currKey
                    val currValue = kvArr[slot + 1]
                    kvArr[slot + 1] = newValue
                    newValue = currValue
                    slot = (slot + 2) and mask
                    currKey = kvArr[slot]
                } while (currKey != emptyKey)

                kvArr[slot] = newKey
                kvArr[slot + 1] = newValue
                return
            }

            slot = (slot + 2) and mask
            ++distance
        }
    }

    // changes emptyKey to a value not currently in the map, rewriting all empty slots
    private fun changeEmptyKey() {
        var candidate = ZERO
        while (candidate == emptyKey || containsKey(candidate)) {
            candidate = Random.nextLong()
        }

        val kvArr = kvArr
        for (i in kvArr.indices step 2) {
            if (kvArr[i] == emptyKey) kvArr[i] = candidate
        }
        emptyKey = candidate
    }

    override operator fun iterator(): MutableFastIterator<MutableLong2LongMap.MutableEntry> = FastEntryIterator()

    public fun forEach(action: (Long, Long) -> Unit) {
        val kvArr = kvArr

        var slot = kvArr.size - 2
        while (slot >= 0) {
            val key = kvArr[slot]
            if(key != emptyKey) {
                action(key, kvArr[slot + 1])
            }
            slot -= 2
        }
    }

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

    private fun Long.slot(mask: Int): Int = Hash.fibonacciHashEven(this) shl 1 and mask
    private fun Long.slotDistance(slot: Int, mask: Int): Int = (slot - Hash.fibonacciHashEven(this)) and mask

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO = 0.toLong()

        private val EMPTY_ARRAY = longArrayOf(ZERO, ZERO)

        private const val MIN_INITIAL_CAPACITY = 7

        private const val CACHE_LINE_SIZE = 64 / Long.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2

        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX = 2 * CACHE_LINE_SIZE

        private fun arraySize(capacity: Int, loadFactor: Float): Int {
            check(capacity >= 0)
            // array must always maintain the invariant of at least one slot remaining open
            val requiredArraySize = 2 * max((capacity / loadFactor).toInt(), capacity + 1)
            return 2 * ArrayUtils.minPowerOfTwo(requiredArraySize)
        }
    }
}
