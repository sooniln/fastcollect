@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.MutableFastIterator

import io.github.sooniln.fastcollect.ints.MutableIntCollection
import io.github.sooniln.fastcollect.ints.MutableIntIterator

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Int to Int
 * relationships. Can be used in place of the Kotlin standard library [HashMap] implementations to improve performance
 * and memory usage. Has the same API contracts as the standard library [HashMap] unless noted otherwise.
 *
 * This implementation differs in behavior from common hash sets in that at low capacity numbers it will effectively
 * force a loadFactor of 1. This may improve performance slightly at low capacities, or at worst will be only a small
 * performance penalty - however it will substantially reduce memory requirements at low capacities.
 *

 * Note that unfortunately many of the common Kotlin Map methods may force primitive type boxing, and thus could incur
 * performance penalties. These methods have been marked as deprecated so they will be easily visible in IDEs. It is
 * encouraged to use the replacement methods this class offers in order to guarantee no unnecessary boxing will occur:
 *
 *   * Use [lookup] instead of [Map.get] or the indexed read operator.
 *   * Use [putValue], [set] or the indexed write operator instead of [MutableMap.put].
 *   * Use [removeKey] instead of [MutableMap.remove].
 *   * Use [primitiveEntries] instead of [Map.entries].
 *   * Use [Entry.key] instead of [Map.Entry.key] and [Entry.value] instead of [Map.Entry.value].

 *
 * The [keys]/[values]/[entries]/[primitiveEntries] mutable collections exposed by this class will throw
 * [UnsupportedOperationException] on any attempt to mutate the collection, EXCEPT that [MutableIterator.remove] will
 * work as expected. Mutation operations should be made directly on this map instead.
 *
 * This class offers faster iteration over entries - a faster iterator can be accessed via [fastIterator] or
 * `primitiveEntries.fastIterator()` with the caveat that it may not be used if the entries returned by
 * [FastIterator.next] escape the iteration loop. See [io.github.sooniln.fastcollect.FastIterable] for more explanation
 * and details.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Note that a load factor of 1.0 is accepted, unlike many HashMaps - this is interpreted to mean that only 1 slot need
 * ever remain free (i.e. the actual load factor is (capacity - 1)/capacity).
 */
public class Int2IntHashMap @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR,

    /** The default value should be the value that is ideally least likely to occur in the map. */
    override val defaultValue: Int = Int.MIN_VALUE

) : AbstractMutableInt2IntMap() {

    init {
        require(loadFactor > 0 && loadFactor <= 1) { "Load factor must be > 0 and <= 1" }
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(map: Map<Int, Int>): this(map.size) {
        putAll(map)
    }

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

    override fun putValue(key: Int, value: Int): Int {
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
        var slot = key.slot(mask)
        var newKey = key
        var newValue: Int = value
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

    override fun removeKey(key: Int): Int {
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
        var slot = key.slot(mask)
        var slotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == key) {
                return slot
            } else if (currKey == ZERO || currKey.slotDistance(slot, mask, rotVal) < slotDistance) {
                return -1
            }

            slot = slot.nextSlot(mask)
            ++slotDistance
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

    override fun putAll(from: Map<out Int, Int>) {
        ensureCapacity(max(size + (from.size shr 1), from.size))

        if (from is Int2IntMap) {
            for (entry in from.fastIterator()) {
                set(entry.key(), entry.value())
            }
        } else {
            for (entry in from) {
                set(entry.key, entry.value)
            }
        }
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

    private var _primitiveEntries: MutableEntrySet<MutableInt2IntMap.MutableEntry>? = null
    override val primitiveEntries: MutableEntrySet<MutableInt2IntMap.MutableEntry> get() {
        return _primitiveEntries ?:
            object : AbstractMutableSet<MutableInt2IntMap.MutableEntry>(), MutableEntrySet<MutableInt2IntMap.MutableEntry> {
                override val size: Int get() = this@Int2IntHashMap.size
                override fun contains(element: MutableInt2IntMap.MutableEntry): Boolean {
                    val value = lookup(element.key())
                    return if (isDefaultValue(value) && !containsKey(element.key())) false else value == element.value()
                }
                override fun add(element: MutableInt2IntMap.MutableEntry): Boolean = throw UnsupportedOperationException()
                override fun remove(element: MutableInt2IntMap.MutableEntry): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableIterator<MutableInt2IntMap.MutableEntry> = EntryIterator()
                override fun fastIterator(): MutableFastIterator<MutableInt2IntMap.MutableEntry> = FastEntryIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _primitiveEntries = it }
    }

    override fun containsKey(key: Int): Boolean {
        return findSlot(key) >= 0
    }

    override fun containsValue(value: Int): Boolean {
        val endSlot = keysArr.endSlot()
        if (valuesArr[endSlot] == value && keysArr[endSlot] == ZERO) return true

        var slot = 0
        while (slot < endSlot) {
            if (valuesArr[slot] == value && keysArr[slot] != ZERO) return true
            ++slot
        }
        return false
    }

    override fun lookup(key: Int): Int {
        val valuesArr = valuesArr
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
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else loadFactor

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = IntArray(newLength)

        val newValuesArr = IntArray(newLength)

        val newMask = newKeysArr.mask()
        val newEndSlot = newKeysArr.endSlot()

        val newRotVal = newMask.rotVal()
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

    private fun putRehashing(keysArr: IntArray, valuesArr: IntArray, mask: Int, rotVal: Int, key: Int, value: Int) {

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

    override operator fun iterator(): MutableIterator<MutableInt2IntMap.MutableEntry> = EntryIterator()

    override fun fastIterator(): MutableFastIterator<MutableInt2IntMap.MutableEntry> = FastEntryIterator()

    private open inner class SlotIterator {
        val keysArr = this@Int2IntHashMap.keysArr
        private val mask = this@Int2IntHashMap.mask
        val valuesArr = this@Int2IntHashMap.valuesArr

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
            if (slotsLeft-- <= 0) throw NoSuchElementException()
            previousSlot = slot
            if (slotsLeft > 0) decrement()
        }

        fun slot(): Int = previousSlot.also { check(it != -1) }
        fun key(): Int = keysArr[previousSlot]
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        fun value(): Int = valuesArr[previousSlot] as Int

        protected fun updateValue(newValue: Int) {
            check(previousSlot != -1)
            if (keysArr !== this@Int2IntHashMap.keysArr) throw ConcurrentModificationException()
            valuesArr[previousSlot] = newValue
        }

        fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@Int2IntHashMap.keysArr) throw ConcurrentModificationException()

            removeSlot(previousSlot)
            previousSlot = -1
        }

        private fun decrement() {
            // see the similar function within HashSet for an explanation and discussion of why we stride by 17 here
            var s = slot
            do {
                s = (s - 17) and mask
            } while (keysArr[s] == ZERO)
            slot = s
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

        override fun setValue(newValue: Int): Int {
            val oldValue = value()
            updateValue(newValue)
            return oldValue
        }

        override fun next(): MutableInt2IntMap.MutableEntry {
            nextSlot()
            return this
        }
    }

    private inner class EntryIterator : SlotIterator(), MutableIterator<MutableInt2IntMap.MutableEntry> {

        override fun next(): MutableInt2IntMap.MutableEntry {
            nextSlot()
            return object : MutableInt2IntMap.MutableEntry {
                private val slot = slot()

                override fun key(): Int = keysArr[slot]
                @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                override fun value(): Int = valuesArr[slot] as Int

                override fun setValue(newValue: Int): Int {
                    val oldValue = value()
                    if (keysArr !== this@Int2IntHashMap.keysArr) throw ConcurrentModificationException()
                    valuesArr[slot] = newValue
                    return oldValue
                }
            }
        }
    }

    // the slot at the end of slot iteration (exclusive), also the slot that stores the zero value
    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.endSlot(): Int = size - 1

    // caching the mask in a member var can shave a little off get miss latency - at the cost of +4 bytes per instance
    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.mask() = size - 2

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.rotVal(): Int = countOneBits()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mixHash(hashcode: Int, mask: Int, rotVal: Int): Int {
        // see equivalent function in HashSet for explanation and commentary
        return (hashcode * K).rotateLeft(rotVal)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slot(mask: Int, rotVal: Int = mask.rotVal()): Int {
        return mixHash(this.hashCode(), mask, rotVal) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.nextSlot(mask: Int): Int {
        return (this + 1) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slotDistance(slot: Int, mask: Int, rotVal: Int = mask.rotVal()): Int {
        return (slot - slot(mask, rotVal)) and mask
    }

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Int = 0.toInt()
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val NONZERO: Int = 1.toInt()

        private val EMPTY_KEY_ARRAY = intArrayOf(ZERO, NONZERO)

        private val EMPTY_VALUE_ARRAY = IntArray(2)


        // Constant taken from:
        //     "Computationally Easy, Spectrally Good Multipliers for Congruential
        //     Pseudorandom Number Generators" by Guy Steele and Sebastiano Vigna.
        private const val K: Int = 0x93d765dd.toInt()

        private const val DEFAULT_LOAD_FACTOR = .85f
        private const val DEFAULT_INITIAL_CAPACITY = 1 shl 2  // must be power of two
        private const val MAXIMUM_CAPACITY: Int = 1 shl 30 // must be power of two

        // we force the load factor to 1.0 up to the size of two cache lines (which we assume are 64 bytes each)
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * 64 / Int.SIZE_BYTES

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
