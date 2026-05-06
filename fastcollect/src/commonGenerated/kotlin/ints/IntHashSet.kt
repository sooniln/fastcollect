package io.github.sooniln.fastcollect.ints

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min

/**
 * A [HashSet](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Ints. Can be used in place of
 * the Kotlin standard library [HashSet] implementations to improve performance and memory usage. Has the same API
 * contracts as the standard library [HashSet] unless noted otherwise.
 *
 * This implementation differs in behavior from common hash sets in that at low capacity numbers it will effectively
 * force a loadFactor of 1. This may improve performance slightly at low capacities, or at worst will be only a small
 * performance penalty - however it will substantially reduce memory requirements at low capacities.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Note that a load factor of 1.0 is accepted, unlike many HashSets - this is interpreted to mean that only 1 slot need
 * ever remain free (i.e. the actual load factor is (capacity - 1)/capacity).
 */
public class IntHashSet @JvmOverloads constructor(
    capacity: Int = DEFAULT_INITIAL_CAPACITY,
    private val loadFactor: Float = DEFAULT_LOAD_FACTOR,
) : AbstractMutableIntSet() {

    init {
        require(loadFactor > 0 && loadFactor <= 1) { "Load factor must be > 0 and <= 1" }
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(elements: Collection<Int>): this(elements.size) {
        addAll(elements)
    }

    // when used in hashing mode, the last slot in the array is used to store the zero key. when used in array mode,
    // there is no special handling for zero.
    private var keysArr = EMPTY_ARRAY
    private var mask = keysArr.mask()

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
            threshold = capacity
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
        val rotVal = mask.rotVal()
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
        val rotVal = mask.rotVal()
        var slot = key.slot(mask, rotVal)
        //var slotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == key) {
                return slot
            } else if (currKey == ZERO /*|| currKey.slotDistance(slot, mask, rotVal) < slotDistance*/) {
                return -1
            }

            // do not bother to compare slot distances to break out of the loop - in benchmarking this has not proved
            // worth it. the calculation overhead is likely too high since we don't cache hash/DIBs. worsened latency in
            // lookup hits is ~4x the improved latency in lookup misses on average...

            slot = slot.nextSlot(mask)
            //++slotDistance
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
        val rotVal = mask.rotVal()

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
            for (element in elements) {
                modified = add(element) or modified
            }
        } else {
            for (element in elements) {
                modified = add(element) or modified
            }
        }
        return modified
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
            threshold = DEFAULT_INITIAL_CAPACITY
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1f else loadFactor

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = IntArray(newLength)
        val newMask = newKeysArr.mask()
        val newEndSlot = newKeysArr.endSlot()

        val newRotVal = newMask.rotVal()
        val oldEndSlot = keysArr.endSlot()
        for (slot in 0..<oldEndSlot) {
            val key = keysArr[slot]
            if (key != ZERO) addRehashing(newKeysArr, newMask, newRotVal, key)
        }
        newKeysArr[newEndSlot] = keysArr[oldEndSlot]

        keysArr = newKeysArr
        mask = newMask

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newEndSlot * actualLoadFactor).toInt(), newEndSlot - 1)
    }

    // we can assume element doesn't exist in array and that we never insert zero
    private fun addRehashing(keysArr: IntArray, mask: Int, rotVal: Int, element: Int) {
        var slot = element.slot(mask, rotVal)
        var newKey = element
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
            // why do we stride by 17 instead of 1 (which we could further optimize and would be cheaper, as well as
            // being more logical)? first, in terms of correctness, since our backing array is power-of-two sized, any
            // odd number will be coprime, and thus any odd stride is still guaranteed to cover all elements given that
            // we're wrapping around via the mask. second, this is a defense against pathological accidentally quadratic
            // scenarios (google "robin hood accidentally quadratic"). since the backing array is by design storing
            // elements as close to their 'home' slot as possible, iteration in array order will tend to return large
            // groupings all with the same/nearby home slot. if this ordering is used for insertion it results in
            // pathologically quadratic devolution, as each element insertion has to push through extremely large chains
            // of elements all close to their home slot. however, we know (you can trivially simulate this or check
            // papers on DIB distributions in robin hood tables) that max DIBs grow very slowly (logarithmic wrt table
            // size), and the 99th percentile of DIBs is 17 for pretty much every table we can address.
            //
            // so this means that by picking a stride of 17 we are pretty much guaranteed that every next element we
            // pick will fall into a different 'home' cluster than the previous element we return. now if we happen to
            // be inserting in iteration order, every element we insert should hash to a different cluster and avoid
            // pathologically quadratic-ness.
            //
            // and of course subtracting a simple constant here is far cheaper than the other solutions considered
            // (random seed in hash smearing - requires an extra 4 bytes per instance to store the seed and an extra
            // operation within the smear which is our most performance sensitive inner loop code, also far less cache
            // hits).
            //
            // note that this is not a defense against dedicated adversarial attacks - the original sequence is
            // trivially reconstructable, but we are not using a smear that's terribly secure anyway, and the overall
            // design of this library prioritizes performance over security.

            var s = slot
            do {
                s = (s - 17) and mask
            } while (keysArr[s] == ZERO)
            slot = s
        }
    }

    // the slot at the end of slot iteration (exclusive), also the slot that stores the zero value
    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.endSlot(): Int = size - 1

    // caching the mask in a member var can reduce lookup miss latency - at the cost of +4 bytes per instance
    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.mask(): Int = size - 2

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.rotVal(): Int = countOneBits()

    @Suppress("NOTHING_TO_INLINE")
    private inline fun mixHash(hashcode: Int, rotVal: Int): Int {
        // hash smear based on rustc-hash, the hashing function used internally within the rust compiler. this has a
        // couple advantages for our use case. 1) it's fast 2) it's not a super strong hash function, but we're using it
        // as a smear... 3) it's a multiplicative hash which means it tends to concentrate entropy in the high bits
        // (where they're not terribly useful for our needs) - but we can rotate by the table size which concentrates
        // the entropy in the low bits exactly where we need them! taking the table size into account means that the
        // hashcode changes every time the table size does, which for a normal robin hood implementation would break
        // hashcode caching. since we don't cache hashcodes however, this is actually a win for us.

        // this smear also performed quite favorably in testing against a variety of adversarial inputs - but that said
        // I am definitely not a hashing expert and would not claim this smear is very DOS resistant. if you see
        // weaknesses, please say something.

        return (hashcode * K).rotateLeft(rotVal)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slot(mask: Int, rotVal: Int = mask.rotVal()): Int {
        return mixHash(this.hashCode(), rotVal) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.nextSlot(mask: Int): Int {
        return (this + 1) and mask
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.slotDistance(slot: Int, mask: Int, rotVal: Int = mask.rotVal()): Int {
        return (slot - slot(mask, rotVal)) and mask
    }

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Int = 0.toInt()
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val NONZERO: Int = 1.toInt()

        private val EMPTY_ARRAY = intArrayOf(ZERO, NONZERO)

        // Constant taken from:
        //     "Computationally Easy, Spectrally Good Multipliers for Congruential
        //     Pseudorandom Number Generators" by Guy Steele and Sebastiano Vigna.
        private const val K: Int = 0x93d765dd.toInt()

        private const val DEFAULT_LOAD_FACTOR = .9f
        private const val DEFAULT_INITIAL_CAPACITY = 1 shl 2  // must be power of two
        private const val MAXIMUM_CAPACITY: Int = 1 shl 30 // must be power of two

        // we force the load factor to 1.0 up to the size of two cache lines (which we assume are 64 bytes each)
        private const val FORCE_LOAD_FACTOR_MAX: Int = 2 * 64 / Int.SIZE_BYTES

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
