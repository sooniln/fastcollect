package io.github.sooniln.fastcollect.ints

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min

/**
 * A [HashSet](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Ints. Can be used in place of
 * the Kotlin standard library [HashSet] implementations to improve performance and memory usage. Has the same API
 * contracts as the standard library [HashSet] unless noted otherwise.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Note that a load factor of 1.0 is accepted, unlike many HashSets - this is interpreted to mean that only 1 slot need
 * ever remain free (i.e. the actual load factor is (capacity - 1)/capacity).
 */
public class IntHashSet @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
) : AbstractMutableIntSet() {

    init {
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(elements: IntCollection): this() { addAll(elements) }

    public constructor(elements: Collection<Int>): this() { addAll(elements) }

    private var keysArr = EMPTY_ARRAY
    private var mask = keysArr.mask()
    private var rotVal = mask.rotVal()

    override var size: Int = 0
        private set

    // use threshold to store the initial size before we allocate anything, after that it's the size at which we rehash
    private var threshold: Int = if (capacity == 0) DEFAULT_INITIAL_CAPACITY else capacity

    /**
     * Ensures that the set can hold at least given number of elements without any further resizing of the backing
     * array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
        if (keysArr === EMPTY_ARRAY) {
            threshold = if (capacity == 0) threshold else capacity
        } else if (capacity > threshold) {
            rehash(capacity)
        }
    }

    /**
     * Reduces the size of the backing array to the minimum required to hold the current number of elements.
     */
    public fun trimToSize() {
        rehash(size)
    }

    override fun add(element: Int): Boolean {
        resizeIfNecessary()

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

        val mask = mask
        val rotVal = rotVal
        var slot = element.slot(mask, rotVal)
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
                    val currKeySlotDistance = currKey.slotDistance(slot, mask, rotVal)
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

    override fun remove(element: Int): Boolean {
        val slot = findSlot(element)
        if (slot >= 0) {
            removeSlot(slot)
            return true
        }

        return false
    }

    override fun clear() {
        if (keysArr !== EMPTY_ARRAY) {
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
        val rotVal = rotVal
        var slot = key.slot(mask, rotVal)
        var slotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == key) {
                return slot
            } else if (currKey == ZERO ||
                    // checking whether the current slot distance is higher than our search distance allows us to early
                    // exit the search loop, but at a non-trivial cost in extra operations. this generally increases
                    // GetHit time and decreases GetMiss time. in order to optimize this further so that we can still
                    // get the benefit of early exiting without paying the full cost, we implement the following: check
                    // for early exit only once per cache line, when we're half way through the cache line. this doesn't
                    // penalize GetHit times much (as we can hopefully find the element before incurring the full cost)
                    // and still substantially reduces GetMiss times.
                    (slotDistance and CACHE_LINE_MASK == HALF_CACHE_LINE_SIZE
                        && currKey.slotDistance(slot, mask, rotVal) < slotDistance)) {
                return -1
            }

            slot = slot.nextSlot(mask)
            ++slotDistance
        }
    }

    private fun removeSlot(slot: Int) {
        val keysArr = keysArr

        val endSlot = keysArr.endSlot()
        if (slot == endSlot) {
            keysArr[endSlot] = NONZERO
            --size
            return
        }

        val mask = mask
        val rotVal = rotVal

        // move all slots left until we hit a zero slot
        var currSlot = slot
        var nextSlot = currSlot.nextSlot(mask)
        var nextKey = keysArr[nextSlot]
        while (nextKey != ZERO && nextKey.slotDistance(nextSlot, mask, rotVal) > 0) {
            keysArr[currSlot] = nextKey

            currSlot = nextSlot
            nextSlot = nextSlot.nextSlot(mask)
            nextKey = keysArr[nextSlot]
        }
        keysArr[currSlot] = ZERO
        --size
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        ensureCapacity(max(size + (elements.size shr 1), elements.size))

        var modified = false
        if (elements is IntCollection) {
            if (isEmpty() && elements is IntHashSet) {
                initializeFrom(elements)
            } else {
                for (element in elements) {
                    modified = add(element) or modified
                }
            }
        } else {
            for (element in elements) {
                modified = add(element) or modified
            }
        }
        return modified
    }

    private fun initializeFrom(from: IntHashSet) {
        check(isEmpty())

        if (from.isEmpty()) return

        keysArr = from.keysArr.copyOf()
        mask = from.mask
        rotVal = from.rotVal
        size = from.size
        threshold = from.threshold
    }

    override fun contains(element: Int): Boolean {
        return findSlot(element) >= 0
    }

    private fun resizeIfNecessary() {
        if (keysArr === EMPTY_ARRAY) {
            rehash(threshold)
        } else if (size >= threshold) {
            rehash(threshold shl 1)
        }
    }

    private fun rehash(capacity: Int) {
        check(capacity >= size)

        if (capacity == 0 && keysArr !== EMPTY_ARRAY) {
            keysArr = EMPTY_ARRAY
            mask = keysArr.mask()
            rotVal = mask.rotVal()
            threshold = DEFAULT_INITIAL_CAPACITY
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else .9f

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = IntArray(newLength)
        val newMask = newKeysArr.mask()
        val newRotVal = newMask.rotVal()
        val newEndSlot = newKeysArr.endSlot()

        val oldEndSlot = keysArr.endSlot()
        for (slot in 0..<oldEndSlot) {
            val key = keysArr[slot]
            if (key != ZERO) addRehashing(newKeysArr, newMask, newRotVal, key)
        }
        newKeysArr[newEndSlot] = keysArr[oldEndSlot]

        keysArr = newKeysArr
        mask = newMask
        rotVal = newRotVal

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newEndSlot * actualLoadFactor).toInt(), newEndSlot - 1)
    }

    // we can assume key doesn't exist in array and that we never insert zero
    private fun addRehashing(keysArr: IntArray, mask: Int, rotVal: Int, key: Int) {
        var slot = key.slot(mask, rotVal)
        var newKey = key
        var newKeySlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == ZERO) {
                keysArr[slot] = newKey
                return
            }

            val currKeySlotDistance = currKey.slotDistance(slot, mask, rotVal)
            if (newKeySlotDistance > currKeySlotDistance) {
                keysArr[slot] = newKey
                newKey = currKey
                newKeySlotDistance = currKeySlotDistance
            }

            slot = slot.nextSlot(mask)
            ++newKeySlotDistance
        }
    }

    override fun iterator(): MutableIntIterator = Iterator()

    private inner class Iterator : MutableIntIterator() {
        private val keysArr = this@IntHashSet.keysArr
        private val mask = this@IntHashSet.mask

        private var slotsLeft = size

        private var slot = keysArr.endSlot()
        private var previousSlot = -1

        init {
            if (keysArr[slot] != ZERO && slotsLeft > 0) {
                decrement()
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
            do {
                slot = (slot - 1) and mask
            } while (keysArr[slot] == ZERO)
        }
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

        private val EMPTY_ARRAY = intArrayOf(ZERO, NONZERO)

        // Knuth multiplicative hash
        private const val PHI: Int = 0x93d765dd.toInt()

        private const val DEFAULT_INITIAL_CAPACITY = 7
        private const val MAXIMUM_CAPACITY: Int = 1 shl 30 // must be power of two

        private const val CACHE_LINE_SIZE = 64 / Int.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2
        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * CACHE_LINE_SIZE
        // mask for # of elements in a single cache line
        private const val CACHE_LINE_MASK: Int = CACHE_LINE_SIZE - 1

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
