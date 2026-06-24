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
    capacity: Int = 0,
) : AbstractMutableLongSet() {

    public constructor(elements: LongCollection): this() { addAll(elements) }
    public constructor(elements: Collection<Long>): this() { addAll(elements) }

    private var keysArr = EMPTY_ARRAY

    private var emptyKey = ZERO

    // threshold + size == capacity (rehash once threshold <= 0, if we haven't allocated yet then threshold.inv() is
    // our initial capacity)
    private var threshold = MIN_INITIAL_CAPACITY.inv()

    override var size: Int = 0
        private set

    init {
        ensureCapacity(capacity)
    }

    /**
     * Ensures that the set can hold at least given number of elements without any further resizing of the backing
     * array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
        if (keysArr === EMPTY_ARRAY) {
            threshold = min(threshold, capacity.inv())
        } else if (capacity > threshold + size) {
            rehash(capacity)
        }
    }

    /**
     * Reduces the size of the backing array to the minimum required to hold the current number of elements.
     */
    public fun trimToSize() {
        rehash(size)
    }

    override fun contains(element: Long): Boolean = findSlot(element, { true }, { false })

    override fun add(element: Long): Boolean = add(element, { false }, { true })

    override fun remove(element: Long): Boolean {
        return findSlot(
            element,
            { slot ->
                removeSlot(slot)
                true
            },
            { false })
    }

    override fun clear() {
        if (keysArr !== EMPTY_ARRAY) {
            keysArr.fill(emptyKey)
            threshold += size
        }
        size = 0
    }

    private inline fun <T> findSlot(key: Long, onFind: (slot: Int) -> T, onFail: () -> T): T {
        val keysArr = keysArr
        val mask = keysArr.size - 1

        var slot = key.slot(mask)
        var currKey = keysArr[slot]

        var slotDistance = 0
        while (true) {
            // checking whether the current slot distance is higher than our search distance allows us to early exit the
            // search loop - but the cost of checking is non-trivial. as a compromise between GetHit and GetMiss
            // performance we only check once every half cache line.
            var i = 0
            do {
                if (currKey == emptyKey) {
                    return onFail()
                } else if (currKey == key) {
                    return onFind(slot)
                }

                slot = (slot + 1) and mask
                currKey = keysArr[slot]
            } while (++i < HALF_CACHE_LINE_SIZE)

            slotDistance += HALF_CACHE_LINE_SIZE
            if (currKey.slotDistance(slot, mask) < slotDistance) {
                return onFail()
            }
        }
    }

    private inline fun <T> add(key: Long, onPresent: () -> T, onAbsent: () -> T): T {
        if (threshold <= 0) increaseCapacity()
        if (key == emptyKey) changeEmptyKey()

        val keysArr = keysArr
        val mask = keysArr.size - 1

        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            var currKey = keysArr[slot]
            if (currKey == key) {
                return onPresent()
            } else if (currKey == emptyKey || distance > currKey.slotDistance(slot, mask)) {
                var newKey = key

                while (currKey != emptyKey) {
                    keysArr[slot] = newKey
                    newKey = currKey
                    slot = (slot + 1) and mask
                    currKey = keysArr[slot]
                }

                keysArr[slot] = newKey
                --threshold
                ++size
                return onAbsent()
            }

            slot = (slot + 1) and mask
            ++distance
        }
    }

    private fun removeSlot(slot: Int) {
        val keysArr = keysArr
        val mask = keysArr.size - 1

        var currSlot = slot
        var nextSlot = (currSlot + 1) and mask
        var nextKey = keysArr[nextSlot]
        while (nextKey != emptyKey && nextKey.slotDistance(nextSlot, mask) > 0) {
            keysArr[currSlot] = nextKey

            currSlot = nextSlot
            nextSlot = (nextSlot + 1) and mask
            nextKey = keysArr[nextSlot]
        }

        keysArr[currSlot] = emptyKey
        ++threshold
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

    private fun increaseCapacity() {
        check(threshold <= 0)
        if (threshold < 0) {
            rehash(threshold.inv())
        } else {
            rehash(size shl 1)
        }
    }

    private fun rehash(capacity: Int) {
        check(capacity >= size)

        if (capacity == 0 && keysArr !== EMPTY_ARRAY) {
            keysArr = EMPTY_ARRAY
            emptyKey = ZERO
            threshold = MIN_INITIAL_CAPACITY.inv()
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else .9f

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = LongArray(newLength)
        if (emptyKey != ZERO) newKeysArr.fill(emptyKey)
        val newMask = newKeysArr.size - 1

        for (slot in keysArr.indices) {
            val key = keysArr[slot]
            if (key != emptyKey) addRehashing(newKeysArr, newMask, key)
        }

        keysArr = newKeysArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newKeysArr.size * actualLoadFactor).toInt(), newKeysArr.size - 1) - size
    }

    // we can assume key doesn't exist in array and that we never insert zero
    private fun addRehashing(keysArr: LongArray, mask: Int, key: Long) {
        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            var currKey = keysArr[slot]
            if (currKey == emptyKey) {
                keysArr[slot] = key
                return
            } else if (distance > currKey.slotDistance(slot, mask)) {
                var newKey = key

                do {
                    keysArr[slot] = newKey
                    newKey = currKey
                    slot = (slot + 1) and mask
                    currKey = keysArr[slot]
                } while (currKey != emptyKey)

                keysArr[slot] = newKey
                return
            }

            slot = (slot + 1) and mask
            ++distance
        }
    }

    // changes emptyKey to a value not currently in the set, rewriting all empty slots
    private fun changeEmptyKey() {
        var candidate = ZERO
        while (candidate == emptyKey || contains(candidate)) {
            candidate = Random.nextLong()
        }

        val keysArr = keysArr
        for (i in keysArr.indices) {
            if (keysArr[i] == emptyKey) keysArr[i] = candidate
        }
        emptyKey = candidate
    }

    override fun iterator(): MutableLongIterator = Iterator()

    public fun forEach(action: (Long) -> Unit) {
        val keysArr = keysArr

        var slot = keysArr.size - 1
        while (slot >= 0) {
            val key = keysArr[slot]
            if(key != emptyKey) {
                action(key)
            }
            --slot
        }
    }

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

    @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
    private fun Long.mix(mask: Int): Int {
        if ((this.toInt() or mask) == mask) {
            return this.toInt()
        } else {
            var h = hashCode()
            h = h xor (h ushr 16)
            h *= PHI
            h = h xor (h ushr 16)
            return h
        }
    }

    private fun Long.slot(mask: Int): Int = mix(mask) and mask
    private fun Long.slotDistance(slot: Int, mask: Int): Int = (slot - mix(mask)) and mask

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Long = 0.toLong()

        private val EMPTY_ARRAY = longArrayOf(ZERO)

        // Knuth multiplicative hash
        private const val PHI: Int = 0x93d765dd.toInt()

        private const val MIN_INITIAL_CAPACITY = 7

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
