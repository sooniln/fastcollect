package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.assert
import io.github.sooniln.fastcollect.doubles.MutableDoubleCollection
import io.github.sooniln.fastcollect.doubles.MutableDoubleIterator
import kotlin.math.max

public class Int2DoubleHashMap(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR,
    /** The default value should be the value that is ideally least likely to occur in the map. */
    override val defaultValue: Double = Double.NaN
) : AbstractMutableInt2DoubleMap() {

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

    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
        if (keysArr.isEmpty()) {
            threshold = capacity
        } else {
            growTo(capacity)
        }
    }

    override fun putValue(key: Int, value: Double): Double {
        resizeIfNecessary()
        return if (isHashing()) putHashing(key, value) else putArray(key, value)
    }

    private fun putHashing(key: Int, value: Double): Double {
        assert(isHashing())

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
                    ++size
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
                        ++size
                        return defaultValue
                    }
                }
            }

            slot = slot.nextSlot(mask)
            ++newKeySlotDistance
        }
    }

    private fun putArray(key: Int, value: Double): Double {
        assert(!isHashing())

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

    override fun removeKey(key: Int): Double {
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

    private fun findSlot(key: Int): Int {
        return if (isHashing()) findSlotHashing(key) else findSlotArray(key)
    }

    private fun findSlotHashing(key: Int): Int {
        assert(isHashing())

        val keysArr = keysArr

        if (key == ZERO) {
            val endSlot = keysArr.endSlot()
            assert(endSlot >= 0)
            return if (keysArr[endSlot] != ZERO) -1 else endSlot
        }

        val mask = keysArr.mask()
        var slot = key.slot(mask)
        while (true) {
            // we could stop looking once the distance < current distance, but this generally worsens performance (extra
            // unpredictable branch which only cuts off a couple iterations).
            val currKey = keysArr[slot]
            when (currKey) {
                key -> return slot
                ZERO -> return -1
            }
            slot = slot.nextSlot(mask)
        }
    }

    private fun findSlotArray(key: Int): Int {
        assert(!isHashing())

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
        assert(isHashing())

        val keysArr = keysArr
        val valuesArr = valuesArr

        val endSlot = keysArr.endSlot()
        if (slot == endSlot) {
            keysArr[endSlot] = NONZERO
            --size
            return
        }

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
        --size
    }

    private fun removeSlotArray(slot: Int) {
        assert(!isHashing())
        assert(slot < size)

        val lastIndex = --size
        if (slot < lastIndex) {
            keysArr[slot] = keysArr[lastIndex]
            valuesArr[slot] = valuesArr[lastIndex]
        }
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
                return if (isDefaultValue(value) && !containsKey(element.key())) false else value == element.value()
            }
            override fun add(element: MutableInt2DoubleMap.MutableEntry): Boolean = throw UnsupportedOperationException()
            override fun remove(element: MutableInt2DoubleMap.MutableEntry): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableIterator<MutableInt2DoubleMap.MutableEntry> = EntryIterator()
            override fun fastIterator(): MutableFastIterator<MutableInt2DoubleMap.MutableEntry> = FastEntryIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override fun containsKey(key: Int): Boolean {
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

    override fun lookup(key: Int): Double {
        return if (isHashing()) lookupHashing(key) else lookupArray(key)
    }

    private fun lookupHashing(key: Int): Double {
        assert(isHashing())

        val keysArr = keysArr
        val valuesArr = valuesArr

        if (key == ZERO) {
            val endSlot = keysArr.endSlot()
            assert(endSlot >= 0)
            return if (keysArr[endSlot] != ZERO) defaultValue else valuesArr[endSlot]
        }

        val mask = keysArr.mask()
        var slot = key.slot(mask)
        while (true) {
            // we could stop looking once the distance < current distance, but this generally worsens performance (extra
            // unpredictable branch which only cuts off a couple iterations).
            val currKey = keysArr[slot]
            when (currKey) {
                key -> return valuesArr[slot]
                ZERO -> return defaultValue
            }
            slot = slot.nextSlot(mask)
        }
    }

    private fun lookupArray(key: Int): Double {
        assert(!isHashing())

        val keysArr = keysArr
        val valuesArr = valuesArr

        // iterate backwards under assumption more recently added values are more likely to be queried
        var slot = size - 1
        while (slot >= 0) {
            if (keysArr[slot] == key) {
                return valuesArr[slot]
            }
            --slot
        }

        return defaultValue
    }

    private fun resizeIfNecessary() {
        if (keysArr.isEmpty()) {
            growTo(threshold)
        } else if (size >= threshold) {
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
            threshold = newLength
            return
        }

        val oldKeys = keysArr
        val oldValues = valuesArr
        val oldSize = size

        keysArr = IntArray(newLength)
        valuesArr = DoubleArray(newLength)
        size = 0

        val endSlot = keysArr.endSlot()
        threshold = (endSlot * loadFactor).toInt()

        if (!oldKeys.isHashing()) {
            keysArr[endSlot] = NONZERO

            var slot = 0
            while (slot < oldSize) {
                putHashing(oldKeys[slot], oldValues[slot])
                ++slot
            }
        } else {
            // TODO: better algorithm?
            val oldEndSlot = oldKeys.endSlot()
            for (slot in 0..<oldEndSlot) {
                val key = oldKeys[slot]
                if (key != ZERO) {
                    putHashing(key, oldValues[slot])
                }
            }

            keysArr[endSlot] = oldKeys[oldEndSlot]
            valuesArr[endSlot] = oldValues[oldEndSlot]
        }

        size = oldSize
    }

    override operator fun iterator(): Iterator<Int2DoubleMap.Entry> = EntryIterator()

    override fun fastIterator(): FastIterator<Int2DoubleMap.Entry> = FastEntryIterator()

    private open inner class SlotIterator {
        val keysArr = this@Int2DoubleHashMap.keysArr
        val valuesArr = this@Int2DoubleHashMap.valuesArr

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
        fun key(): Int = keysArr[previousSlot]
        fun value(): Double = valuesArr[previousSlot]

        protected fun updateValue(newValue: Double) {
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

        override fun setValue(newValue: Double): Double {
            val oldValue = value()
            updateValue(newValue)
            return oldValue
        }

        override fun next(): MutableInt2DoubleMap.MutableEntry {
            nextSlot()
            return this
        }
    }

    private inner class EntryIterator : SlotIterator(), MutableIterator<MutableInt2DoubleMap.MutableEntry> {

        override fun next(): MutableInt2DoubleMap.MutableEntry {
            nextSlot()
            return object : MutableInt2DoubleMap.MutableEntry {
                private val slot = slot()

                override fun key(): Int = keysArr[slot]
                override fun value(): Double = valuesArr[slot]

                override fun setValue(newValue: Double): Double {
                    val oldValue = valuesArr[slot]
                    if (keysArr !== this@Int2DoubleHashMap.keysArr) throw ConcurrentModificationException()
                    valuesArr[slot] = newValue
                    return oldValue
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.isHashing(): Boolean = (size - 1) > HASHIFY_THRESHOLD

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isHashing(): Boolean = keysArr.isHashing()

    // the slot at the end of slot iteration (exclusive), also the slot that stores the zero value
    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.endSlot(): Int = size - 1

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.mask() = size - 2

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mixHash(hashcode: Int): Int {
        val h = hashcode * INT_PHI
        return h xor (h ushr 16)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slot(mask: Int): Int {
        assert(this != ZERO)
        assert(mask == keysArr.mask())
        return mixHash(this.hashCode()) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.nextSlot(mask: Int): Int {
        assert(mask == keysArr.mask())
        return (this + 1) and mask
    }

    private fun Int.slotDistance(slot: Int, mask: Int): Int {
        return (slot - slot(mask)) and mask
    }

    private companion object {

        private val EMPTY_KEY_ARRAY = IntArray(0)
        private val EMPTY_VALUE_ARRAY = DoubleArray(0)

        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Int = 0.toInt()
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val NONZERO: Int = 1.toInt()

        /** 2<sup>32</sup> &middot; &phi;, &phi; = (&#x221A;5 &minus; 1)/2. */
        private const val INT_PHI: Int = -0x61c88647

        private const val DEFAULT_LOAD_FACTOR = .75f
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
