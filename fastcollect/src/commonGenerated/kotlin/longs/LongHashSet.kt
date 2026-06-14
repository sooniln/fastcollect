package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.ArrayUtils
import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashSet](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Longs. Can be used in place of
 * the Kotlin standard library [HashSet] implementations to improve performance and memory usage. Has the same API
 * contracts as the standard library [HashSet] unless noted otherwise.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Note that a load factor of 1.0 is accepted, unlike many HashSets - this is interpreted to mean that only 1 slot need
 * ever remain free (i.e. the actual load factor is (capacity - 1)/capacity).
 */
public class LongHashSet @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
) : AbstractMutableLongSet() {

    init {
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(elements: LongCollection): this() { addAll(elements) }
    public constructor(elements: Collection<Long>): this() { addAll(elements) }

    private var keysArr = EMPTY_ARRAY
    private var emptyKey = ZERO

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

    override fun contains(element: Long): Boolean {
        return findSlot(element) >= 0
    }

    override fun add(element: Long): Boolean {
        resizeIfNecessary()

        if (element == emptyKey) changeEmptyKey()

        val keysArr = keysArr
        val emptyKey = emptyKey
        val mask = keysArr.size - 1
        val pow2 = keysArr.size.countTrailingZeroBits()

        var slot = element.slot(mask, pow2)
        var newKey = element
        var newSlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == newKey) {
                return false
            } else if (currKey == emptyKey) {
                keysArr[slot] = newKey
                ++size
                return true
            }

            val currKeySlotDistance = currKey.slotDistance(slot, mask, pow2)
            if (newSlotDistance > currKeySlotDistance) {
                keysArr[slot] = newKey
                newKey = currKey
                newSlotDistance = currKeySlotDistance
            }

            slot = (slot + 1) and mask
            ++newSlotDistance
        }
    }

    override fun remove(element: Long): Boolean {
        val slot = findSlot(element)
        if (slot >= 0) {
            removeSlot(slot)
            return true
        }

        return false
    }

    override fun clear() {
        if (keysArr !== EMPTY_ARRAY) {
            keysArr.fill(emptyKey)
        }
        size = 0
    }

    private fun findSlot(key: Long): Int {
        val keysArr = keysArr
        val emptyKey = emptyKey
        val mask = keysArr.size - 1
        val pow2 = keysArr.size.countTrailingZeroBits()

        var slot = key.slot(mask, pow2)
        var currKey = keysArr[slot]

        var slotDistance = 0
        while (true) {
            // checking whether the current slot distance is higher than our search distance allows us to early exit the
            // search loop - but the cost of checking is non-trivial. as a compromise between GetHit and GetMiss
            // performance we only check once every half cache line.
            var i = 0
            do {
                if (currKey == emptyKey) {
                    return -1
                } else if (currKey == key) {
                    return slot
                }

                slot = (slot + 1) and mask
                currKey = keysArr[slot]
            } while (++i < HALF_CACHE_LINE_SIZE)

            slotDistance += HALF_CACHE_LINE_SIZE
            if (currKey.slotDistance(slot, mask, pow2) < slotDistance) {
                return -1
            }
        }
    }

    private fun removeSlot(slot: Int) {
        val keysArr = keysArr
        val emptyKey = emptyKey
        val mask = keysArr.size - 1
        val pow2 = keysArr.size.countTrailingZeroBits()

        // move all slots left until we hit a zero slot
        var currSlot = slot
        var nextSlot = (currSlot + 1) and mask
        var nextKey = keysArr[nextSlot]
        while (nextKey != emptyKey && nextKey.slotDistance(nextSlot, mask, pow2) > 0) {
            keysArr[currSlot] = nextKey

            currSlot = nextSlot
            nextSlot = (nextSlot + 1) and mask
            nextKey = keysArr[nextSlot]
        }
        keysArr[currSlot] = emptyKey
        --size
    }

    override fun addAll(elements: LongCollection): Boolean {
        val it = if (elements is LongHashSet && elements.size / 2 > size) {
            iterator().also { resetTo(elements) }
        } else {
            elements.iterator()
        }

        ensureCapacity(max(size + (elements.size / 2), elements.size))
        var modified = false
        for (element in it) {
            modified = add(element) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Long>): Boolean {
        if (elements is LongCollection) {
            return addAll(elements)
        } else {
            ensureCapacity(max(size + (elements.size / 2), elements.size))
            var modified = false
            for (element in elements) {
                modified = add(element) or modified
            }
            return modified
        }
    }

    private fun resetTo(elements: LongHashSet) {
        check(!elements.isEmpty())

        keysArr = elements.keysArr.copyOf()
        emptyKey = elements.emptyKey
        size = elements.size
        threshold = elements.threshold
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
        val newMask = newKeysArr.size - 1
        val newPow2 = newKeysArr.size.countTrailingZeroBits()

        for (slot in keysArr.indices) {
            val key = keysArr[slot]
            if (key != emptyKey) addRehashing(newKeysArr, newMask, newPow2, key)
        }

        keysArr = newKeysArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newKeysArr.size * actualLoadFactor).toInt(), newKeysArr.size - 1)
    }

    // we can assume key doesn't exist in array and that we never insert zero
    private fun addRehashing(keysArr: LongArray, mask: Int, pow2: Int, key: Long) {
        val emptyKey = emptyKey

        var slot = key.slot(mask, pow2)
        var newKey = key
        var newSlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == emptyKey) {
                keysArr[slot] = newKey
                return
            }

            val currKeySlotDistance = currKey.slotDistance(slot, mask, pow2)
            if (newSlotDistance > currKeySlotDistance) {
                keysArr[slot] = newKey
                newKey = currKey
                newSlotDistance = currKeySlotDistance
            }

            slot = (slot + 1) and mask
            ++newSlotDistance
        }
    }

    // changes emptyKey to a value not currently in the set, rewriting all empty slots
    private fun changeEmptyKey() {
        val oldEmptyKey = emptyKey

        // TODO: should we always try zero first or is that asking for trouble?
        var candidate = ZERO
        while (candidate == oldEmptyKey || findSlot(candidate) != -1) {
            candidate = Random.nextLong()
        }

        val keysArr = keysArr
        for (i in keysArr.indices) {
            if (keysArr[i] == oldEmptyKey) keysArr[i] = candidate
        }
        emptyKey = candidate
    }

    override fun iterator(): MutableLongIterator = Iterator()

    private inner class Iterator : MutableLongIterator() {
        private val keysArr = this@LongHashSet.keysArr
        private val emptyKey = this@LongHashSet.emptyKey
        private val mask = keysArr.size - 1

        private var slotsLeft = size
        private var slot = keysArr.size - 1
        private var previousSlot = -1

        init {
            if (slotsLeft > 0) decrement()
        }

        override fun hasNext(): Boolean {
            return slotsLeft > 0
        }

        override fun nextLong(): Long {
            if (slotsLeft <= 0) throw NoSuchElementException()
            previousSlot = slot
            if (--slotsLeft > 0) decrement()
            return keysArr[previousSlot]
        }

        override fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@LongHashSet.keysArr) throw ConcurrentModificationException()

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

    private fun Long.slot(mask: Int, pow2: Int): Int {
        return (this.hashCode() * PHI).rotateLeft(pow2) and mask
    }

    private fun Long.slotDistance(slot: Int, mask: Int, pow2: Int): Int {
        return (slot - (this.hashCode() * PHI).rotateLeft(pow2)) and mask
    }

    internal companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Long = 0.toLong()

        private val EMPTY_ARRAY = longArrayOf(ZERO)

        // Knuth multiplicative hash
        private const val PHI: Int = 0x93d765dd.toInt()

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
