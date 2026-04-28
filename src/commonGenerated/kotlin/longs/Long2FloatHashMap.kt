package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.MutableEntrySet
import io.github.sooniln.fastcollect.MutableFastIterator
import io.github.sooniln.fastcollect.floats.MutableFloatCollection
import io.github.sooniln.fastcollect.floats.MutableFloatIterator
import kotlin.math.max

public class Long2FloatHashMap(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR,
    /** The default value should be the value that is ideally least likely to occur in the map. */
    override val defaultValue: Float = Float.NaN
) : MutableLong2FloatMap {

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

    override fun putValue(key: Long, value: Float): Float {
        resizeIfNecessary()
        return if (isHashing()) putInternalHashing(key, value) else putInternalArray(key, value)
    }

    private fun putInternalHashing(key: Long, value: Float): Float {
        // assert(isHashing())

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

    private fun putInternalArray(key: Long, value: Float): Float {
        // assert(!isHashing())

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
        // assert(isHashing())

        if (key == ZERO) {
            val endSlot = keysArr.endSlot()
            // assert(endSlot >= 0)
            return if (keysArr[endSlot] != ZERO) -1 else endSlot
        }

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

    private fun findSlotArray(key: Long): Int {
        // assert(!isHashing())

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
        // assert(isHashing())

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
        // assert(!isHashing())
        // assert(slot < arrayUsage)

        val lastIndex = --size
        if (slot < lastIndex) {
            keysArr[slot] = keysArr[lastIndex]
            valuesArr[slot] = valuesArr[lastIndex]
        }
    }

    override fun putAll(from: Map<out Long, Float>) {
        ensureCapacity(from.size)

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

    override val keys: MutableLongSet by lazy {
        object : MutableLongSet {
            override val size: Int get() = this@Long2FloatHashMap.size
            override fun contains(element: Long): Boolean = containsKey(element)
            override fun add(element: Long): Boolean = throw UnsupportedOperationException()
            override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableLongIterator = KeyIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override val values: MutableFloatCollection by lazy {
        object : MutableFloatCollection {
            override val size: Int get() = this@Long2FloatHashMap.size
            override fun contains(element: Float): Boolean = containsValue(element)
            override fun add(element: Float): Boolean = throw UnsupportedOperationException()
            override fun remove(element: Float): Boolean = throw UnsupportedOperationException()
            override fun iterator(): MutableFloatIterator = ValueIterator()
            override fun clear() = throw UnsupportedOperationException()
        }
    }

    override val primitiveEntries: MutableEntrySet<MutableLong2FloatMap.MutableEntry> by lazy {
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
    }

    override fun containsKey(key: Long): Boolean {
        return findSlot(key) >= 0
    }

    override fun containsValue(value: Float): Boolean {
        return if (isHashing()) containsValueHashing(value) else containsValueArray(value)
    }

    private fun containsValueHashing(value: Float): Boolean {
        val endSlot = keysArr.endSlot()
        if (valuesArr[endSlot] == value && keysArr[endSlot] == ZERO) return true

        var slot = 0
        while (slot < endSlot) {
            if (valuesArr[slot] == value && keysArr[slot] != ZERO) return true
            ++slot
        }
        return false
    }

    private fun containsValueArray(value: Float): Boolean {
        var slot = size - 1
        while (slot >= 0) {
            if (valuesArr[slot] == value) return true
            --slot
        }
        return false
    }

    override fun lookup(key: Long): Float {
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
        valuesArr = FloatArray(newLength)
        size = 0

        val endSlot = keysArr.endSlot()
        threshold = (endSlot * loadFactor).toInt()

        if (!oldKeys.isHashing()) {
            keysArr[endSlot] = NONZERO

            var slot = 0
            while (slot < oldSize) {
                putInternalHashing(oldKeys[slot], oldValues[slot])
                ++slot
            }
        } else {
            val oldEndSlot = oldKeys.endSlot()
            for (slot in 0..<oldEndSlot) {
                val key = oldKeys[slot]
                if (key != ZERO) {
                    putInternalHashing(key, oldValues[slot])
                }
            }

            keysArr[endSlot] = oldKeys[oldEndSlot]
            valuesArr[endSlot] = oldValues[oldEndSlot]
        }

        size = oldSize
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
        return Iterable<Long2FloatMap.Entry> { FastEntryIterator() }.joinToString(", ", "{", "}") { "${it.key()}=${it.value()}" }
    }

    private open inner class SlotIterator() {
        private val keysArr = this@Long2FloatHashMap.keysArr
        private val valuesArr = this@Long2FloatHashMap.valuesArr

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

        fun slot(): Int = slot
        fun key(): Long = keysArr[previousSlot]
        fun value(): Float = valuesArr[previousSlot]

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
                private val _key = this@EntryIterator.key()
                private var _value = this@EntryIterator.value()

                override fun key(): Long = _key
                override fun value(): Float = _value

                override fun setValue(newValue: Float): Float {
                    val oldValue = _value
                    // TODO: what the fuck is going on with nullability here
                    _value = merge(_key, newValue) { oldValue, value ->
                        if (oldValue != _value) throw ConcurrentModificationException()
                        return@merge value
                    }!!
                    return oldValue
                }
            }
        }
    }

    override operator fun iterator(): Iterator<Long2FloatMap.Entry> = EntryIterator()

    override fun fastIterator(): FastIterator<Long2FloatMap.Entry> = FastEntryIterator()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.isHashingLength(): Boolean = (this - 1) > HASHIFY_THRESHOLD

    @Suppress("NOTHING_TO_INLINE")
    private inline fun LongArray.isHashing(): Boolean = size.isHashingLength()

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
        // assert(this != 0)
        // assert(mask == keysArr.mask())
        return mixHash(this.hashCode()) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.nextSlot(mask: Int): Int {
        // assert(mask == keysArr.mask())
        return (this + 1) and mask
    }

    private fun Long.slotDistance(slot: Int, mask: Int): Int {
        return (slot - slot(mask)) and mask
    }

    private companion object {

        private val EMPTY_KEY_ARRAY = LongArray(0)
        private val EMPTY_VALUE_ARRAY = FloatArray(0)

        // the value of a field in an uninitialized primitive array
        private const val ZERO: Long = 0.toLong()
        private const val NONZERO: Long = 1.toLong()

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
