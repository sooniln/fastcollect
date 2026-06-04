@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableFastIterator

import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleIterator

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Int to Double
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
public class Int2DoubleHashMap @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,

    /** The default value should be the value that is ideally least likely to occur in the map. */
    override val defaultValue: Double = Double.NaN,

) : AbstractMutableInt2DoubleMap() {

    init {
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(map: Int2DoubleMap): this() { putAll(map) }

    public constructor(map: Map<Int, Double>): this() { putAll(map) }

    // when used in hashing mode, the last slot in the array is used to store the zero key/value respectively. when used
    // in array mode, there is no special handling for zero.
    private var keysArr = EMPTY_KEY_ARRAY
    private var mask = keysArr.mask()

    private var valuesArr = EMPTY_VALUE_ARRAY


    override var size: Int = 0
        private set

    // use threshold to store the initial size before we allocate anything, after that it's the size at which we rehash
    private var threshold: Int = if (capacity == 0) DEFAULT_INITIAL_CAPACITY else capacity

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

    override fun put(key: Int, value: Double): Double {
        resizeIfNecessary()

        val keysArr = keysArr
        val valuesArr = valuesArr

        if (key == ZERO) {
            val endSlot = keysArr.endSlot()
            val oldValue = if (keysArr[endSlot] != ZERO) {
                keysArr[endSlot] = ZERO
                ++size
                defaultValue
            } else {
                valuesArr[endSlot]
            }
            valuesArr[endSlot] = value
            return oldValue
        }

        val mask = mask
        val rotVal = mask.rotVal()

        var slot = key.slot(mask, rotVal)
        var newKey = key
        var newValue: Double = value
        var newKeySlotDistance = 0
        while (true) {
            when (val currKey = keysArr[slot]) {
                newKey -> {
                    val oldValue = valuesArr[slot]
                    valuesArr[slot] = newValue
                    return oldValue
                }
                ZERO -> {
                    keysArr[slot] = newKey
                    valuesArr[slot] = newValue
                    ++size
                    return defaultValue
                }
                else -> {
                    val currKeySlotDistance = currKey.slotDistance(slot, mask, rotVal)
                    if (newKeySlotDistance > currKeySlotDistance) {
                        val currValue = valuesArr[slot]
                        keysArr[slot] = newKey
                        valuesArr[slot] = newValue
                        newKey = currKey
                        newValue = currValue
                        newKeySlotDistance = currKeySlotDistance
                    }
                }
            }

            slot = slot.nextSlot(mask)
            ++newKeySlotDistance
        }
    }

    override fun remove(key: Int): Double {
        val slot = findSlot(key)
        if (slot >= 0) {
            val oldValue = valuesArr[slot]
            removeSlot(slot)
            return oldValue
        }

        return defaultValue
    }

    override fun clear() {
        if (keysArr !== EMPTY_KEY_ARRAY) {
            keysArr.fill(ZERO)

            keysArr[keysArr.endSlot()] = NONZERO
        }
        size = 0
    }

    private fun findSlot(key: Int): Int {
        val keysArr = keysArr

        if (key == ZERO) {
            val endSlot = keysArr.endSlot()
            return if (keysArr[endSlot] != ZERO) -1 else endSlot
        }

        val mask = mask
        val rotVal = mask.rotVal()

        var slot = key.slot(mask, rotVal)
        var currKey = keysArr[slot]

        if (currKey == key) {
            return slot
        } else if (currKey == ZERO) {
            return -1
        }

        var slotDistance = 1
        while (true) {
            // checking whether the current slot distance is higher than our search distance allows us to early exit the
            // search loop - but the cost of checking is non-trivial. as a compromise between GetHit and GetMiss
            // performance we only check once every half cache line.
            repeat(HALF_CACHE_LINE_SIZE) {
                slot = slot.nextSlot(mask)
                currKey = keysArr[slot]

                if (currKey == key) {
                    return slot
                } else if (currKey == ZERO) {
                    return -1
                }
            }

            slotDistance += HALF_CACHE_LINE_SIZE
            if (currKey.slotDistance(slot, mask, rotVal) < slotDistance) {
                return -1
            }
        }
    }

    private fun removeSlot(slot: Int) {
        val keysArr = keysArr
        val valuesArr = valuesArr

        val endSlot = keysArr.endSlot()
        if (slot == endSlot) {
            keysArr[endSlot] = NONZERO

            --size
            return
        }

        val mask = mask
        val rotVal = mask.rotVal()

        // move all slots left until we hit a zero slot
        var currSlot = slot
        var nextSlot = currSlot.nextSlot(mask)
        var nextKey = keysArr[nextSlot]
        var nextValue = valuesArr[nextSlot]
        while (nextKey != ZERO && nextKey.slotDistance(nextSlot, mask, rotVal) > 0) {
            keysArr[currSlot] = nextKey
            valuesArr[currSlot] = nextValue

            currSlot = nextSlot
            nextSlot = nextSlot.nextSlot(mask)
            nextKey = keysArr[nextSlot]
            nextValue = valuesArr[nextSlot]
        }
        keysArr[currSlot] = ZERO

        --size
    }

    override fun putAll(from: Int2DoubleMap) {
        if (isEmpty() && from is Int2DoubleHashMap) {
            initializeFrom(from)
        } else {
            ensureCapacity(max(size + (from.size shr 1), from.size))
            for (entry in from) {
                set(entry.key, entry.value)
            }
        }
    }

    override fun putAll(from: Map<out Int, Double>) {
        ensureCapacity(max(size + (from.size shr 1), from.size))
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    private fun initializeFrom(from: Int2DoubleHashMap) {
        check(isEmpty())

        if (from.isEmpty()) return

        keysArr = from.keysArr.copyOf()
        mask = from.mask
        valuesArr = from.valuesArr.copyOf()
        size = from.size
        threshold = from.threshold
    }

    private var _keys: MutableIntSet? = null
    override val keys: MutableIntSet get() {
        return _keys ?:
            object : MutableIntSet {
                override val size: Int get() = this@Int2DoubleHashMap.size
                override fun contains(element: Int): Boolean = containsKey(element)
                override fun add(element: Int): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Int): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableIntIterator = KeyIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _keys = it }
    }

    private var _values: MutableDoubleCollection? = null
    override val values: MutableDoubleCollection get() {
        return _values ?:

            object : MutableDoubleCollection {

                override val size: Int get() = this@Int2DoubleHashMap.size
                override fun contains(element: Double): Boolean = containsValue(element)
                override fun add(element: Double): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Double): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableDoubleIterator = ValueIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _values = it }
    }

    override fun containsKey(key: Int): Boolean {
        return findSlot(key) >= 0
    }

    override fun containsValue(value: Double): Boolean {
        val endSlot = keysArr.endSlot()
        if (valuesArr[endSlot] == value && keysArr[endSlot] == ZERO) return true

        var slot = 0
        while (slot < endSlot) {
            if (valuesArr[slot] == value && keysArr[slot] != ZERO) return true
            ++slot
        }
        return false
    }

    override fun get(key: Int): Double {
        val slot = findSlot(key)
        return if (slot >= 0) valuesArr[slot] else defaultValue
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
            mask = keysArr.mask()

            valuesArr = EMPTY_VALUE_ARRAY

            threshold = DEFAULT_INITIAL_CAPACITY
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else .9f

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = IntArray(newLength)

        val newValuesArr = DoubleArray(newLength)

        val newMask = newKeysArr.mask()
        val newRotVal = newMask.rotVal()
        val newEndSlot = newKeysArr.endSlot()

        val oldEndSlot = keysArr.endSlot()
        for (slot in 0..<oldEndSlot) {
            val key = keysArr[slot]
            if (key != ZERO) putRehashing(newKeysArr, newValuesArr, newMask, newRotVal, key, valuesArr[slot])
        }
        newKeysArr[newEndSlot] = keysArr[oldEndSlot]
        newValuesArr[newEndSlot] = valuesArr[oldEndSlot]

        keysArr = newKeysArr
        mask = newMask
        valuesArr = newValuesArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newEndSlot * actualLoadFactor).toInt(), newEndSlot - 1)
    }

    // we can assume key doesn't exist in array and that we never insert zero

    private fun putRehashing(keysArr: IntArray, valuesArr: DoubleArray, mask: Int, rotVal: Int, key: Int, value: Double) {

        var slot = key.slot(mask, rotVal)
        var newKey = key
        var newValue = value
        var newKeySlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == ZERO) {
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                return
            }

            val currKeySlotDistance = currKey.slotDistance(slot, mask, rotVal)
            if (newKeySlotDistance > currKeySlotDistance) {
                keysArr[slot] = newKey
                newKey = currKey
                val currValue = valuesArr[slot]
                valuesArr[slot] = newValue
                newValue = currValue
                newKeySlotDistance = currKeySlotDistance
            }

            slot = slot.nextSlot(mask)
            ++newKeySlotDistance
        }
    }

    override operator fun iterator(): MutableFastIterator<MutableInt2DoubleMap.MutableEntry> = FastEntryIterator()

    private open inner class SlotIterator {
        private val keysArr = this@Int2DoubleHashMap.keysArr
        private val mask = this@Int2DoubleHashMap.mask
        private val valuesArr = this@Int2DoubleHashMap.valuesArr

        private var slotsLeft = size

        private var slot = keysArr.endSlot()
        private var previousSlot = -1

        init {
            if (keysArr[slot] != ZERO && slotsLeft > 0) {
                decrement()
            }
        }

        fun hasNext(): Boolean {
            return slotsLeft > 0
        }

        fun nextSlot() {
            if (slotsLeft <= 0) throw NoSuchElementException()
            previousSlot = slot
            if (--slotsLeft > 0) decrement()
        }

        fun slot(): Int = previousSlot.also { check(it != -1) }
        fun key(): Int = keysArr[previousSlot]
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        fun value(): Double = valuesArr[previousSlot] as Double

        fun updateValue(newValue: Double) {
            check(previousSlot != -1)
            if (keysArr !== this@Int2DoubleHashMap.keysArr) throw ConcurrentModificationException()
            valuesArr[previousSlot] = newValue
        }

        fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@Int2DoubleHashMap.keysArr) throw ConcurrentModificationException()

            removeSlot(previousSlot)
            previousSlot = -1
        }

        private fun decrement() {
            do {
                slot = (slot - 1) and mask
            } while (keysArr[slot] == ZERO)
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


    private inner class ValueIterator : MutableDoubleIterator() {

        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()


        override fun nextDouble(): Double {

            it.nextSlot()
            return it.value()
        }

        override fun remove() = it.remove()
    }

    private inner class FastEntryIterator: SlotIterator(), MutableFastIterator<MutableInt2DoubleMap.MutableEntry>, MutableInt2DoubleMap.MutableEntry {

        override val key: Int get() = key()
        override var value: Double
            get() = value()
            set(value) {
                updateValue(value)
            }

        override fun next(): MutableInt2DoubleMap.MutableEntry {
            nextSlot()
            return this
        }

        override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
        override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        override fun toString(): String = "$key=$value"
    }

    // the slot at the end of slot iteration (exclusive), also the slot that stores the zero value
    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.endSlot(): Int = size - 1

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.mask(): Int = size - 2

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.rotVal(): Int = countOneBits()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mixHash(hashcode: Int, rotVal: Int): Int {
        return (hashcode * PHI).rotateLeft(rotVal)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slot(mask: Int, rotVal: Int): Int {
        return mixHash(this.hashCode(), rotVal) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.nextSlot(mask: Int): Int {
        return (this + 1) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slotDistance(slot: Int, mask: Int, rotVal: Int): Int {
        return (slot - slot(mask, rotVal)) and mask
    }

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Int = 0.toInt()
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val NONZERO: Int = 1.toInt()

        private val EMPTY_KEY_ARRAY = intArrayOf(ZERO, NONZERO)

        private val EMPTY_VALUE_ARRAY = DoubleArray(2)


        // Knuth multiplicative hash
        private const val PHI: Int = 0x9E3779B9.toInt()

        private const val DEFAULT_INITIAL_CAPACITY = 7
        private const val MAXIMUM_CAPACITY: Int = 1 shl 30 // must be power of two

        private const val CACHE_LINE_SIZE = 64 / Int.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2
        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * CACHE_LINE_SIZE

        private fun arraySize(capacity: Int, loadFactor: Float): Int {
            check(capacity >= 0)
            // array must always maintain the invariant of at least one slot remaining open
            val requiredArraySize = max((capacity / loadFactor).toInt(), capacity + 1)
            // add extra slot to hold zero value at the end
            return minPowerOfTwo(requiredArraySize) + 1
        }

        private fun minPowerOfTwo(cap: Int): Int {
            val n = -1 ushr (cap - 1).countLeadingZeroBits()
            return if (n < 0) 1 else if (n >= MAXIMUM_CAPACITY) MAXIMUM_CAPACITY else n + 1
        }
    }
}
