@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.MutableFastIterator

import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleIterator

import kotlin.math.max

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Long to Double
 * relationships. Can be used in place of the Kotlin standard library [HashMap] implementations to improve performance
 * and memory usage. Has the same API contracts as the standard library [HashMap] unless noted otherwise.
 *
 * This implementation differs in behavior from common hash maps in that at low capacity numbers it will simply store
 * elements linearly (similarly to how common ArrayMap implementations work). This may improve performance slightly at
 * low capacities, or at worst will be only a small performance penalty - however it will substantially reduce memory
 * requirements at low capacities.
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
public class Long2DoubleHashMap(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR,

    /** The default value should be the value that is ideally least likely to occur in the map. */
    override val defaultValue: Double = Double.NaN

) : AbstractMutableLong2DoubleMap() {

    init {
        require(loadFactor > 0 && loadFactor < 1) { "Load factor must be greater than 0 and smaller than 1" }
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
    }

    // when used in hashing mode, the last slot in the array is used to store the zero key/value respectively. when used
    // in array mode, there is no special handling for zero.
    private var keysArr = EMPTY_KEY_ARRAY

    private var valuesArr = EMPTY_VALUE_ARRAY


    override var size: Int = 0
        private set

    // use threshold to store the initial size before we allocate anything
    private var threshold: Int = if (capacity == 0) DEFAULT_INITIAL_CAPACITY else capacity

    /**
     * Ensures that the map can hold at least given number of key/value pairs without any further resizing of the
     * backing array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
        if (keysArr.isEmpty()) {
            threshold = capacity
        } else {
            growTo(capacity)
        }
    }

    /**
     * Reduces the size of the backing array to the minimum required to hold the current number of elements.
     */
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    public fun trimToSize() {
        val newLength = arraySize(size, loadFactor)
        if (keysArr.size <= newLength) {
            return
        } else if (newLength == 0) {
            keysArr = EMPTY_KEY_ARRAY

            valuesArr = EMPTY_VALUE_ARRAY

            threshold = DEFAULT_INITIAL_CAPACITY
            return
        }

        if (!keysArr.isHashing()) {
            keysArr = keysArr.copyOf(newLength)
            valuesArr = valuesArr.copyOf(newLength)
            threshold = newLength
            return
        }

        val oldKeys = keysArr
        val oldValues = valuesArr
        val oldSize = size
        val oldEndSlot = oldKeys.endSlot()

        keysArr = LongArray(newLength)

        valuesArr = DoubleArray(newLength)

        size = 0

        if (newLength > HASHIFY_THRESHOLD) {
            val endSlot = keysArr.endSlot()
            threshold = (endSlot * loadFactor).toInt()

            keysArr[endSlot] = oldKeys[oldEndSlot]
            valuesArr[endSlot] = oldValues[oldEndSlot]
        } else {
            threshold = newLength
            if (oldKeys[oldEndSlot] == ZERO) {
                putArray(ZERO, oldValues[oldEndSlot] as Double)
            }
        }

        for (slot in 0..<oldEndSlot) {
            val key = oldKeys[slot]
            if (key != ZERO) {
                putHashing(key, oldValues[slot] as Double)
            }
        }

        size = oldSize
    }

    override fun putValue(key: Long, value: Double): Double {
        resizeIfNecessary()
        return if (isHashing()) putHashing(key, value) else putArray(key, value)
    }

    private fun putHashing(key: Long, value: Double): Double {
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

        val mask = keysArr.mask()
        var slot = key.slot(mask)
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

    private fun putArray(key: Long, value: Double): Double {
        var slot = 0
        while (slot < size) {
            if (keysArr[slot] == key) {
                val oldValue = valuesArr[slot]
                valuesArr[slot] = value
                return oldValue
            }

            ++slot
        }

        keysArr[slot] = key
        valuesArr[slot] = value
        ++size
        return defaultValue
    }

    override fun removeKey(key: Long): Double {
        val slot = findSlot(key)
        if (slot >= 0) {
            val oldValue = valuesArr[slot]
            removeSlot(slot)
            return oldValue
        }

        return defaultValue
    }

    override fun clear() {
        keysArr.fill(ZERO)

        if (keysArr.isHashing()) {
            keysArr[keysArr.endSlot()] = NONZERO
        }
        size = 0
    }

    private fun findSlot(key: Long): Int {
        return if (isHashing()) findSlotHashing(key) else findSlotArray(key)
    }

    private fun findSlotHashing(key: Long): Int {
        val keysArr = keysArr

        if (key == ZERO) {
            val endSlot = keysArr.endSlot()
            return if (keysArr[endSlot] != ZERO) -1 else endSlot
        }

        val mask = keysArr.mask()
        var slot = key.slot(mask)
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == key) {
                return slot
            } else if (currKey == ZERO) {
                return -1
            }
            // do not bother to compare slot distances to break out of the loop - the additional cost is usually more
            // expensive than a couple extra iterations.
            slot = slot.nextSlot(mask)
        }
    }

    private fun findSlotArray(key: Long): Int {
        val keysArr = keysArr

        // iterate backwards under assumption more recently added values are more likely to be queried
        var slot = size - 1
        while (slot >= 0) {
            if (keysArr[slot] == key) {
                return slot
            }
            --slot
        }

        return -1
    }

    private fun removeSlot(slot: Int) {
        if (isHashing()) removeSlotHashing(slot) else removeSlotArray(slot)
    }

    private fun removeSlotHashing(slot: Int) {
        val keysArr = keysArr
        val valuesArr = valuesArr

        val endSlot = keysArr.endSlot()
        if (slot == endSlot) {
            keysArr[endSlot] = NONZERO

            --size
            return
        }

        val mask = keysArr.mask()

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

    private fun removeSlotArray(slot: Int) {
        val lastIndex = --size
        if (slot < lastIndex) {
            keysArr[slot] = keysArr[lastIndex]
            valuesArr[slot] = valuesArr[lastIndex]

        }
    }

    override fun putAll(from: Map<out Long, Double>) {
        ensureCapacity(from.size)

        if (from is Long2DoubleMap) {
            for (entry in from.fastIterator()) {
                set(entry.key(), entry.value())
            }
        } else {
            for (entry in from) {
                set(entry.key, entry.value)
            }
        }
    }

    override val keys: MutableLongSet by lazy {
        object : MutableLongSet {
            override val size: Int get() = this@Long2DoubleHashMap.size
            override fun contains(element: Long): Boolean = containsKey(element)
            override fun add(element: Long): Boolean = throw UnsupportedOperationException()
            override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableLongIterator = KeyIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override val values: MutableDoubleCollection by lazy {

        object : MutableDoubleCollection {

            override val size: Int get() = this@Long2DoubleHashMap.size
            override fun contains(element: Double): Boolean = containsValue(element)
            override fun add(element: Double): Boolean = throw UnsupportedOperationException()
            override fun remove(element: Double): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableDoubleIterator = ValueIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override val primitiveEntries: MutableEntrySet<MutableLong2DoubleMap.MutableEntry> by lazy {
        object : AbstractMutableSet<MutableLong2DoubleMap.MutableEntry>(), MutableEntrySet<MutableLong2DoubleMap.MutableEntry> {
            override val size: Int get() = this@Long2DoubleHashMap.size
            override fun contains(element: MutableLong2DoubleMap.MutableEntry): Boolean {
                val value = lookup(element.key())
                return if (isDefaultValue(value) && !containsKey(element.key())) false else value == element.value()
            }
            override fun add(element: MutableLong2DoubleMap.MutableEntry): Boolean = throw UnsupportedOperationException()
            override fun remove(element: MutableLong2DoubleMap.MutableEntry): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableIterator<MutableLong2DoubleMap.MutableEntry> = EntryIterator()
            override fun fastIterator(): MutableFastIterator<MutableLong2DoubleMap.MutableEntry> = FastEntryIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override fun containsKey(key: Long): Boolean {
        return findSlot(key) >= 0
    }

    override fun containsValue(value: Double): Boolean {
        return if (isHashing()) containsValueHashing(value) else containsValueArray(value)
    }

    private fun containsValueHashing(value: Double): Boolean {
        val endSlot = keysArr.endSlot()
        if (valuesArr[endSlot] == value && keysArr[endSlot] == ZERO) return true

        var slot = 0
        while (slot < endSlot) {
            if (valuesArr[slot] == value && keysArr[slot] != ZERO) return true
            ++slot
        }
        return false
    }

    private fun containsValueArray(value: Double): Boolean {
        var slot = size - 1
        while (slot >= 0) {
            if (valuesArr[slot] == value) return true
            --slot
        }
        return false
    }

    override fun lookup(key: Long): Double {
        val valuesArr = valuesArr
        val slot = findSlot(key)
        return if (slot >= 0) valuesArr[slot] else defaultValue
    }

    private fun resizeIfNecessary() {
        if (keysArr.isEmpty()) {
            growTo(threshold)
        } else if (size >= threshold) {
            growTo(threshold shl 1)
        }
    }

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    private fun growTo(capacity: Int) {
        val newLength = arraySize(capacity, loadFactor)
        if (keysArr.size >= newLength) {
            return
        }

        if (newLength <= HASHIFY_THRESHOLD) {
            keysArr = keysArr.copyOf(newLength)
            valuesArr = valuesArr.copyOf(newLength)
            threshold = newLength
            return
        }

        val oldKeys = keysArr
        val oldValues = valuesArr
        val oldSize = size

        keysArr = LongArray(newLength)

        valuesArr = DoubleArray(newLength)

        size = 0

        val endSlot = keysArr.endSlot()
        threshold = (endSlot * loadFactor).toInt()

        if (!oldKeys.isHashing()) {
            keysArr[endSlot] = NONZERO

            var slot = 0
            while (slot < oldSize) {
                putHashing(oldKeys[slot], oldValues[slot] as Double)
                ++slot
            }
        } else {
            // TODO: better algorithm?
            val oldEndSlot = oldKeys.endSlot()
            for (slot in 0..<oldEndSlot) {
                val key = oldKeys[slot]
                if (key != ZERO) {
                    putHashing(key, oldValues[slot] as Double)
                }
            }

            keysArr[endSlot] = oldKeys[oldEndSlot]
            valuesArr[endSlot] = oldValues[oldEndSlot]
        }

        size = oldSize
    }

    override operator fun iterator(): Iterator<Long2DoubleMap.Entry> = EntryIterator()

    override fun fastIterator(): FastIterator<Long2DoubleMap.Entry> = FastEntryIterator()

    private open inner class SlotIterator {
        val keysArr = this@Long2DoubleHashMap.keysArr
        val valuesArr = this@Long2DoubleHashMap.valuesArr

        private var slotsLeft = size
        private val mask = keysArr.mask()

        private var slot: Int
        private var previousSlot = -1

        init {
            if (keysArr.isHashing()) {
                slot = keysArr.endSlot()
                if (keysArr[slot] != ZERO && slotsLeft > 0) {
                    decrement()
                }
            } else {
                slot = size - 1
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
        fun value(): Double = valuesArr[previousSlot] as Double

        protected fun updateValue(newValue: Double) {
            check(previousSlot != -1)
            if (keysArr !== this@Long2DoubleHashMap.keysArr) throw ConcurrentModificationException()
            valuesArr[previousSlot] = newValue
        }

        fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@Long2DoubleHashMap.keysArr) throw ConcurrentModificationException()

            removeSlot(previousSlot)
            previousSlot = -1
        }

        private fun decrement() {
            if (keysArr.isHashing()) {
                // deliberate local variable so JIT can optimize better
                var s = (slot - 1) and mask
                while (keysArr[s] == ZERO) {
                    s = (s - 1) and mask
                }
                slot = s
            } else {
                --slot
            }
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


    private inner class ValueIterator : MutableDoubleIterator() {

        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()


        override fun nextDouble(): Double {

            it.nextSlot()
            return it.value()
        }

        override fun remove() = it.remove()
    }

    private inner class FastEntryIterator: SlotIterator(), MutableFastIterator<MutableLong2DoubleMap.MutableEntry>, MutableLong2DoubleMap.MutableEntry {

        override fun setValue(newValue: Double): Double {
            val oldValue = value()
            updateValue(newValue)
            return oldValue
        }

        override fun next(): MutableLong2DoubleMap.MutableEntry {
            nextSlot()
            return this
        }
    }

    private inner class EntryIterator : SlotIterator(), MutableIterator<MutableLong2DoubleMap.MutableEntry> {

        override fun next(): MutableLong2DoubleMap.MutableEntry {
            nextSlot()
            return object : MutableLong2DoubleMap.MutableEntry {
                private val slot = slot()

                override fun key(): Long = keysArr[slot]
                @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                override fun value(): Double = valuesArr[slot] as Double

                override fun setValue(newValue: Double): Double {
                    val oldValue = value()
                    if (keysArr !== this@Long2DoubleHashMap.keysArr) throw ConcurrentModificationException()
                    valuesArr[slot] = newValue
                    return oldValue
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun LongArray.isHashing(): Boolean = (size - 1) > HASHIFY_THRESHOLD

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isHashing(): Boolean = keysArr.isHashing()

    // the slot at the end of slot iteration (exclusive), also the slot that stores the zero value
    @Suppress("NOTHING_TO_INLINE")
    private inline fun LongArray.endSlot(): Int = size - 1

    @Suppress("NOTHING_TO_INLINE")
    private inline fun LongArray.mask() = size - 2

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mixHash(hashcode: Int): Int {
        val h = hashcode * INT_PHI
        return h xor (h ushr 16)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Long.slot(mask: Int): Int {
        return mixHash(this.hashCode()) and mask
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

        private val EMPTY_KEY_ARRAY = LongArray(0)

        private val EMPTY_VALUE_ARRAY = DoubleArray(0)


        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Long = 0.toLong()
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val NONZERO: Long = 1.toLong()

        /** 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. */
        private const val INT_PHI: Int = -0x61c88647

        private const val DEFAULT_LOAD_FACTOR = .85f
        private const val DEFAULT_INITIAL_CAPACITY = 1 shl 2  // must be power of two
        private const val MAXIMUM_CAPACITY: Int = 1 shl 30 // must be power of two
        private const val HASHIFY_THRESHOLD: Int = 1 shl 5 // must be power of two
        private const val MIN_HASH_CAPACITY = HASHIFY_THRESHOLD shr 1 // must be power of two

        private fun arraySize(capacity: Int, loadFactor: Float): Int {
            check(capacity >= 0)
            return if (capacity <= HASHIFY_THRESHOLD) {
                capacity
            } else {
                // add extra slot to hold zero value at the end
                max(minPowerOfTwo((capacity / loadFactor).toInt()), MIN_HASH_CAPACITY) + 1
            }
        }

        private fun minPowerOfTwo(cap: Int): Int {
            val n = -1 ushr (cap - 1).countLeadingZeroBits()
            return if (n < 0) 1 else if (n >= MAXIMUM_CAPACITY) MAXIMUM_CAPACITY else n + 1
        }
    }
}
