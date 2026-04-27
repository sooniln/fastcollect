package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleIterator
import kotlin.math.max

public class Int2DoubleHashMap(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR,
    override val defaultValue: Double = Double.NaN
) : MutableInt2DoubleMap {

    init {
        require(loadFactor > 0 && loadFactor < 1) { "Load factor must be greater than 0 and smaller than 1" }
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
    }

    private var keysArr = EMPTY_KEY_ARRAY
    private var valuesArr = EMPTY_VALUE_ARRAY

    private var arrayUsage = 0
    private var zeroValue = 0.toDouble()

    // use threshold to store the initial size before we allocate anything, and since threshold cannot be negative, we
    // also use the highest bit to store whether the map contains zero or not
    private var thresholdAndContainsZero = if (capacity == 0) DEFAULT_INITIAL_CAPACITY else capacity

    private var threshold: Int
        inline get() = thresholdAndContainsZero and ARRAY_USAGE_MASK
        inline set(value) {
            thresholdAndContainsZero = value or (thresholdAndContainsZero and ARRAY_USAGE_MASK.inv())
        }

    private var containsZero: Boolean
        inline get() = thresholdAndContainsZero and ARRAY_USAGE_MASK.inv() != 0
        inline set(value) {
            thresholdAndContainsZero = if (value) {
                thresholdAndContainsZero or ARRAY_USAGE_MASK.inv()
            } else {
                thresholdAndContainsZero and ARRAY_USAGE_MASK
            }
        }

    override val size: Int get() = if (containsZero) arrayUsage + 1 else arrayUsage

    override fun isEmpty(): Boolean {
        return size == 0
    }

    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
        if (keysArr.isEmpty()) {
            threshold = capacity
        } else {
            growTo(capacity)
        }
    }

    override fun putValue(key: Int, value: Double): Double {
        if (key == ZERO) {
            if (!containsZero) {
                containsZero = true
                zeroValue = value
                return defaultValue
            } else {
                val oldValue = zeroValue
                zeroValue = value
                return oldValue
            }
        }

        resizeIfNecessary()

        return if (isHashing()) putInternalHashing(key, value) else putInternalArray(key, value)
    }

    private fun putInternalHashing(key: Int, value: Double): Double {
        // assert(isHashing())
        // assert(key != 0)

        val mask = keysArr.mask()

        var slot = key.slot(mask)
        var newKeySlotDistance = 0
        while (true) {
            var currKey = keysArr[slot]
            when (currKey) {
                key -> {
                    val oldValue = valuesArr[slot]
                    valuesArr[slot] = value
                    return oldValue
                }
                ZERO -> {
                    keysArr[slot] = key
                    valuesArr[slot] = value
                    ++arrayUsage
                    return defaultValue
                }
                else -> {
                    if (newKeySlotDistance > currKey.slotDistance(slot, mask)) {
                        var currValue = valuesArr[slot]
                        var newKey = key
                        var newValue = value
                        // move all slots right until we hit a zero slot. max slot distance is generally not high
                        // enough for System.arrayCopy() to outperform the manual loop here, especially with the
                        // additional complexity needed for System.arrayCopy().
                        do {
                            keysArr[slot] = newKey
                            valuesArr[slot] = newValue
                            newKey = currKey
                            newValue = currValue

                            slot = slot.nextSlot(mask)
                            currKey = keysArr[slot]
                            currValue = valuesArr[slot]
                        } while (currKey != ZERO)

                        keysArr[slot] = newKey
                        valuesArr[slot] = newValue
                        ++arrayUsage
                        return defaultValue
                    }
                }
            }

            slot = slot.nextSlot(mask)
            ++newKeySlotDistance
        }
    }

    private fun putInternalArray(key: Int, value: Double): Double {
        // assert(!isHashing())
        // assert(key != 0)

        var slot = 0
        while (slot < arrayUsage) {
            if (keysArr[slot] == key) {
                val oldValue = valuesArr[slot]
                valuesArr[slot] = value
                return oldValue
            }

            ++slot
        }

        keysArr[slot] = key
        valuesArr[slot] = value
        ++arrayUsage
        return defaultValue
    }

    override fun removeKey(key: Int): Double {
        if (key == ZERO) {
            if (containsZero) {
                containsZero = false
                return zeroValue
            }
        } else {
            val slot = findSlot(key)
            if (slot >= 0) {
                val oldValue = valuesArr[slot]
                removeSlot(slot)
                return oldValue
            }
        }

        return defaultValue
    }

    override fun clear() {
        keysArr.fill(ZERO)
        containsZero = false
        arrayUsage = 0
    }

    private fun findSlot(key: Int): Int {
        return if (isHashing()) findSlotHashing(key) else findSlotArray(key)
    }

    private fun findSlotHashing(key: Int): Int {
        // assert(isHashing())
        // assert(key != 0)

        val mask = keysArr.mask()

        var slot = key.slot(mask)
        var currKey = keysArr[slot]
        while (true) {
            // we could stop looking once the distance < current distance, but this generally worsens performance (extra
            // unpredictable branch which only cuts off a couple iterations).
            when (currKey) {
                ZERO -> return -1
                key -> return slot
            }

            slot = slot.nextSlot(mask)
            currKey = keysArr[slot]
        }
    }

    private fun findSlotArray(key: Int): Int {
        // assert(!isHashing())
        // assert(key != 0)

        // iterate backwards under assumption more recently added values are more likely to be queried
        var slot = arrayUsage - 1
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
        // assert(isHashing())

        val mask = keysArr.mask()

        // move all slots left until we hit a zero slot. max slot distance is generally not high enough for
        // System.arrayCopy() to outperform the manual loop here, especially with the additional complexity needed
        // for System.arrayCopy().
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
        --arrayUsage
    }

    private fun removeSlotArray(slot: Int) {
        // assert(!isHashing())
        // assert(keysArr[slot] != 0)
        // assert(slot < arrayUsage)

        val lastIndex = arrayUsage - 1
        if (slot < lastIndex) {
            keysArr[slot] = keysArr[lastIndex]
            valuesArr[slot] = valuesArr[lastIndex]
        }
        --arrayUsage
    }

    override fun putAll(from: Map<out Int, Double>) {
        ensureCapacity(from.size)

        if (from is Int2DoubleMap) {
            for (entry in from.fastIterator()) {
                set(entry.key(), entry.value())
            }
        } else {
            for (entry in from) {
                set(entry.key, entry.value)
            }
        }
    }

    override val keys: MutableIntSet by lazy {
        object : MutableIntSet {
            override val size: Int get() = this@Int2DoubleHashMap.size
            override fun contains(element: Int): Boolean = containsKey(element)
            override fun add(element: Int): Boolean = throw UnsupportedOperationException()
            override fun remove(element: Int): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableIntIterator = KeyIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override val values: MutableDoubleCollection by lazy {
        object : MutableDoubleCollection {
            override val size: Int get() = this@Int2DoubleHashMap.size
            override fun contains(element: Double): Boolean = containsValue(element)
            override fun add(element: Double): Boolean = throw UnsupportedOperationException()
            override fun remove(element: Double): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableDoubleIterator = ValueIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override val primitiveEntries: MutableEntrySet<MutableInt2DoubleMap.MutableEntry> by lazy {
        object : AbstractMutableSet<MutableInt2DoubleMap.MutableEntry>(), MutableEntrySet<MutableInt2DoubleMap.MutableEntry> {
            override val size: Int get() = this@Int2DoubleHashMap.size
            override fun contains(element: MutableInt2DoubleMap.MutableEntry): Boolean {
                val value = lookup(element.key())
                return if (value == defaultValue && !containsKey(element.key())) false else value == element.value()
            }
            override fun add(element: MutableInt2DoubleMap.MutableEntry): Boolean = throw UnsupportedOperationException()
            override fun remove(element: MutableInt2DoubleMap.MutableEntry): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableIterator<MutableInt2DoubleMap.MutableEntry> = EntryIterator()
            override fun fastIterator(): MutableFastIterator<MutableInt2DoubleMap.MutableEntry> = FastEntryIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override fun containsKey(key: Int): Boolean {
        return if (key == ZERO) {
            containsZero
        } else {
            findSlot(key) >= 0
        }
    }

    override fun containsValue(value: Double): Boolean {
        if (containsZero && zeroValue == value) return true

        if (isHashing()) {
            var slot = 0
            while (slot < keysArr.size) {
                if (valuesArr[slot] == value && keysArr[slot] != ZERO) return true
                ++slot
            }
            return false
        } else {
            var slot = arrayUsage - 1
            while (slot >= 0) {
                if (valuesArr[slot] == value) return true
                --slot
            }
            return false
        }
    }

    override fun lookup(key: Int): Double {
        if (key == ZERO) {
            return if (containsZero) {
                zeroValue
            } else {
                defaultValue
            }
        } else {
            val slot = findSlot(key)
            return if (slot >= 0) {
                valuesArr[slot]
            } else {
                defaultValue
            }
        }
    }

    private fun resizeIfNecessary() {
        if (keysArr.isEmpty()) {
            // assert(threshold > 0)
            growTo(threshold)
        } else if (arrayUsage >= threshold) {
            growTo(threshold shl 1)
        }
    }

    private fun growTo(capacity: Int) {
        val newLength = arraySize(capacity, loadFactor)
        if (keysArr.size >= newLength) {
            return
        }

        if (newLength <= HASHIFY_THRESHOLD) {
            keysArr = keysArr.copyOf(newLength)
            valuesArr = valuesArr.copyOf(newLength)
            threshold = keysArr.size
            return
        }

        val oldKeys = keysArr
        val oldValues = valuesArr
        val oldArrayUsage = arrayUsage

        keysArr = IntArray(newLength)
        valuesArr = DoubleArray(newLength)
        arrayUsage = 0
        threshold = (keysArr.size * loadFactor).toInt()

        if (oldValues.size <= HASHIFY_THRESHOLD) {
            var slot = 0
            while (slot < oldArrayUsage) {
                putInternalHashing(oldKeys[slot], oldValues[slot])
                ++slot
            }
        } else {
            // TODO: better algorithm?
            for ((slot, key) in oldKeys.withIndex()) {
                if (key != ZERO) {
                    putInternalHashing(key, oldValues[slot])
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Map<*, *>) {
            if (other.size != size) return false

            for (entry in FastEntryIterator()) {
                if (other[entry.key()] != entry.value()) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        for (entry in FastEntryIterator()) {
            result += entry.key().hashCode() xor entry.value().hashCode()
        }
        return result
    }

    // TODO: should be in abstract class?
    override fun toString(): String {
        return Iterable<Entry> { FastEntryIterator() }.joinToString(", ", "{", "}") { "${it.key()}=${it.value()}" }
    }

    private inner class KeyIterator : MutableIntIterator() {
        private val keysArr = this@Int2DoubleHashMap.keysArr
        private val arrayUsage = this@Int2DoubleHashMap.arrayUsage

        private var entriesLeft = size
        private var slot = numSlots()
        private var previousSlot = -1
        private var nextKey = ZERO

        init {
            if (entriesLeft > 0 && !containsZero) decrement()
        }

        private fun numSlots() = if (isHashing()) keysArr.size else arrayUsage

        override fun hasNext(): Boolean {
            return entriesLeft > 0
        }

        override fun nextInt(): Int {
            if (entriesLeft-- <= 0) throw NoSuchElementException()
            val k = nextKey
            decrement()
            return k
        }

        override fun remove() {
            check(previousSlot != -1)
            if (previousSlot < numSlots()) {
                removeSlot(previousSlot)
            } else {
                // assert(previousSlot == numSlots() && containsZero)
                containsZero = false
            }
            previousSlot = -1
        }

        private fun decrement() {
            previousSlot = slot
            if (entriesLeft <= 0) return

            do {
                if (slot > 0) {
                    // simple subtraction is a lot faster if we can get away with it (ie 99% of the time)
                    --slot
                } else {
                    slot = (slot - 1) and keysArr.mask()
                }
            } while (keysArr[slot] == ZERO)
            nextKey = keysArr[slot]
        }
    }

    private inner class ValueIterator : MutableDoubleIterator() {
        private val keysArr = this@Int2DoubleHashMap.keysArr
        private val valuesArr = this@Int2DoubleHashMap.valuesArr
        private val arrayUsage = this@Int2DoubleHashMap.arrayUsage

        private var entriesLeft = size
        private var slot = numSlots()
        private var previousSlot = -1
        private var nextValue = zeroValue

        init {
            if (entriesLeft > 0 && !containsZero) decrement()
        }

        private fun numSlots() = if (isHashing()) keysArr.size else arrayUsage

        override fun hasNext(): Boolean {
            return entriesLeft > 0
        }

        override fun nextDouble(): Double {
            if (entriesLeft-- <= 0) throw NoSuchElementException()
            val v = nextValue
            decrement()
            return v
        }

        override fun remove() {
            check(previousSlot != -1)
            if (previousSlot < numSlots()) {
                removeSlot(previousSlot)
            } else {
                // assert(previousSlot == numSlots() && containsZero)
                containsZero = false
            }
            previousSlot = -1
        }

        private fun decrement() {
            previousSlot = slot
            if (entriesLeft <= 0) return

            do {
                if (slot > 0) {
                    // simple subtraction is a lot faster if we can get away with it (ie 99% of the time)
                    --slot
                } else {
                    slot = (slot - 1) and keysArr.mask()
                }
            } while (keysArr[slot] == ZERO)
            nextValue = valuesArr[slot]
        }
    }

    private inner class FastEntryIterator: MutableFastIterator<Entry> {
        private val keysArr = this@Int2DoubleHashMap.keysArr
        private val valuesArr = this@Int2DoubleHashMap.valuesArr
        private val arrayUsage = this@Int2DoubleHashMap.arrayUsage

        private var entriesLeft = size
        private var slot = numSlots()
        private var previousSlot = -1

        private var nextKey = ZERO
        private var nextValue = zeroValue
        private val entry = Entry(nextKey, nextValue)

        init {
            if (entriesLeft > 0 && !containsZero) decrement()
        }

        private fun numSlots() = if (isHashing()) keysArr.size else arrayUsage

        override fun hasNext(): Boolean {
            return entriesLeft > 0
        }

        override fun next(): Entry {
            if (entriesLeft-- <= 0) throw NoSuchElementException()
            entry._key = nextKey
            entry._value = nextValue
            decrement()
            return entry
        }

        override fun remove() {
            check(previousSlot != -1)
            if (previousSlot < numSlots()) {
                removeSlot(previousSlot)
            } else {
                // assert(previousSlot == numSlots() && containsZero)
                containsZero = false
            }
            previousSlot = -1
        }

        private fun decrement() {
            previousSlot = slot
            if (entriesLeft <= 0) return

            do {
                if (slot > 0) {
                    // simple subtraction is a lot faster if we can get away with it (ie 99% of the time)
                    --slot
                } else {
                    slot = (slot - 1) and keysArr.mask()
                }
            } while (keysArr[slot] == ZERO)
            nextKey = keysArr[slot]
            nextValue = valuesArr[slot]
        }
    }

    private inner class EntryIterator : MutableIterator<Entry> {
        private val it = FastEntryIterator()

        override fun hasNext(): Boolean = it.hasNext()
        override fun next(): Entry = Entry(it.next())
        override fun remove() = it.remove()
    }

    private inner class Entry(var _key: Int, var _value: Double) : MutableInt2DoubleMap.MutableEntry {
        constructor(entry: Entry) : this(entry._key, entry._value)

        override fun key(): Int = _key
        override fun value(): Double = _value

        override fun setValue(newValue: Double): Double {
            val oldValue = _value
            // TODO: what the fuck is going on with nullability here
            _value = merge(_key, newValue) { oldValue, value ->
                if (oldValue != _value) throw ConcurrentModificationException()
                return@merge value
            }!!
            return oldValue
        }
    }

    override operator fun iterator(): Iterator<Int2DoubleMap.Entry> = EntryIterator()

    override fun fastIterator(): FastIterator<Int2DoubleMap.Entry> = FastEntryIterator()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isHashing(): Boolean = keysArr.size > HASHIFY_THRESHOLD

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mixHash(element: Int): Int {
        val h = element * INT_PHI
        return h xor (h ushr 16)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.mask() = size - 1

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slot(mask: Int): Int {
        // assert(this != 0)
        // assert(mask == keysArr.mask())
        return mixHash(this.hashCode()) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.nextSlot(mask: Int): Int {
        // assert(mask == keysArr.mask())
        return (this + 1) and mask
    }

    private fun Int.slotDistance(slot: Int, mask: Int): Int {
        return (slot - slot(mask)) and mask
    }

    private companion object {

        private val EMPTY_KEY_ARRAY = IntArray(0)
        private val EMPTY_VALUE_ARRAY = DoubleArray(0)

        // the value of a field in an uninitialized primitive array
        private const val ZERO: Int = 0.toInt()

        /** 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. */
        private const val INT_PHI: Int = -0x61c88647

        private const val DEFAULT_LOAD_FACTOR = .75f
        private const val DEFAULT_INITIAL_CAPACITY = 1 shl 2  // must be power of two
        private const val MAXIMUM_CAPACITY: Int = 1 shl 30 // must be power of two
        private const val HASHIFY_THRESHOLD: Int = 1 shl 5 // must be power of two
        private const val MIN_HASH_CAPACITY = 1 shl 4 // must be power of two

        private const val ARRAY_USAGE_MASK = 0x7FFFFFFF

        private fun arraySize(capacity: Int, loadFactor: Float): Int {
            return if (capacity <= HASHIFY_THRESHOLD) {
                capacity
            } else {
                max(minPowerOfTwo((capacity / loadFactor).toInt()), MIN_HASH_CAPACITY)
            }
        }

        private fun minPowerOfTwo(cap: Int): Int {
            val n = -1 ushr (cap - 1).countLeadingZeroBits()
            return if (n < 0) 1 else if (n >= MAXIMUM_CAPACITY) MAXIMUM_CAPACITY else n + 1
        }
    }
}
