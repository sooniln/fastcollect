package io.github.sooniln.fastcollect

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Int to Byte
 * relationships.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Note that a load factor of 1.0 is accepted - this is interpreted to mean that only 1 slot need ever remain free (i.e.
 * the actual load factor is (capacity - 1)/capacity). For small capacities, this HashMap automatically forces a load
 * factor of 1.0.
 *
 * The extension method [asMap] produces a thin wrapper around this class which exposes it as Kotlin map which can be
 * used anywhere a Kotlin map is expected. Using this wrapper may incur boxing penalties.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public class Int2ByteHashMap @JvmOverloads constructor(
    capacity: Int = 0,

    /** The default value should be the value that is ideally least likely to occur in the map. */
    private val defaultValue: Byte = Byte.MIN_VALUE,

) : AbstractMutableInt2ByteMap() {


    @JvmOverloads
    public constructor(map: Int2ByteMap, defaultValue: Byte = Byte.MIN_VALUE): this(defaultValue = defaultValue) { putAll(map) }
    @JvmOverloads
    public constructor(map: Map<Int, Byte>, defaultValue: Byte = Byte.MIN_VALUE): this(defaultValue = defaultValue) { putAll(map) }


    private var keysArr = EMPTY_KEY_ARRAY

    private var valuesArr = EMPTY_VALUE_ARRAY


    private var emptyKey = ZERO

    // threshold + size == capacity (rehash once threshold <= 0, if we haven't allocated yet then threshold.inv() is
    // our initial capacity)
    private var threshold = MIN_INITIAL_CAPACITY.inv()

    @get:JvmName("size")
    override var size: Int = 0
        private set

    init {
        ensureCapacity(capacity)
    }

    override fun isDefaultValue(value: Byte): Boolean = value equalsRaw defaultValue

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

    override fun containsKey(key: Int): Boolean = findSlot(key, { true }, { false })

    override fun containsValue(value: Byte): Boolean {
        val keysArr = keysArr
        val valuesArr = valuesArr
        for (slot in keysArr.indices) {
            if (valuesArr[slot] equalsRaw value && keysArr[slot] != emptyKey) return true
        }
        return false
    }

    override fun get(key: Int): Byte = findSlot(key, { slot -> valuesArr[slot] }, { defaultValue })

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    override fun getValue(key: Int): Byte = findSlot(key, { slot -> valuesArr[slot] as Byte }, { throw NoSuchElementException() })

    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    override fun getOrDefault(key: Int, defaultValue: Byte): Byte = findSlot(key, { slot -> valuesArr[slot] as Byte }, { defaultValue })

    override fun put(key: Int, value: Byte): Byte {
        var returnValue = defaultValue
        set(key, {
            value
        }, { slot ->
            returnValue = valuesArr[slot]
            value
        })
        return returnValue
    }

    override fun putIfAbsent(key: Int, value: Byte): Byte {
        set(key, { value }, { slot -> return valuesArr[slot] })
        return defaultValue
    }

    override fun set(key: Int, value: Byte) {
        set(key, { value }, { value })
    }

    override fun replace(key: Int, value: Byte): Byte {
        return findSlot(key, { slot ->
            val oldValue = valuesArr[slot]
            valuesArr[slot] = value
            @Suppress("UNCHECKED_CAST", "USELESS_CAST")
            oldValue as Byte
        }, {
            throw NoSuchElementException()
        })
    }

    override fun remove(key: Int): Byte {
        return findSlot(
            key,
            { slot ->
                val oldValue = valuesArr[slot]
                removeSlot(slot)
                oldValue
            },
            { defaultValue })
    }

    override fun removeKey(key: Int): Byte {
        return findSlot(
            key,
            { slot ->
                val oldValue = valuesArr[slot]
                removeSlot(slot)
                @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                oldValue as Byte
            },
            { throw NoSuchElementException() })
    }

    override fun remove(key: Int, value: Byte): Boolean {
            return findSlot(
                key,
                { slot ->
                    val oldValue = valuesArr[slot]
                    if (oldValue equalsRaw value) {
                        removeSlot(slot)
                        true
                    } else {
                        false
                    }
                },
                { false })
        }

    override fun clear() {
        if (keysArr !== EMPTY_KEY_ARRAY) {
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

    private inline fun set(key: Int, onAdd: () -> Byte, onReplace: (slot: Int) -> Byte) {
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

    private fun shiftAndInsert(keysArr: IntArray, mask: Int, slot: Int, currKey: Int, newKey: Int, newValue: Byte) {
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

    override fun putAll(from: Int2ByteMap) {
        if (from is Int2ByteHashMap && from.size / 2 > size) {
            val oldKeysArr = keysArr
            val oldValuesArr = valuesArr
            val oldEmptyKey = emptyKey

            resetTo(from)
            for (slot in oldKeysArr.indices) {
                val key = oldKeysArr[slot]
                if (key != oldEmptyKey) {
                    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                    putIfAbsent(key, oldValuesArr[slot] as Byte)
                }
            }
        } else {
            ensureCapacity(max(size + (from.size / 2), from.size))
            from.foreach { key, value ->
                set(key, value)
            }
        }
    }

    override fun putAll(from: Map<out Int, Byte>) {
        ensureCapacity(max(size + (from.size / 2), from.size))
        for ((key, value) in from) {
            set(key, value)
        }
    }

    private fun resetTo(from: Int2ByteHashMap) {
        check(!from.isEmpty())

        keysArr = from.keysArr.copyOf()
        valuesArr = from.valuesArr.copyOf()
        emptyKey = from.emptyKey
        size = from.size
        threshold = from.threshold
    }

    private var _keys: IntSet? = null

    @get:JvmName("keys")
    override val keys: IntSet get() {
        return _keys ?:
            object : AbstractIntSet() {
                override val size: Int get() = this@Int2ByteHashMap.size
                override fun contains(element: Int): Boolean = containsKey(element)
                override fun iterator(): IntIterator = KeyIterator()
                override fun traverser(): IntTraverser = Traverser().asKeyTraverser()
            }
            .also { _keys = it }
    }

    private var _values: ByteCollection? = null

    @get:JvmName("values")
    override val values: ByteCollection get() {
        return _values ?:

            object : AbstractByteCollection() {

                override val size: Int get() = this@Int2ByteHashMap.size
                override fun contains(element: Byte): Boolean = containsValue(element)
                override fun iterator(): ByteIterator = ValueIterator()

                override fun traverser(): ByteTraverser = Traverser().asValueTraverser()

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

        if (capacity == 0) {
            if (keysArr !== EMPTY_KEY_ARRAY) {
                keysArr = EMPTY_KEY_ARRAY

                valuesArr = EMPTY_VALUE_ARRAY

                emptyKey = ZERO
            }
            threshold = MIN_INITIAL_CAPACITY.inv()
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val newLength = arraySize(capacity, loadFactor(capacity))
        if (keysArr.size == newLength) return

        val newKeysArr = IntArray(newLength)
        if (emptyKey != ZERO) newKeysArr.fill(emptyKey)

        val newValuesArr = ByteArray(newLength)

        val newMask = newLength - 1

        for (slot in keysArr.indices) {
            val key = keysArr[slot]
            if (key != emptyKey) setRehashing(newKeysArr, newValuesArr, newMask, key, valuesArr[slot])
        }

        keysArr = newKeysArr
        valuesArr = newValuesArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newLength * loadFactor(newLength)).toInt(), newMask) - size
    }

    // we can assume key doesn't exist in array and that we never insert emptyKey

    private fun setRehashing(keysArr: IntArray, valuesArr: ByteArray, mask: Int, key: Int, value: Byte) {

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
            candidate = Random.nextInt()
        }

        val keysArr = keysArr
        for (i in keysArr.indices) {
            if (keysArr[i] == emptyKey) keysArr[i] = candidate
        }
        emptyKey = candidate
    }

    override operator fun iterator(): MutableIterator<MutableInt2ByteMap.MutableEntry> = EntryIterator()

    override fun traverser(): MutableInt2ByteTraverser = Traverser()

    private open inner class SlotIterator {
        private val keysArr = this@Int2ByteHashMap.keysArr
        private val valuesArr = this@Int2ByteHashMap.valuesArr
        private val emptyKey = this@Int2ByteHashMap.emptyKey
        private val mask = keysArr.size - 1

        private var slotsLeft = size
        private var slot = keysArr.size
        private var previousSlot = -1

        init {
            if (slotsLeft > 0) decrement()
        }

        fun hasNext(): Boolean {
            return slotsLeft > 0
        }

        fun nextSlot() {
            if (slotsLeft <= 0) throw NoSuchElementException()
            if (keysArr !== this@Int2ByteHashMap.keysArr) throw ConcurrentModificationException()
            previousSlot = slot
            if (--slotsLeft > 0) decrement()
        }

        fun slot(): Int = previousSlot
        fun key(): Int = keysArr[previousSlot]
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        fun value(): Byte = valuesArr[previousSlot] as Byte

        fun remove() {
            check(previousSlot != -1)
            if (keysArr !== this@Int2ByteHashMap.keysArr) throw ConcurrentModificationException()

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

    private inner class KeyIterator : IntIterator() {
        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()

        override fun nextInt(): Int {
            it.nextSlot()
            return it.key()
        }
    }


    private inner class ValueIterator : ByteIterator() {

        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()


        override fun nextByte(): Byte {

            it.nextSlot()
            return it.value()
        }
    }

    private inner class EntryIterator: SlotIterator(), MutableIterator<MutableInt2ByteMap.MutableEntry> {
        override fun next(): MutableInt2ByteMap.MutableEntry {
            nextSlot()
            return object: MutableInt2ByteMap.AbstractMutableEntry() {
                private val slot = slot()
                override val key: Int = key()
                override var value: Byte = value()
                    set(value) {
                        if (keysArr[slot] != key || valuesArr[slot] notEqualsRaw field) throw ConcurrentModificationException()
                        valuesArr[slot] = value
                        field = value
                    }
            }
        }
    }

    private inner class Traverser : MutableInt2ByteTraverser {
        private val keysArr = this@Int2ByteHashMap.keysArr
        private val valuesArr = this@Int2ByteHashMap.valuesArr
        private val emptyKey = this@Int2ByteHashMap.emptyKey
        private val mask = keysArr.size - 1

        private var slotsLeft = size
        private var slot = keysArr.size
        private var _key = emptyKey

        override val key: Int get() {
            check(_key != emptyKey)
            return _key
        }
        override var value: Byte
            get() {
                check(_key != emptyKey)
                @Suppress("UNCHECKED_CAST", "USELESS_CAST")
                return valuesArr[slot] as Byte
            }
            set(value) {
                check(_key != emptyKey)
                if (keysArr !== this@Int2ByteHashMap.keysArr) throw ConcurrentModificationException()
                valuesArr[slot] = value
            }

        override fun forward(): Boolean {
            if (slotsLeft <= 0) return false
            if (keysArr !== this@Int2ByteHashMap.keysArr) throw ConcurrentModificationException()

            while (true) {
                slot = (slot - 1) and mask
                _key = keysArr[slot]
                if (_key != emptyKey) {
                    --slotsLeft
                    return true
                }
            }
        }

        override fun remove() {
            check(_key != emptyKey)
            if (keysArr !== this@Int2ByteHashMap.keysArr) throw ConcurrentModificationException()

            removeSlot(slot)
            _key = emptyKey
        }
    }

    private fun Int.slot(mask: Int): Int = Hash.mix(this) and mask
    private fun Int.slotDistance(slot: Int, mask: Int): Int = (slot - Hash.mix(this)) and mask

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO = 0.toInt()

        private val EMPTY_KEY_ARRAY = intArrayOf(ZERO)

        private val EMPTY_VALUE_ARRAY = ByteArray(1)


        private const val MIN_INITIAL_CAPACITY = 7

        private const val CACHE_LINE_SIZE = 64 / Int.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2

        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX = 2 * CACHE_LINE_SIZE

        private fun loadFactor(size: Int): Double = if (size <= FORCE_LOAD_FACTOR_MAX) 1.0 else 7.0/8.0

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
