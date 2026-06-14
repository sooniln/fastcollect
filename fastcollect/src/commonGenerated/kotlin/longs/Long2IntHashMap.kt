@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.ArrayUtils
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator

import io.github.sooniln.fastcollect.ints.MutableIntCollection
import io.github.sooniln.fastcollect.ints.MutableIntIterator

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Long to Int
 * relationships.
 *
 * The [keys]/[values] mutable collections exposed by this class will throw [UnsupportedOperationException] on any
 * attempt to mutate the collection, EXCEPT that [MutableIterator.remove] will work as expected. Other mutation
 * operations should be made directly on the map rather than on the sub-collections.
 *
 * The entry [iterator] exposed by this class is a [FastIterator] - clients may not allow the returned entry to escape. See
 * the [FastIterator] documentation for more information.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 */
public class Long2IntHashMap @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,

    /** The default value should be the value that is ideally least likely to occur in the map. */
    private val defaultValue: Int = Int.MIN_VALUE,

) : AbstractMutableLong2IntMap() {

    init {
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(map: Long2IntMap): this() { putAll(map) }
    public constructor(map: Map<Long, Int>): this() { putAll(map) }

    // when used in hashing mode, the last slot in the array is used to store the zero key/value respectively. when used
    // in array mode, there is no special handling for zero.
    private var keysArr = EMPTY_KEY_ARRAY

    private var valuesArr = EMPTY_VALUE_ARRAY


    private var emptyKey = ZERO

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
        if (keysArr === EMPTY_KEY_ARRAY) {
            threshold = capacity
        } else if (capacity > threshold) {
            rehash(capacity)
        }
    }

    /**
     * Reduces the size of the backing array to the minimum required to hold the current number of elements.
     */
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    public fun trimToSize() {
        rehash(size)
    }

    override fun containsKey(key: Long): Boolean = findSlot(key, { true }, { false })

    override fun containsValue(value: Int): Boolean {
        val keysArr = keysArr
        val valuesArr = valuesArr
        val emptyKey = emptyKey
        for (slot in keysArr.indices) {
            if (valuesArr[slot] == value && keysArr[slot] != emptyKey) return true
        }
        return false
    }

    override fun get(key: Long): Int = findSlot(key, { slot -> valuesArr[slot] }, { defaultValue })

    override fun put(key: Long, value: Int): Int {
        resizeIfNecessary()

        if (key == emptyKey) changeEmptyKey()

        val keysArr = keysArr
        val valuesArr = valuesArr
        val emptyKey = emptyKey
        val mask = keysArr.size - 1

        var newKey = key
        var newValue: Int = value

        var slot = key.slot(mask)
        var newSlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == emptyKey) {
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                ++size
                return defaultValue
            } else if (currKey == newKey) {
                val oldValue = valuesArr[slot]
                valuesArr[slot] = newValue
                return oldValue
            }

            val currSlotDistance = currKey.slotDistance(slot, mask)
            if (newSlotDistance > currSlotDistance) {
                val currValue = valuesArr[slot]
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                newKey = currKey
                newValue = currValue
                newSlotDistance = currSlotDistance
            }

            slot = (slot + 1) and mask
            ++newSlotDistance
        }
    }

    override fun remove(key: Long): Int {
        return findSlot(
            key,
            { slot ->
                val oldValue = valuesArr[slot]
                removeSlot(slot)
                oldValue
            },
            { defaultValue })
    }

    override fun clear() {
        if (keysArr !== EMPTY_KEY_ARRAY) {
            keysArr.fill(emptyKey)

        }
        size = 0
    }

    private inline fun <T> findSlot(key: Long, onFind: (slot: Int) -> T, onFail: () -> T): T {
        val keysArr = keysArr
        val emptyKey = emptyKey
        val mask = keysArr.size - 1

        var slot = key.slot(mask)
        var currKey = keysArr[slot]

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

                slot = (slot + 1) and mask
                currKey = keysArr[slot]
            } while (++i < HALF_CACHE_LINE_SIZE)

            slotDistance += HALF_CACHE_LINE_SIZE
            if (currKey.slotDistance(slot, mask) < slotDistance) {
                return onFail()
            }
        }
    }

    private fun removeSlot(slot: Int) {
        val keysArr = keysArr
        val valuesArr = valuesArr
        val emptyKey = emptyKey
        val mask = keysArr.size - 1

        var currSlot = slot
        var nextSlot = (currSlot + 1) and mask
        var nextKey = keysArr[nextSlot]
        while (nextKey != emptyKey && nextKey.slotDistance(nextSlot, mask) > 0) {
            keysArr[currSlot] = nextKey
            valuesArr[currSlot] = valuesArr[nextSlot]

            currSlot = nextSlot
            nextSlot = (nextSlot + 1) and mask
            nextKey = keysArr[nextSlot]
        }
        keysArr[currSlot] = emptyKey

        --size
    }

    override fun putAll(from: Long2IntMap) {
        val it = if (from is Long2IntHashMap && from.size / 2 > size) {
            iterator().also { resetTo(from) }
        } else {
            from.iterator()
        }

        ensureCapacity(max(size + (from.size / 2), from.size))
        for ((key, value) in it) {
            set(key, value)
        }
    }

    override fun putAll(from: Map<out Long, Int>) {
        ensureCapacity(max(size + (from.size / 2), from.size))
        for ((key, value) in from) {
            set(key, value)
        }
    }

    private fun resetTo(from: Long2IntHashMap) {
        check(!from.isEmpty())

        keysArr = from.keysArr.copyOf()
        valuesArr = from.valuesArr.copyOf()
        emptyKey = from.emptyKey
        size = from.size
        threshold = from.threshold
    }

    private var _keys: MutableLongSet? = null
    override val keys: MutableLongSet get() {
        return _keys ?:
            object : MutableLongSet {
                override val size: Int get() = this@Long2IntHashMap.size
                override fun contains(element: Long): Boolean = containsKey(element)
                override fun add(element: Long): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableLongIterator = KeyIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _keys = it }
    }

    private var _values: MutableIntCollection? = null
    override val values: MutableIntCollection get() {
        return _values ?:

            object : MutableIntCollection {

                override val size: Int get() = this@Long2IntHashMap.size
                override fun contains(element: Int): Boolean = containsValue(element)
                override fun add(element: Int): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Int): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableIntIterator = ValueIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _values = it }
    }

    private fun resizeIfNecessary() {
        if (keysArr === EMPTY_KEY_ARRAY) {
            rehash(threshold)
        } else if (size >= threshold) {
            rehash(threshold shl 1)
        }
    }

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    private fun rehash(capacity: Int) {
        check(capacity >= size)

        if (capacity == 0 && keysArr !== EMPTY_KEY_ARRAY) {
            keysArr = EMPTY_KEY_ARRAY

            valuesArr = EMPTY_VALUE_ARRAY

            emptyKey = ZERO
            threshold = DEFAULT_INITIAL_CAPACITY
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else .9f

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = LongArray(newLength)
        if (emptyKey != ZERO) newKeysArr.fill(emptyKey)

        val newValuesArr = IntArray(newLength)

        val newMask = newKeysArr.size - 1

        val oldKeysArr = keysArr
        val emptyKey = emptyKey
        for (slot in oldKeysArr.indices) {
            val key = keysArr[slot]
            if (key != emptyKey) putRehashing(newKeysArr, newValuesArr, newMask, key, valuesArr[slot])
        }

        keysArr = newKeysArr
        valuesArr = newValuesArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newKeysArr.size * actualLoadFactor).toInt(), newKeysArr.size - 1)
    }

    // we can assume key doesn't exist in array and that we never insert emptyKey

    private fun putRehashing(keysArr: LongArray, valuesArr: IntArray, mask: Int, key: Long, value: Int) {

        val emptyKey = emptyKey

        var slot = key.slot(mask)
        var newKey = key
        var newValue = value
        var newSlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == emptyKey) {
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                return
            }

            val currSlotDistance = currKey.slotDistance(slot, mask)
            if (newSlotDistance > currSlotDistance) {
                keysArr[slot] = newKey
                newKey = currKey
                val currValue = valuesArr[slot]
                valuesArr[slot] = newValue
                newValue = currValue
                newSlotDistance = currSlotDistance
            }

            slot = (slot + 1) and mask
            ++newSlotDistance
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

        val keysArr = keysArr
        for (i in keysArr.indices) {
            if (keysArr[i] == oldEmptyKey) keysArr[i] = candidate
        }
        emptyKey = candidate
    }

    override operator fun iterator(): MutableFastIterator<MutableLong2IntMap.MutableEntry> = FastEntryIterator()

    private open inner class SlotIterator {
        private val keysArr = this@Long2IntHashMap.keysArr
        private val valuesArr = this@Long2IntHashMap.valuesArr
        private val emptyKey = this@Long2IntHashMap.emptyKey
        private val mask = keysArr.size - 1

        private var slotsLeft = size
        private var slot = keysArr.size - 1
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

        fun key(): Long = keysArr[previousSlot]
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        fun value(): Int = valuesArr[previousSlot] as Int

        fun updateValue(newValue: Int) {
            check(previousSlot != -1)
            if (keysArr !== this@Long2IntHashMap.keysArr) throw ConcurrentModificationException()
            valuesArr[previousSlot] = newValue
        }

        fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@Long2IntHashMap.keysArr) throw ConcurrentModificationException()

            removeSlot(previousSlot)
            previousSlot = -1

            // if removal wrapped all the way around to our next slot then we need to adjust
            if (keysArr[slot] == emptyKey) {
                slot = (slot - 1) and mask
            }
        }

        private fun decrement() {
            do {
                slot = (slot - 1) and mask
            } while (keysArr[slot] == emptyKey)
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


    private inner class ValueIterator : MutableIntIterator() {

        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()


        override fun nextInt(): Int {

            it.nextSlot()
            return it.value()
        }

        override fun remove() = it.remove()
    }

    private inner class FastEntryIterator: SlotIterator(), MutableFastIterator<MutableLong2IntMap.MutableEntry>, MutableLong2IntMap.MutableEntry {

        override val key: Long get() = key()
        override var value: Int
            get() = value()
            set(value) {
                updateValue(value)
            }

        override fun next(): MutableLong2IntMap.MutableEntry {
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
        return h and mask
    }

    private fun Long.slotDistance(slot: Int, mask: Int): Int {
        var h = hashCode()
        h = h xor (h ushr 16)
        h = h * PHI
        h = h xor (h ushr 16)
        return (slot - h) and mask
    }

    internal companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Long = 0.toLong()

        private val EMPTY_KEY_ARRAY = longArrayOf(ZERO)

        private val EMPTY_VALUE_ARRAY = IntArray(1)


        // Knuth multiplicative hash
        private const val PHI: Int = 0x9E3779B9.toInt()

        internal const val DEFAULT_INITIAL_CAPACITY = 7

        private const val CACHE_LINE_SIZE = 64 / Long.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2
        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * CACHE_LINE_SIZE

        private fun arraySize(capacity: Int, loadFactor: Float): Int {
            check(capacity >= 0)
            // array must always maintain the invariant of at least one slot remaining open
            val requiredArraySize = max((capacity / loadFactor).toInt(), capacity + 1)
            return ArrayUtils.minPowerOfTwo(requiredArraySize)
        }
    }
}
