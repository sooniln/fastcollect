@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.ArrayUtils
import io.github.sooniln.fastcollect.FastIterator
import io.github.sooniln.fastcollect.Hash
import io.github.sooniln.fastcollect.MutableFastIterator

import io.github.sooniln.fastcollect.longs.MutableLongCollection
import io.github.sooniln.fastcollect.longs.MutableLongIterator
import io.github.sooniln.fastcollect.longs.LongConsumer

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Long to Long
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
public class Long2LongHashMap @JvmOverloads constructor(
    capacity: Int = 0,

    /** The default value should be the value that is ideally least likely to occur in the map. */
    private val defaultValue: Long = Long.MIN_VALUE,

) : AbstractMutableLong2LongMap() {

    public constructor(map: Long2LongMap): this() { putAll(map) }
    public constructor(map: Map<Long, Long>): this() { putAll(map) }

    private var keysArr = EMPTY_KEY_ARRAY

    private var valuesArr = EMPTY_VALUE_ARRAY


    private var emptyKey = ZERO

    // threshold + size == capacity (rehash once threshold <= 0, if we haven't allocated yet then threshold.inv() is
    // our initial capacity)
    private var threshold = MIN_INITIAL_CAPACITY.inv()

    override var size: Int = 0
        private set

    init {
        ensureCapacity(capacity)
    }

    override fun isDefaultValue(value: Long): Boolean = value == defaultValue

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

    override fun containsValue(value: Long): Boolean {
        val keysArr = keysArr
        val valuesArr = valuesArr
        for (slot in keysArr.indices) {
            if (valuesArr[slot] == value && keysArr[slot] != emptyKey) return true
        }
        return false
    }

    override fun get(key: Long): Long = findSlot(key, { slot -> valuesArr[slot] }, { defaultValue })

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    override fun getValue(key: Long): Long = findSlot(key, { slot -> valuesArr[slot] as Long }, { throw NoSuchElementException() })

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    override fun getOrDefault(key: Long, defaultValue: Long): Long = findSlot(key, { slot -> valuesArr[slot] as Long }, { defaultValue })

    override fun put(key: Long, value: Long): Long {
        var returnValue = defaultValue
        set(key, {
            value
        }, { slot ->
            returnValue = valuesArr[slot]
            value
        })
        return returnValue
    }

    public fun putIfAbsent(key: Long, value: Long): Long {
        set(key, { value }, { slot -> return valuesArr[slot] })
        return defaultValue
    }

    override fun set(key: Long, value: Long) {
        set(key, { value }, { value })
    }

    override fun remove(key: Long): Long {
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

    private inline fun set(key: Long, onAdd: () -> Long, onReplace: (slot: Int) -> Long) {
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

    private fun shiftAndInsert(keysArr: LongArray, mask: Int, slot: Int, currKey: Long, newKey: Long, newValue: Long) {
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

        threshold += 1
        size -= 1
    }

    override fun putAll(from: Long2LongMap) {
        if (from is Long2LongHashMap && from.size / 2 > size) {
            val oldKeysArr = keysArr
            val oldValuesArr = valuesArr
            val oldEmptyKey = emptyKey

            resetTo(from)
            for (slot in oldKeysArr.indices) {
                val key = oldKeysArr[slot]
                if (key != oldEmptyKey) {
                    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                    putIfAbsent(key, oldValuesArr[slot] as Long)
                }
            }
            trimToSize()
        } else {
            ensureCapacity(max(size + (from.size / 2), from.size))
            for ((key, value) in from) {
                set(key, value)
            }
        }
    }

    override fun putAll(from: Map<out Long, Long>) {
        ensureCapacity(max(size + (from.size / 2), from.size))
        for ((key, value) in from) {
            set(key, value)
        }
    }

    private fun resetTo(from: Long2LongHashMap) {
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
                override val size: Int get() = this@Long2LongHashMap.size
                override fun contains(element: Long): Boolean = containsKey(element)
                override fun add(element: Long): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableLongIterator = KeyIterator()
                override fun foreach(action: LongConsumer) = foreachKey(action)
                override fun clear() = throw UnsupportedOperationException()
            }
            .also { _keys = it }
    }

    private var _values: MutableLongCollection? = null
    override val values: MutableLongCollection get() {
        return _values ?:

            object : MutableLongCollection {

                override val size: Int get() = this@Long2LongHashMap.size
                override fun contains(element: Long): Boolean = containsValue(element)
                override fun add(element: Long): Boolean = throw UnsupportedOperationException()
                override fun remove(element: Long): Boolean = throw UnsupportedOperationException()
                override fun iterator(): MutableLongIterator = ValueIterator()

                override fun foreach(action: LongConsumer) = foreach { _, value -> action.accept(value) }

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

            valuesArr = EMPTY_VALUE_ARRAY

            emptyKey = ZERO
            threshold = MIN_INITIAL_CAPACITY.inv()
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1.0 else 7.0/8.0

        val newLength = arraySize(capacity, actualLoadFactor)
        if (keysArr.size == newLength) return

        val newKeysArr = LongArray(newLength)
        if (emptyKey != ZERO) newKeysArr.fill(emptyKey)

        val newValuesArr = LongArray(newLength)

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

    private fun setRehashing(keysArr: LongArray, valuesArr: LongArray, mask: Int, key: Long, value: Long) {

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

    override operator fun iterator(): MutableFastIterator<MutableLong2LongMap.MutableEntry> = FastEntryIterator()


    override fun foreach(action: LongLongConsumer) {

        val keysArr = keysArr
        val valuesArr = valuesArr

        for (slot in keysArr.indices) {
            val key = keysArr[slot]
            if (key != emptyKey) {
                @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                action.accept(key, valuesArr[slot] as Long)
            }
        }
    }

    override fun foreachKey(action: LongConsumer) {
        val keysArr = keysArr

        for (slot in keysArr.indices) {
            val key = keysArr[slot]
            if (key != emptyKey) {
                action.accept(key)
            }
        }
    }

    private open inner class SlotIterator {
        private val keysArr = this@Long2LongHashMap.keysArr
        private val valuesArr = this@Long2LongHashMap.valuesArr
        private val emptyKey = this@Long2LongHashMap.emptyKey
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
        fun value(): Long = valuesArr[previousSlot] as Long

        fun updateValue(newValue: Long) {
            check(previousSlot != -1)
            if (keysArr !== this@Long2LongHashMap.keysArr) throw ConcurrentModificationException()

            valuesArr[previousSlot] = newValue
        }

        fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@Long2LongHashMap.keysArr) throw ConcurrentModificationException()

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


    private inner class ValueIterator : MutableLongIterator() {

        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()


        override fun nextLong(): Long {

            it.nextSlot()
            return it.value()
        }

        override fun remove() = it.remove()
    }

    private inner class FastEntryIterator: SlotIterator(), MutableFastIterator<MutableLong2LongMap.MutableEntry>, MutableLong2LongMap.MutableEntry {

        override val key: Long get() = key()
        override var value: Long
            get() = value()
            set(value) {
                updateValue(value)
            }

        override fun next(): MutableLong2LongMap.MutableEntry {
            nextSlot()
            return this
        }

        override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
        override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        override fun toString(): String = "$key=$value"
    }

    private fun Long.slot(mask: Int): Int = Hash.mix(this) and mask
    private fun Long.slotDistance(slot: Int, mask: Int): Int = (slot - Hash.mix(this)) and mask

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO = 0.toLong()

        private val EMPTY_KEY_ARRAY = longArrayOf(ZERO)

        private val EMPTY_VALUE_ARRAY = LongArray(1)


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
            if (actualArraySize < requiredArraySize) throw Error("Required array length $requiredArraySize is too large")
            return actualArraySize
        }
    }
}
