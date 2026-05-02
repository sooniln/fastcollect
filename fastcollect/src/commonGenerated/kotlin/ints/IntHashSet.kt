package io.github.sooniln.fastcollect.ints

import kotlin.math.max

/**
 * A [HashSet](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Ints. Can be used in place of
 * the Kotlin standard library [HashSet] implementations to improve performance and memory usage. Has the same API
 * contracts as the standard library [HashSet] unless noted otherwise.
 *
 * This implementation differs in behavior from common hash sets in that at low capacity numbers it will simply store
 * elements linearly (similarly to how common ArraySet implementations work). This may improve performance slightly at
 * low capacities, or at worst will be only a small performance penalty - however it will substantially reduce memory
 * requirements at low capacities.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 */
public class IntHashSet(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR
) : AbstractMutableIntSet() {

    init {
        require(loadFactor > 0 && loadFactor < 1) { "Load factor must be greater than 0 and smaller than 1" }
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
    }

    public constructor(elements: Collection<Int>): this(elements.size) {
        addAll(elements)
    }

    // when used in hashing mode, the last slot in the array is used to store the zero key. when used in array mode,
    // there is no special handling for zero.
    private var keysArr = EMPTY_ARRAY

    override var size: Int = 0
            private set

    // use threshold to store the initial size before we allocate anything
    private var threshold: Int = if (capacity == 0) DEFAULT_INITIAL_CAPACITY else capacity

    /**
     * Ensures that the set can hold at least given number of elements without any further resizing of the backing
     * array.
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
    public fun trimToSize() {
        val newLength = arraySize(size, loadFactor)
        if (keysArr.size <= newLength) {
            return
        } else if (newLength == 0) {
            keysArr = EMPTY_ARRAY
            threshold = DEFAULT_INITIAL_CAPACITY
            return
        }

        if (!keysArr.isHashing()) {
            keysArr = keysArr.copyOf(newLength)
            threshold = newLength
            return
        }

        val oldKeys = keysArr
        val oldSize = size
        val oldEndSlot = oldKeys.endSlot()

        keysArr = IntArray(newLength)
        size = 0

        if (newLength > HASHIFY_THRESHOLD) {
            val endSlot = keysArr.endSlot()
            threshold = (endSlot * loadFactor).toInt()

            keysArr[endSlot] = oldKeys[oldEndSlot]
        } else {
            threshold = newLength
            if (oldKeys[oldEndSlot] == ZERO) {
                addArray(ZERO)
            }
        }

        for (slot in 0..<oldEndSlot) {
            val key = oldKeys[slot]
            if (key != ZERO) {
                addHashing(key)
            }
        }

        size = oldSize
    }

    override fun add(element: Int): Boolean {
        resizeIfNecessary()
        return if (isHashing()) addHashing(element) else addArray(element)
    }

    private fun addHashing(element: Int): Boolean {
        val keysArr = keysArr

        if (element == ZERO) {
            val endSlot = keysArr.endSlot()
            return if (keysArr[endSlot] != ZERO) {
                keysArr[endSlot] = ZERO
                ++size
                true
            } else {
                false
            }
        }

        val mask = keysArr.mask()
        var slot = element.slot(mask)
        var newKey = element
        var newKeySlotDistance = 0
        while (true) {
            when (val currKey = keysArr[slot]) {
                newKey -> {
                    return false
                }
                ZERO -> {
                    keysArr[slot] = newKey
                    ++size
                    return true
                }
                else -> {
                    val currKeySlotDistance =  currKey.slotDistance(slot, mask)
                    if (newKeySlotDistance > currKeySlotDistance) {
                        keysArr[slot] = newKey
                        newKey = currKey
                        newKeySlotDistance = currKeySlotDistance
                    }
                }
            }

            slot = slot.nextSlot(mask)
            ++newKeySlotDistance
        }
    }

    private fun addArray(element: Int): Boolean {
        var slot = 0
        while (slot < size) {
            if (keysArr[slot] == element) return false
            ++slot
        }

        keysArr[slot] = element
        ++size
        return true
    }

    override fun remove(element: Int): Boolean {
        val slot = findSlot(element)
        if (slot >= 0) {
            removeSlot(slot)
            return true
        }

        return false
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

    private fun findSlotArray(key: Int): Int {
        val keysArr = keysArr

        // iterate backwards under assumption more recently added keys are more likely to be queried
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
        while (nextKey != ZERO && nextKey.slotDistance(nextSlot, mask) > 0) {
            keysArr[currSlot] = nextKey

            currSlot = nextSlot
            nextSlot = nextSlot.nextSlot(mask)
            nextKey = keysArr[nextSlot]
        }
        keysArr[currSlot] = ZERO
        --size
    }

    private fun removeSlotArray(slot: Int) {
        val lastIndex = --size
        if (slot < lastIndex) {
            keysArr[slot] = keysArr[lastIndex]
        }
    }

    override fun contains(element: Int): Boolean {
        return findSlot(element) >= 0
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
            threshold = newLength
            return
        }

        val oldKeys = keysArr
        val oldSize = size

        keysArr = IntArray(newLength)
        size = 0

        val endSlot = keysArr.endSlot()
        threshold = (endSlot * loadFactor).toInt()

        if (!oldKeys.isHashing()) {
            keysArr[endSlot] = NONZERO

            var slot = 0
            while (slot < oldSize) {
                addHashing(oldKeys[slot])
                ++slot
            }
        } else {
            // TODO: better algorithm?
            val oldEndSlot = oldKeys.endSlot()
            for (slot in 0..<oldEndSlot) {
                val key = oldKeys[slot]
                if (key != ZERO) {
                    addHashing(key)
                }
            }

            keysArr[endSlot] = oldKeys[oldEndSlot]
        }

        size = oldSize
    }

    override fun iterator(): MutableIntIterator = Iterator()

    private inner class Iterator : MutableIntIterator() {
        private val keysArr = this@IntHashSet.keysArr

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

        override fun hasNext(): Boolean {
            return slotsLeft > 0
        }

        override fun nextInt(): Int {
            if (slotsLeft-- <= 0) throw NoSuchElementException()
            previousSlot = slot
            if (slotsLeft > 0) decrement()
            return keysArr[previousSlot]
        }

        override fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@IntHashSet.keysArr) throw ConcurrentModificationException()

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

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.isHashing(): Boolean = (size - 1) > HASHIFY_THRESHOLD

    @Suppress("NOTHING_TO_INLINE")
    private inline fun isHashing(): Boolean = keysArr.isHashing()

    // the slot at the end of slot iteration (exclusive), also the slot that stores the zero value
    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.endSlot(): Int = size - 1

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.mask(): Int = size - 2

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mixHash(hashcode: Int): Int {
        val h = hashcode * INT_PHI
        return h xor (h ushr 16)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slot(mask: Int): Int {
        return mixHash(this.hashCode()) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.nextSlot(mask: Int): Int {
        return (this + 1) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slotDistance(slot: Int, mask: Int): Int {
        return (slot - slot(mask)) and mask
    }

    internal companion object {
        private val EMPTY_ARRAY = IntArray(0)

        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Int = 0.toInt()
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val NONZERO: Int = 1.toInt()

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
