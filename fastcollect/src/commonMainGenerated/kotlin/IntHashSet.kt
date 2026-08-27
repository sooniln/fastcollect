package io.github.sooniln.fastcollect

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashSet](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Ints.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Note that a load factor of 1.0 is accepted - this is interpreted to mean that only 1 slot need ever remain free (i.e.
 * the actual load factor is (capacity - 1)/capacity). For small capacities, this HashSet automatically forces a load
 * factor of 1.0.
 *
 * The extension method [asSet] produces a thin wrapper around this class which exposes it as Kotlin map which can be
 * used anywhere a Kotlin set is expected. Using this wrapper may incur boxing penalties.
 */
public class IntHashSet @JvmOverloads constructor(
    capacity: Int = 0,
) : AbstractMutableIntSet() {

    public constructor(elements: IntCollection): this() { addAll(elements) }
    public constructor(elements: Collection<Int>): this() { addAll(elements) }

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

    override fun contains(element: Int): Boolean = findSlot(element, { true }, { false })

    override fun add(element: Int): Boolean = add(element, { false }, { true })

    override fun remove(element: Int): Boolean {
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

    private inline fun <T> findSlot(key: Int, onFind: (slot: Int) -> T, onFail: () -> T): T {
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
                if (currKey equalsRaw emptyKey) {
                    return onFail()
                } else if (currKey equalsRaw key) {
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

    private inline fun <T> add(key: Int, onPresent: () -> T, onAbsent: () -> T): T {
        if (threshold <= 0) increaseCapacity()
        if (key equalsRaw emptyKey) changeEmptyKey()

        val keysArr = keysArr
        val mask = keysArr.size - 1

        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey equalsRaw key) {
                return onPresent()
            } else if (currKey equalsRaw emptyKey || distance > currKey.slotDistance(slot, mask)) {
                shiftAndInsert(keysArr, mask, slot, currKey, key)
                return onAbsent()
            }

            slot = (slot + 1) and mask
            distance += 1
        }
    }

    private fun shiftAndInsert(keysArr: IntArray, mask: Int, slot: Int, currKey: Int, newKey: Int) {
        var nextSlot = slot
        var currKey = currKey
        var newKey = newKey

        while (currKey notEqualsRaw emptyKey) {
            keysArr[nextSlot] = newKey
            newKey = currKey
            nextSlot = (nextSlot + 1) and mask
            currKey = keysArr[nextSlot]
        }

        keysArr[nextSlot] = newKey
        threshold -= 1
        size += 1

        // since Robin Hood hashing shifts chains of elements on inserts, it is a viable DoS attack strategy to create
        // large chains of entries such that insertion devolves to O(n^2). Beyond DoS, this can be triggered even in
        // unintentional ways, since the iteration order is hash order. It is not our intention to prevent or even
        // mitigate DoS attacks (this class is not designed to be attack-resistant), but we do want to help the user
        // out if they shoot themselves in the foot a bit.
        //
        // if we detect pathologically long chains of shifts on inserts (far greater than we would expect in any
        // conceivable normal usage), and if the table is more than 50% full, we rehash to the next greater size to
        // reduce chain length. to calculate expected chain length at a given size, formulas are given in
        // https://www.cs.tau.ac.il//~zwick/Adv-Alg-2015/Linear-Probing.pdf and similar. i'd say i used those, but it
        // was much simpler to do a monte carlo simulation and approximate the results for α=7/8:
        //
        // k* ≈ 80·log₂(n) − C   -> overestimate with power-of-two ->   k* ≈ 128·b, b=log₂(n)

        if (threshold < size && ((nextSlot - slot) and mask) > 128 * mask.countOneBits()) {
            val newCapacity = (threshold + size) shl 1
            if (newCapacity > size) rehash(newCapacity)
        }
    }

    private fun removeSlot(slot: Int) {
        val keysArr = keysArr
        val mask = keysArr.size - 1

        var currSlot = slot
        var nextSlot = (currSlot + 1) and mask
        var nextKey = keysArr[nextSlot]
        while (nextKey notEqualsRaw emptyKey && nextKey.slotDistance(nextSlot, mask) > 0) {
            keysArr[currSlot] = nextKey

            currSlot = nextSlot
            nextSlot = (nextSlot + 1) and mask
            nextKey = keysArr[nextSlot]
        }

        keysArr[currSlot] = emptyKey
        threshold += 1
        size -= 1
    }

    override fun addAll(elements: IntCollection): Boolean {
        val oldSize = size
        if (elements is IntHashSet && elements.size / 2 > size) {
            val oldKeysArr = keysArr
            val oldEmptyKey = emptyKey

            resetTo(elements)
            for (key in oldKeysArr) {
                if (key notEqualsRaw oldEmptyKey) {
                    add(key)
                }
            }
            trimToSize()
        } else {
            ensureCapacity(max(size + (elements.size / 2), elements.size))
            elements.foreach { element ->
                add(element)
            }
        }
        return size != oldSize
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        ensureCapacity(max(size + (elements.size / 2), elements.size))
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    private fun resetTo(elements: IntHashSet) {
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
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1.0 else 7.0/8.0

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = IntArray(newLength)
        if (emptyKey notEqualsRaw ZERO) newKeysArr.fill(emptyKey)
        val newMask = newKeysArr.size - 1

        for (slot in keysArr.indices) {
            val key = keysArr[slot]
            if (key notEqualsRaw emptyKey) addRehashing(newKeysArr, newMask, key)
        }

        keysArr = newKeysArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newKeysArr.size * actualLoadFactor).toInt(), newKeysArr.size - 1) - size
    }

    // we can assume key doesn't exist in array and that we never insert zero
    private fun addRehashing(keysArr: IntArray, mask: Int, key: Int) {
        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            var currKey = keysArr[slot]
            if (currKey equalsRaw emptyKey) {
                keysArr[slot] = key
                return
            } else if (distance > currKey.slotDistance(slot, mask)) {
                var newKey = key

                do {
                    keysArr[slot] = newKey
                    newKey = currKey
                    slot = (slot + 1) and mask
                    currKey = keysArr[slot]
                } while (currKey notEqualsRaw emptyKey)

                keysArr[slot] = newKey
                return
            }

            slot = (slot + 1) and mask
            distance += 1
        }
    }

    // changes emptyKey to a value not currently in the set, rewriting all empty slots
    private fun changeEmptyKey() {
        var candidate = ZERO
        while (candidate equalsRaw emptyKey || contains(candidate)) {
            candidate = Random.nextInt()
        }

        val keysArr = keysArr
        for (i in keysArr.indices) {
            if (keysArr[i] equalsRaw emptyKey) keysArr[i] = candidate
        }
        emptyKey = candidate
    }

    override fun iterator(): MutableIntIterator = Iterator()

    override fun traverser(): MutableIntTraverser = Traverser()

    private inner class Iterator : MutableIntIterator() {
        private val keysArr = this@IntHashSet.keysArr
        private val emptyKey = this@IntHashSet.emptyKey
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

        override fun nextInt(): Int {
            if (slotsLeft <= 0) throw NoSuchElementException()
            previousSlot = slot
            if (--slotsLeft > 0) decrement()
            return keysArr[previousSlot]
        }

        override fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@IntHashSet.keysArr) throw ConcurrentModificationException()

            removeSlot(previousSlot)
            previousSlot = -1

            // if removal wrapped all the way around to our next slot then we need to adjust
            if (keysArr[slot] equalsRaw emptyKey) {
                slot = (slot - 1) and mask
            }
        }

        private fun decrement() {
            do {
                slot = (slot - 1) and mask
            } while (keysArr[slot] equalsRaw emptyKey)
        }
    }

    private inner class Traverser : MutableIntTraverser {
        private val keysArr = this@IntHashSet.keysArr
        private val emptyKey = this@IntHashSet.emptyKey
        private val mask = keysArr.size - 1

        private var slotsLeft = size
        private var slot = keysArr.size
        private var key = emptyKey

        override val value: Int get() {
            check(key notEqualsRaw emptyKey)
            return key
        }

        override fun forward(): Boolean {
            if (slotsLeft <= 0) {
                return false
            }
            if (keysArr !== this@IntHashSet.keysArr) throw ConcurrentModificationException()

            while (true) {
                slot = (slot - 1) and mask
                key = keysArr[slot]
                if (key notEqualsRaw emptyKey) {
                    --slotsLeft
                    return true
                }
            }
        }

        override fun remove() {
            check(key notEqualsRaw emptyKey)
            if (keysArr !== this@IntHashSet.keysArr) throw ConcurrentModificationException()

            removeSlot(slot)
            key = emptyKey
        }
    }

    private fun Int.slot(mask: Int): Int = Hash.mix(this) and mask
    private fun Int.slotDistance(slot: Int, mask: Int): Int = (slot - Hash.mix(this)) and mask

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Int = 0.toInt()

        private val EMPTY_ARRAY = intArrayOf(ZERO)

        private const val MIN_INITIAL_CAPACITY = 7

        private const val CACHE_LINE_SIZE = 64 / Int.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2

        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * CACHE_LINE_SIZE

        private fun arraySize(capacity: Int, loadFactor: Double): Int {
            check(capacity >= 0)
            // array must always maintain the invariant of at least one slot remaining open
            val requiredArraySize = max((capacity / loadFactor).toInt(), capacity + 1)
            val actualArraySize = minPowerOfTwo(requiredArraySize)
            if (actualArraySize < requiredArraySize) throw Error("Required array length $requiredArraySize is too large")
            return actualArraySize
        }
    }
}
