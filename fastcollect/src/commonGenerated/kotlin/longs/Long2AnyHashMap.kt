@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.ArrayUtils
import io.github.sooniln.fastcollect.FastIterator
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
    capacity: Int = DEFAULT_INITIAL_CAPACITY,

) : AbstractMutableLong2AnyMap<V>() {

    init {
        require(capacity >= 0) { "Capacity must be >= 0" }
    }

    public constructor(map: Long2AnyMap<V>): this() { putAll(map) }
    public constructor(map: Map<Long, V>): this() { putAll(map) }

    // when used in hashing mode, the last slot in the array is used to store the zero key/value respectively. when used
    // in array mode, there is no special handling for zero.
    private var keysArr = EMPTY_KEY_ARRAY

    @Suppress("UNCHECKED_CAST")
    private var valuesArr = EMPTY_VALUE_ARRAY as Array<V?>

    private inline val defaultValue: V? get() = null


    private var emptyKey = ZERO

    // use threshold to store the initial size before we allocate anything, after that it's the size at which we rehash
    private var threshold: Int = if (capacity == 0) DEFAULT_INITIAL_CAPACITY else capacity

    override var size: Int = 0
        private set

    override fun isDefaultValue(value: V?): Boolean = value == defaultValue

    /**
     * Ensures that the map can hold at least given number of key/value pairs without any further resizing of the
     * backing array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "The expected number of elements must be nonnegative" }
        if (keysArr === EMPTY_KEY_ARRAY) {
            threshold = capacity
        } else if (capacity > threshold) {
            rehash(capacity)
        }
    }

    /**
     * Reduces the size of the backing array to the minimum required to hold the current number of elements.
     */
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    public fun trimToSize() {
        rehash(size)
    }

    override fun containsKey(key: Long): Boolean = findSlot(key, { true }, { false })

    override fun containsValue(value: V): Boolean {
        val keysArr = keysArr
        val valuesArr = valuesArr
        val emptyKey = emptyKey
        for (slot in keysArr.indices) {
            if (valuesArr[slot] == value && keysArr[slot] != emptyKey) return true
        }
        return false
    }

    override fun get(key: Long): V? = findSlot(key, { slot -> valuesArr[slot] }, { defaultValue })

    override fun put(key: Long, value: V): V? {
        resizeIfNecessary()

        if (key == emptyKey) changeEmptyKey()

        val keysArr = keysArr
        val valuesArr = valuesArr
        val emptyKey = emptyKey
        val mask = keysArr.size - 1

        var newKey = key
        var newValue: V? = value

        var slot = key.slot(mask)
        var newSlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == emptyKey) {
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                ++size
                return defaultValue
            } else if (currKey == newKey) {
                val oldValue = valuesArr[slot]
                valuesArr[slot] = newValue
                return oldValue
            }

            val currSlotDistance = currKey.slotDistance(slot, mask)
            if (newSlotDistance > currSlotDistance) {
                val currValue = valuesArr[slot]
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                newKey = currKey
                newValue = currValue
                newSlotDistance = currSlotDistance
            }

            slot = (slot + 1) and mask
            ++newSlotDistance
        }
    }

    override fun set(key: Long, value: V) {
        resizeIfNecessary()

        if (key == emptyKey) changeEmptyKey()

        val keysArr = keysArr
        val valuesArr = valuesArr
        val emptyKey = emptyKey
        val mask = keysArr.size - 1

        var newKey = key
        var newValue: V? = value

        var slot = key.slot(mask)
        var newSlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == emptyKey) {
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                ++size
                return
            } else if (currKey == newKey) {
                valuesArr[slot] = newValue
            }

            val currSlotDistance = currKey.slotDistance(slot, mask)
            if (newSlotDistance > currSlotDistance) {
                val currValue = valuesArr[slot]
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                newKey = currKey
                newValue = currValue
                newSlotDistance = currSlotDistance
            }

            slot = (slot + 1) and mask
            ++newSlotDistance
        }
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

            valuesArr.fill(null)

        }
        size = 0
    }

    private inline fun <T> findSlot(key: Long, onFind: (slot: Int) -> T, onFail: () -> T): T {
        val keysArr = keysArr
        val emptyKey = emptyKey
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

    private fun removeSlot(slot: Int) {
        val keysArr = keysArr
        val valuesArr = valuesArr
        val emptyKey = emptyKey
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

        --size
    }

    override fun putAll(from: Long2AnyMap<V>) {
        if (from is Long2AnyHashMap && from.size / 2 > size) {
            val old = iterator()
            resetTo(from)
            for ((key, value) in old) {
                if (!containsKey(key)) set(key, value)
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

    private fun resizeIfNecessary() {
        if (keysArr === EMPTY_KEY_ARRAY) {
            rehash(threshold)
        } else if (size >= threshold) {
            rehash(threshold shl 1)
        }
    }

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    private fun rehash(capacity: Int) {
        check(capacity >= size)

        if (capacity == 0 && keysArr !== EMPTY_KEY_ARRAY) {
            keysArr = EMPTY_KEY_ARRAY

            valuesArr = EMPTY_VALUE_ARRAY as Array<V?>

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

        val newValuesArr = arrayOfNulls<Any>(newLength) as Array<V?>

        val newMask = newKeysArr.size - 1

        val oldKeysArr = keysArr
        val emptyKey = emptyKey
        for (slot in oldKeysArr.indices) {
            val key = keysArr[slot]
            if (key != emptyKey) putRehashing(newKeysArr, newValuesArr, newMask, key, valuesArr[slot])
        }

        keysArr = newKeysArr
        valuesArr = newValuesArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newKeysArr.size * actualLoadFactor).toInt(), newKeysArr.size - 1)
    }

    // we can assume key doesn't exist in array and that we never insert emptyKey

    private fun putRehashing(keysArr: LongArray, valuesArr: Array<V?>, mask: Int, key: Long, value: V?) {

        val emptyKey = emptyKey

        var slot = key.slot(mask)
        var newKey = key
        var newValue = value
        var newSlotDistance = 0
        while (true) {
            val currKey = keysArr[slot]
            if (currKey == emptyKey) {
                keysArr[slot] = newKey
                valuesArr[slot] = newValue
                return
            }

            val currSlotDistance = currKey.slotDistance(slot, mask)
            if (newSlotDistance > currSlotDistance) {
                keysArr[slot] = newKey
                newKey = currKey
                val currValue = valuesArr[slot]
                valuesArr[slot] = newValue
                newValue = currValue
                newSlotDistance = currSlotDistance
            }

            slot = (slot + 1) and mask
            ++newSlotDistance
        }
    }

    // changes emptyKey to a value not currently in the map, rewriting all empty slots
    private fun changeEmptyKey() {
        val oldEmptyKey = emptyKey

        // TODO: should we always try zero first or is that asking for trouble?
        var candidate = ZERO
        while (candidate == oldEmptyKey || findSlot(candidate, { true }, { false })) {
            candidate = Random.nextLong()
        }

        val keysArr = keysArr
        for (i in keysArr.indices) {
            if (keysArr[i] == oldEmptyKey) keysArr[i] = candidate
        }
        emptyKey = candidate
    }

    override operator fun iterator(): MutableFastIterator<MutableLong2AnyMap.MutableEntry<V>> = FastEntryIterator()

    public fun forEach(action: (Long, V) -> Unit) {
        val keysArr = keysArr
        val valuesArr = valuesArr
        val emptyKey = emptyKey

        var slot = keysArr.size - 1
        while (slot >= 0) {
            val key = keysArr[slot]
            if(key != emptyKey) {
                @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                action(key, valuesArr[slot] as V)
            }
            --slot
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

    @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
    private fun Long.slot(mask: Int): Int {
        if ((this.toInt() or mask) == mask) {
            return this.toInt()
        } else {
            var h = hashCode()
            h = h xor (h ushr 16)
            h = h * PHI
            h = h xor (h ushr 16)
            return h and mask
        }
    }

    @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
    private fun Long.slotDistance(slot: Int, mask: Int): Int {
        if ((this.toInt() or mask) == mask) {
            return (slot - this.toInt()) and mask
        } else {
            var h = hashCode()
            h = h xor (h ushr 16)
            h = h * PHI
            h = h xor (h ushr 16)
            return (slot - h) and mask
        }
    }

    internal companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO: Long = 0.toLong()

        private val EMPTY_KEY_ARRAY = longArrayOf(ZERO)

        private val EMPTY_VALUE_ARRAY = arrayOfNulls<Any?>(1)


        // Knuth multiplicative hash
        private const val PHI: Int = 0x9E3779B9.toInt()

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
