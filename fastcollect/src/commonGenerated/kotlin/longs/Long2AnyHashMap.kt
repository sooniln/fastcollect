@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.ArrayUtils
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.Hash
import io.github.sooniln.fastcollect.MutableFastIterator

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Long to V
 * relationships.
 *
 * The [keys]/[values] mutable collections exposed by this class will throw [UnsupportedOperationException] on any
 * attempt to mutate the collection, EXCEPT that [MutableIterator.remove] will work as expected. Other mutation
 * operations should be made directly on the map rather than on the sub-collections.
 *
 * The entry [iterator] exposed by this class is a [FastIterator] - clients may not allow the returned entry to escape. See
 * the [FastIterator] documentation for more information.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 */
public class Long2AnyHashMap<V> @JvmOverloads constructor(
    capacity: Int = 0,

) : AbstractMutableLong2AnyMap<V>() {

    public constructor(map: Long2AnyMap<V>): this() { putAll(map) }
    public constructor(map: Map<Long, V>): this() { putAll(map) }

    // when used in hashing mode, the last slot in the array is used to store the zero key/value respectively. when used
    // in array mode, there is no special handling for zero.
    private var keysArr = EMPTY_KEY_ARRAY

    @Suppress("UNCHECKED_CAST")
    private var valuesArr = EMPTY_VALUE_ARRAY as Array<V?>

    private inline val defaultValue: V? get() = null


    private var emptyKey = ZERO

    // threshold + size == capacity (rehash once threshold <= 0, if we haven't allocated yet then threshold.inv() is
    // our initial capacity)
    private var threshold = MIN_INITIAL_CAPACITY.inv()

    override var size: Int = 0
        private set

    init {
        ensureCapacity(capacity)
    }

    override fun isDefaultValue(value: V?): Boolean = value == defaultValue

    /**
     * Ensures that the map can hold at least given number of key/value pairs without any further resizing of the
     * backing array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "Capacity must be >= 0" }
        if (keysArr === EMPTY_KEY_ARRAY) {
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

    override fun containsKey(key: Long): Boolean = findSlot(key, { true }, { false })

    override fun containsValue(value: V): Boolean {
        val keysArr = keysArr
        val valuesArr = valuesArr
        for (slot in keysArr.indices) {
            if (valuesArr[slot] == value && keysArr[slot] != emptyKey) return true
        }
        return false
    }

    override fun get(key: Long): V? = findSlot(key, { slot -> valuesArr[slot] }, { defaultValue })

    public fun getOrDefault(key: Long, default: V): V? = findSlot(key, { slot -> valuesArr[slot] }, { default })

    override fun put(key: Long, value: V): V? {
        var returnValue = defaultValue
        set(key, {
            value
        }, { slot ->
            returnValue = valuesArr[slot]
            value
        })
        return returnValue
    }

    public fun putIfAbsent(key: Long, value: V): V? {
        set(key, { value }, { slot -> return valuesArr[slot] })
        return defaultValue
    }

    override fun set(key: Long, value: V) {
        set(key, { value }, { value })
    }

    override fun remove(key: Long): V? {
        return findSlot(
            key,
            { slot ->
                val oldValue = valuesArr[slot]
                removeSlot(slot)
                oldValue
            },
            { defaultValue })
    }

    override fun clear() {
        if (keysArr !== EMPTY_KEY_ARRAY) {
            keysArr.fill(emptyKey)
            threshold += size

            valuesArr.fill(null)

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

    private inline fun set(key: Long, onAdd: () -> V, onReplace: (slot: Int) -> V) {
        if (threshold <= 0) increaseCapacity()
        if (key == emptyKey) changeEmptyKey()

        val keysArr = keysArr
        val mask = keysArr.size - 1

        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == key) {
                valuesArr[slot] = onReplace(slot)
                return
            } else if (currKey == emptyKey || distance > currKey.slotDistance(slot, mask)) {
                shiftAndInsert(keysArr, mask, slot, currKey, key, onAdd())
                return
            }

            slot = (slot + 1) and mask
            distance += 1
        }
    }

    private fun shiftAndInsert(keysArr: LongArray, mask: Int, slot: Int, currKey: Long, newKey: Long, newValue: V?) {
        val valuesArr = valuesArr

        var nextSlot = slot
        var currKey = currKey
        var newKey = newKey
        var newValue = newValue

        while (currKey != emptyKey) {
            val currValue = valuesArr[nextSlot]
            keysArr[nextSlot] = newKey
            valuesArr[nextSlot] = newValue
            newKey = currKey
            newValue = currValue
            nextSlot = (nextSlot + 1) and mask
            currKey = keysArr[nextSlot]
        }

        keysArr[nextSlot] = newKey
        valuesArr[nextSlot] = newValue
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
        // was much simpler to do a monte carlo simulation and approximate the results for α=5/6:
        //
        // k* ≈ 44·log₂(n) − 200   -> overestimate with power-of-two ->   k* ≈ 64·b, b=log₂(n)

        if (threshold < size && (nextSlot - slot) and mask > 64 * mask.countOneBits()) {
            val newCapacity = (threshold + size) shl 1
            if (newCapacity > size) rehash(newCapacity)
        }
    }

    private fun removeSlot(slot: Int) {
        val keysArr = keysArr
        val valuesArr = valuesArr
        val mask = keysArr.size - 1

        var currSlot = slot
        var nextSlot = (currSlot + 1) and mask
        var nextKey = keysArr[nextSlot]
        while (nextKey != emptyKey && nextKey.slotDistance(nextSlot, mask) > 0) {
            keysArr[currSlot] = nextKey
            valuesArr[currSlot] = valuesArr[nextSlot]

            currSlot = nextSlot
            nextSlot = (nextSlot + 1) and mask
            nextKey = keysArr[nextSlot]
        }
        keysArr[currSlot] = emptyKey

        valuesArr[currSlot] = null

        threshold += 1
        size -= 1
    }

    override fun putAll(from: Long2AnyMap<V>) {
        if (from is Long2AnyHashMap && from.size / 2 > size) {
            val old = iterator()
            resetTo(from)
            for ((key, value) in old) {
                putIfAbsent(key, value)
            }
        } else {
            ensureCapacity(max(size + (from.size / 2), from.size))
            for ((key, value) in from) {
                set(key, value)
            }
        }
    }

    override fun putAll(from: Map<out Long, V>) {
        ensureCapacity(max(size + (from.size / 2), from.size))
        for ((key, value) in from) {
            set(key, value)
        }
    }

    private fun resetTo(from: Long2AnyHashMap<V>) {
        check(!from.isEmpty())

        keysArr = from.keysArr.copyOf()
        valuesArr = from.valuesArr.copyOf()
        emptyKey = from.emptyKey
        size = from.size
        threshold = from.threshold
    }

    private var _keys: MutableLongSet? = null
    override val keys: MutableLongSet get() {
        return _keys ?:
            object : MutableLongSet {
                override val size: Int get() = this@Long2AnyHashMap.size
                override fun contains(element: Long): Boolean = containsKey(element)
                override fun add(element: Long): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableLongIterator = KeyIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _keys = it }
    }

    private var _values: MutableCollection<V>? = null
    override val values: MutableCollection<V> get() {
        return _values ?:

            object : AbstractMutableCollection<V>() {

                override val size: Int get() = this@Long2AnyHashMap.size
                override fun contains(element: V): Boolean = containsValue(element)
                override fun add(element: V): Boolean = throw UnsupportedOperationException()
                override fun remove(element: V): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableIterator<V> = ValueIterator()
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _values = it }
    }

    private fun increaseCapacity() {
        check(threshold <= 0)
        if (threshold < 0) {
            rehash(threshold.inv())
        } else {
            rehash(size shl 1)
        }
    }

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    private fun rehash(capacity: Int) {
        check(capacity >= size)

        if (capacity == 0 && keysArr !== EMPTY_KEY_ARRAY) {
            keysArr = EMPTY_KEY_ARRAY

            valuesArr = EMPTY_VALUE_ARRAY as Array<V?>

            emptyKey = ZERO
            threshold = MIN_INITIAL_CAPACITY.inv()
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1.0 else 5.0/6.0

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = LongArray(newLength)
        if (emptyKey != ZERO) newKeysArr.fill(emptyKey)

        val newValuesArr = arrayOfNulls<Any>(newLength) as Array<V?>

        val newMask = newLength - 1

        for (slot in keysArr.indices) {
            val key = keysArr[slot]
            if (key != emptyKey) setRehashing(newKeysArr, newValuesArr, newMask, key, valuesArr[slot])
        }

        keysArr = newKeysArr
        valuesArr = newValuesArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newLength * actualLoadFactor).toInt(), newMask) - size
    }

    // we can assume key doesn't exist in array and that we never insert emptyKey

    private fun setRehashing(keysArr: LongArray, valuesArr: Array<V?>, mask: Int, key: Long, value: V?) {

        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            var currKey = keysArr[slot]
            if (currKey == emptyKey) {
                keysArr[slot] = key
                valuesArr[slot] = value
                return
            } else if (distance > currKey.slotDistance(slot, mask)) {
                var newKey = key
                var newValue = value

                do {
                    val currValue = valuesArr[slot]
                    keysArr[slot] = newKey
                    valuesArr[slot] = newValue
                    newKey = currKey
                    newValue = currValue
                    slot = (slot + 1) and mask
                    currKey = keysArr[slot]
                } while (currKey != emptyKey)

                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                return
            }

            slot = (slot + 1) and mask
            distance += 1
        }
    }

    // changes emptyKey to a value not currently in the map, rewriting all empty slots
    private fun changeEmptyKey() {
        var candidate = ZERO
        while (candidate == emptyKey || containsKey(candidate)) {
            candidate = Random.nextLong()
        }

        val keysArr = keysArr
        for (i in keysArr.indices) {
            if (keysArr[i] == emptyKey) keysArr[i] = candidate
        }
        emptyKey = candidate
    }

    override operator fun iterator(): MutableFastIterator<MutableLong2AnyMap.MutableEntry<V>> = FastEntryIterator()

    public fun forEach(action: (Long, V) -> Unit) {
        val keysArr = keysArr
        val valuesArr = valuesArr

        var slot = keysArr.size - 1
        while (slot >= 0) {
            val key = keysArr[slot]
            if(key != emptyKey) {
                @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                action(key, valuesArr[slot] as V)
            }
            slot -= 1
        }
    }

    private open inner class SlotIterator {
        private val keysArr = this@Long2AnyHashMap.keysArr
        private val valuesArr = this@Long2AnyHashMap.valuesArr
        private val emptyKey = this@Long2AnyHashMap.emptyKey
        private val mask = keysArr.size - 1

        private var slotsLeft = size
        private var slot = keysArr.size - 1
        private var previousSlot = -1

        init {
            if (slotsLeft > 0) decrement()
        }

        fun hasNext(): Boolean {
            return slotsLeft > 0
        }

        fun nextSlot() {
            if (slotsLeft <= 0) throw NoSuchElementException()
            previousSlot = slot
            if (--slotsLeft > 0) decrement()
        }

        fun key(): Long = keysArr[previousSlot]
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        fun value(): V = valuesArr[previousSlot] as V

        fun updateValue(newValue: V) {
            check(previousSlot != -1)
            if (keysArr !== this@Long2AnyHashMap.keysArr) throw ConcurrentModificationException()

            valuesArr[previousSlot] = newValue
        }

        fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@Long2AnyHashMap.keysArr) throw ConcurrentModificationException()

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

    private inner class KeyIterator : MutableLongIterator() {
        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()

        override fun nextLong(): Long {
            it.nextSlot()
            return it.key()
        }

        override fun remove() = it.remove()
    }


    private inner class ValueIterator : MutableIterator<V> {

        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()


        override fun next(): V {

            it.nextSlot()
            return it.value()
        }

        override fun remove() = it.remove()
    }

    private inner class FastEntryIterator: SlotIterator(), MutableFastIterator<MutableLong2AnyMap.MutableEntry<V>>, MutableLong2AnyMap.MutableEntry<V> {

        override val key: Long get() = key()
        override var value: V
            get() = value()
            set(value) {
                updateValue(value)
            }

        override fun next(): MutableLong2AnyMap.MutableEntry<V> {
            nextSlot()
            return this
        }

        override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
        override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        override fun toString(): String = "$key=$value"
    }

    private fun Long.slot(mask: Int, shift: Int = mask.countLeadingZeroBits()): Int = Hash.fibonacciHash(this, shift) and mask
    private fun Long.slotDistance(slot: Int, mask: Int, shift: Int = mask.countLeadingZeroBits()): Int = (slot - Hash.fibonacciHash(this, shift)) and mask

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO = 0.toLong()

        private val EMPTY_KEY_ARRAY = longArrayOf(ZERO)

        private val EMPTY_VALUE_ARRAY = arrayOfNulls<Any?>(1)


        private const val MIN_INITIAL_CAPACITY = 7

        private const val CACHE_LINE_SIZE = 64 / Long.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2

        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX = 2 * CACHE_LINE_SIZE

        private fun arraySize(capacity: Int, loadFactor: Double): Int {
            check(capacity >= 0)
            // array must always maintain the invariant of at least one slot remaining open
            val requiredArraySize = max((capacity / loadFactor).toInt(), capacity + 1)
            val actualArraySize = ArrayUtils.minPowerOfTwo(requiredArraySize)
            if (actualArraySize < requiredArraySize) throw Error("Required array length requiredArraySize is too large")
            return actualArraySize
        }
    }
}
