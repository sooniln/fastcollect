@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.MutableFastIterator

import io.github.sooniln.fastcollect.floats.MutableFloatCollection
import io.github.sooniln.fastcollect.floats.MutableFloatIterator

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Long to Float
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
 */
public class Long2FloatHashMap @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR,

    /** The default value should be the value that is ideally least likely to occur in the map. */
    override val defaultValue: Float = Float.NaN

) : AbstractMutableLong2FloatMap() {

    init {
        require(loadFactor > 0 && loadFactor < 1) { "Load factor must be greater than 0 and smaller than 1" }
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
    }

    public constructor(map: Map<Long, Float>): this(map.size) {
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

    override fun putValue(key: Long, value: Float): Float {
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
        var slot = key.slot(mask)
        var newKey = key
        var newValue: Float = value
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
                    val currKeySlotDistance = currKey.slotDistance(slot, mask)
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

    override fun removeKey(key: Long): Float {
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

    private fun findSlot(key: Long): Int {
        val keysArr = keysArr

        if (key == ZERO) {
            val endSlot = keysArr.endSlot()
            return if (keysArr[endSlot] != ZERO) -1 else endSlot
        }

        val mask = mask
        var slot = key.slot(mask)
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == key) {
                return slot
            } else if (currKey == ZERO) {
                return -1
            }

            // do not bother to compare slot distances to break out of the loop - in benchmarking this has not proved
            // worth it. the calculation overhead is likely too high since we don't cache hash/DIBs. worsened latency in
            // lookup hits is ~4x the improved latency in lookup misses on average...

            slot = slot.nextSlot(mask)
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

        // move all slots left until we hit a zero slot
        var currSlot = slot
        var nextSlot = currSlot.nextSlot(mask)
        var nextKey = keysArr[nextSlot]
        var nextValue = valuesArr[nextSlot]
        while (nextKey != ZERO && nextKey.slotDistance(nextSlot, mask) > 0) {
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

    override fun putAll(from: Map<out Long, Float>) {
        ensureCapacity(max(size + (from.size shr 1), from.size))

        if (from is Long2FloatMap) {
            for (entry in from.fastIterator()) {
                set(entry.key(), entry.value())
            }
        } else {
            for (entry in from) {
                set(entry.key, entry.value)
            }
        }
    }

    private var _keys: MutableLongSet? = null
    override val keys: MutableLongSet get() {
        return _keys ?:
            object : MutableLongSet {
                override val size: Int get() = this@Long2FloatHashMap.size
                override fun contains(element: Long): Boolean = containsKey(element)
                override fun add(element: Long): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableLongIterator = KeyIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _keys = it }
    }

    private var _values: MutableFloatCollection? = null
    override val values: MutableFloatCollection get() {
        return _values ?:

            object : MutableFloatCollection {

                override val size: Int get() = this@Long2FloatHashMap.size
                override fun contains(element: Float): Boolean = containsValue(element)
                override fun add(element: Float): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Float): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableFloatIterator = ValueIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _values = it }
    }

    private var _primitiveEntries: MutableEntrySet<MutableLong2FloatMap.MutableEntry>? = null
    override val primitiveEntries: MutableEntrySet<MutableLong2FloatMap.MutableEntry> get() {
        return _primitiveEntries ?:
            object : AbstractMutableSet<MutableLong2FloatMap.MutableEntry>(), MutableEntrySet<MutableLong2FloatMap.MutableEntry> {
                override val size: Int get() = this@Long2FloatHashMap.size
                override fun contains(element: MutableLong2FloatMap.MutableEntry): Boolean {
                    val value = lookup(element.key())
                    return if (isDefaultValue(value) && !containsKey(element.key())) false else value == element.value()
                }
                override fun add(element: MutableLong2FloatMap.MutableEntry): Boolean = throw UnsupportedOperationException()
                override fun remove(element: MutableLong2FloatMap.MutableEntry): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableIterator<MutableLong2FloatMap.MutableEntry> = EntryIterator()
                override fun fastIterator(): MutableFastIterator<MutableLong2FloatMap.MutableEntry> = FastEntryIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _primitiveEntries = it }
    }

    override fun containsKey(key: Long): Boolean {
        return findSlot(key) >= 0
    }

    override fun containsValue(value: Float): Boolean {
        val endSlot = keysArr.endSlot()
        if (valuesArr[endSlot] == value && keysArr[endSlot] == ZERO) return true

        var slot = 0
        while (slot < endSlot) {
            if (valuesArr[slot] == value && keysArr[slot] != ZERO) return true
            ++slot
        }
        return false
    }

    override fun lookup(key: Long): Float {
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

        val newKeysArr = LongArray(newLength)

        val newValuesArr = FloatArray(newLength)

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

    private fun putRehashing(keysArr: LongArray, valuesArr: FloatArray, mask: Int, rotVal: Int, key: Long, value: Float) {

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

            val currKeySlotDistance = currKey.slotDistance(slot, mask)
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

    override operator fun iterator(): MutableIterator<MutableLong2FloatMap.MutableEntry> = EntryIterator()

    override fun fastIterator(): MutableFastIterator<MutableLong2FloatMap.MutableEntry> = FastEntryIterator()

    private open inner class SlotIterator {
        val keysArr = this@Long2FloatHashMap.keysArr
        private val mask = this@Long2FloatHashMap.mask
        val valuesArr = this@Long2FloatHashMap.valuesArr

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
        fun key(): Long = keysArr[previousSlot]
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        fun value(): Float = valuesArr[previousSlot] as Float

        protected fun updateValue(newValue: Float) {
            check(previousSlot != -1)
            if (keysArr !== this@Long2FloatHashMap.keysArr) throw ConcurrentModificationException()
            valuesArr[previousSlot] = newValue
        }

        fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@Long2FloatHashMap.keysArr) throw ConcurrentModificationException()

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

    private inner class KeyIterator : MutableLongIterator() {
        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()
        override fun nextLong(): Long {
            it.nextSlot()
            return it.key()
        }
        override fun remove() = it.remove()
    }


    private inner class ValueIterator : MutableFloatIterator() {

        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()


        override fun nextFloat(): Float {

            it.nextSlot()
            return it.value()
        }

        override fun remove() = it.remove()
    }

    private inner class FastEntryIterator: SlotIterator(), MutableFastIterator<MutableLong2FloatMap.MutableEntry>, MutableLong2FloatMap.MutableEntry {

        override fun setValue(newValue: Float): Float {
            val oldValue = value()
            updateValue(newValue)
            return oldValue
        }

        override fun next(): MutableLong2FloatMap.MutableEntry {
            nextSlot()
            return this
        }
    }

    private inner class EntryIterator : SlotIterator(), MutableIterator<MutableLong2FloatMap.MutableEntry> {

        override fun next(): MutableLong2FloatMap.MutableEntry {
            nextSlot()
            return object : MutableLong2FloatMap.MutableEntry {
                private val slot = slot()

                override fun key(): Long = keysArr[slot]
                @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                override fun value(): Float = valuesArr[slot] as Float

                override fun setValue(newValue: Float): Float {
                    val oldValue = value()
                    if (keysArr !== this@Long2FloatHashMap.keysArr) throw ConcurrentModificationException()
                    valuesArr[slot] = newValue
                    return oldValue
                }
            }
        }
    }

    // the slot at the end of slot iteration (exclusive), also the slot that stores the zero value
    @Suppress("NOTHING_TO_INLINE")
    private inline fun LongArray.endSlot(): Int = size - 1

    // caching the mask in a member var can shave a little off get miss latency - at the cost of +4 bytes per instance
    @Suppress("NOTHING_TO_INLINE")
    private inline fun LongArray.mask() = size - 2

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.rotVal(): Int = countOneBits()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mixHash(hashcode: Int, mask: Int, rotVal: Int): Int {
        // see equivalent function in HashSet for explanation and commentary
        return (hashcode * K).rotateLeft(rotVal)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Long.slot(mask: Int, rotVal: Int = mask.rotVal()): Int {
        return mixHash(this.hashCode(), mask, rotVal) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.nextSlot(mask: Int): Int {
        return (this + 1) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Long.slotDistance(slot: Int, mask: Int): Int {
        return (slot - slot(mask)) and mask
    }

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Long = 0.toLong()
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val NONZERO: Long = 1.toLong()

        private val EMPTY_KEY_ARRAY = longArrayOf(ZERO, NONZERO)

        private val EMPTY_VALUE_ARRAY = FloatArray(2)


        // Constant taken from:
        //     "Computationally Easy, Spectrally Good Multipliers for Congruential
        //     Pseudorandom Number Generators" by Guy Steele and Sebastiano Vigna.
        private const val K: Int = 0x93d765dd.toInt()

        private const val DEFAULT_LOAD_FACTOR = .85f
        private const val DEFAULT_INITIAL_CAPACITY = 1 shl 2  // must be power of two
        private const val MAXIMUM_CAPACITY: Int = 1 shl 30 // must be power of two

        // we force the load factor to 1.0 up to the size of two cache lines (which we assume are 64 bytes each)
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * 64 / Long.SIZE_BYTES

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
