package io.github.sooniln.fastcollect

import kotlin.jvm.JvmOverloads
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

/**
 * A [HashMap](https://en.wikipedia.org/wiki/Hash_table) implementation for storing Int to Int
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
public class Int2IntHashMap @JvmOverloads constructor(
    capacity: Int = 0,
    /** The default value should be the value that is ideally least likely to occur in the map. */
    private val defaultValue: Int = Int.MIN_VALUE,
) : AbstractMutableInt2IntMap() {

    public constructor(map: Int2IntMap): this() { putAll(map) }

    public constructor(map: Map<Int, Int>): this() { putAll(map) }

    private var kvArr = EMPTY_ARRAY

    private var emptyEntry = ZERO_ENTRY

    // threshold + size == capacity (rehash once threshold <= 0, if we haven't allocated yet then threshold.inv() is
    // our initial capacity)
    private var threshold = MIN_INITIAL_CAPACITY.inv()

    override var size: Int = 0
        private set

    init {
        ensureCapacity(capacity)
    }

    override fun isDefaultValue(value: Int): Boolean = value equalsRaw defaultValue

    /**
     * Ensures that the map can hold at least given number of key/value pairs without any further resizing of the
     * backing array.
     */
    public fun ensureCapacity(capacity: Int) {
        require(capacity >= 0) { "Capacity must be >= 0" }
        if (kvArr === EMPTY_ARRAY) {
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

    override fun containsKey(key: Int): Boolean = findSlot(key, { _, _ -> true }, { false })

    override fun containsValue(value: Int): Boolean {
        val kvArr = kvArr
        for (slot in kvArr.indices) {
            val entry = kvArr[slot]
            if (entry != emptyEntry && entry.value() equalsRaw value) return true
        }
        return false
    }

    override fun get(key: Int): Int = findSlot(key, { _, entry -> entry.value() }, { defaultValue })

    override fun getValue(key: Int): Int = findSlot(key, { _, entry -> entry.value() }, { throw NoSuchElementException() })

    override fun getOrDefault(key: Int, defaultValue: Int): Int = findSlot(key, { _, entry -> entry.value() }, { defaultValue })

    override fun put(key: Int, value: Int): Int {
        var returnValue = defaultValue
        set(key, {
            value
        }, { entry ->
            returnValue = entry.value()
            value
        })
        return returnValue
    }

    override fun putIfAbsent(key: Int, value: Int): Int {
        set(key, { value }, { entry -> return entry.value() })
        return defaultValue
    }

    override fun set(key: Int, value: Int) {
        set(key, { value }, { value })
    }

    override fun replace(key: Int, value: Int): Int {
        return findSlot(key, { slot, entry ->
            val oldValue = entry.value()
            kvArr[slot] = arrayEntry(entry.key(), value)
            oldValue
        }, {
            throw NoSuchElementException()
        })
    }

    override fun remove(key: Int): Int {
        return findSlot(
            key,
            { slot, entry ->
                val oldValue = entry.value()
                removeSlot(slot)
                oldValue
            },
            { defaultValue })
    }

    override fun removeKey(key: Int): Int {
        return findSlot(
            key,
            { slot, entry ->
                val oldValue = entry.value()
                removeSlot(slot)
                oldValue
            },
            { throw NoSuchElementException() })
    }

    override fun remove(key: Int, value: Int): Boolean {
        return findSlot(
            key,
            { slot, entry ->
                if (entry.value() equalsRaw value) {
                    removeSlot(slot)
                    true
                } else {
                    false
                }
            },
            { false })
    }

    override fun clear() {
        if (kvArr !== EMPTY_ARRAY) {
            threshold += size
            kvArr.fill(emptyEntry)
        }
        size = 0
    }

    private inline fun <T> findSlot(key: Int, onFind: (slot: Int, entry: Long) -> T, onFail: () -> T): T {
        val kvArr = kvArr
        val mask = kvArr.size - 1

        var slot = key.slot(mask)
        var currEntry = kvArr[slot]

        var slotDistance = 0
        while (true) {
            // checking whether the current slot distance is higher than our search distance allows us to early exit the
            // search loop - but the cost of checking is non-trivial. as a compromise between GetHit and GetMiss
            // performance we only check once every half cache line.
            var i = 0
            do {
                if (currEntry == emptyEntry) {
                    return onFail()
                } else if (currEntry.key() == key) {
                    return onFind(slot, currEntry)
                }

                slot = (slot + 1) and mask
                currEntry = kvArr[slot]
            } while (++i < HALF_CACHE_LINE_SIZE)

            slotDistance += HALF_CACHE_LINE_SIZE
            if (currEntry.key().slotDistance(slot, mask) < slotDistance) {
                return onFail()
            }
        }
    }

    private inline fun set(key: Int, onAdd: () -> Int, onReplace: (entry: Long) -> Int) {
        if (threshold <= 0) increaseCapacity()
        if (key == emptyEntry.key()) changeEmptyEntry()

        val kvArr = kvArr
        val mask = kvArr.size - 1

        var slot = key.slot(mask)
        var distance = 0
        while (true) {
            val currEntry = kvArr[slot]
            if (currEntry.key() == key) {
                kvArr[slot] = arrayEntry(key, onReplace(currEntry))
                return
            } else if (currEntry == emptyEntry || distance > currEntry.key().slotDistance(slot, mask)) {
                shiftAndInsert(kvArr, mask, slot, currEntry, arrayEntry(key, onAdd()))
                return
            }

            slot = (slot + 1) and mask
            distance += 1
        }
    }

    private fun shiftAndInsert(kvArr: LongArray, mask: Int, slot: Int, currEntry: Long, newEntry: Long) {
        var nextSlot = slot
        var currEntry = currEntry
        var newEntry = newEntry

        while (currEntry != emptyEntry) {
            kvArr[nextSlot] = newEntry
            newEntry = currEntry
            nextSlot = (nextSlot + 1) and mask
            currEntry = kvArr[nextSlot]
        }

        kvArr[nextSlot] = newEntry
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
        val kvArr = kvArr
        val mask = kvArr.size - 1

        var currSlot = slot
        var nextSlot = (currSlot + 1) and mask
        var nextEntry = kvArr[nextSlot]
        while (nextEntry != emptyEntry && nextEntry.key().slotDistance(nextSlot, mask) > 0) {
            kvArr[currSlot] = nextEntry

            currSlot = nextSlot
            nextSlot = (nextSlot + 1) and mask
            nextEntry = kvArr[nextSlot]
        }
        kvArr[currSlot] = emptyEntry
        threshold += 1
        size -= 1
    }

    override fun putAll(from: Int2IntMap) {
        if (from is Int2IntHashMap && from.size / 2 > size) {
            val oldKvArr = kvArr
            val oldEmptyEntry = emptyEntry

            resetTo(from)
            for (entry in oldKvArr) {
                if (entry != oldEmptyEntry) {
                    putIfAbsent(entry.key(), entry.value())
                }
            }
            trimToSize()
        } else {
            ensureCapacity(max(size + (from.size / 2), from.size))
            from.foreach { key, value ->
                set(key, value)
            }
        }
    }

    override fun putAll(from: Map<out Int, Int>) {
        ensureCapacity(max(size + (from.size / 2), from.size))
        for ((key, value) in from) {
            set(key, value)
        }
    }

    private fun resetTo(from: Int2IntHashMap) {
        check(!from.isEmpty())

        kvArr = from.kvArr.copyOf()
        emptyEntry = from.emptyEntry
        size = from.size
        threshold = from.threshold
    }

    private var _keys: IntSet? = null
    override val keys: IntSet get() {
        return _keys ?:
            object : AbstractIntSet() {
                override val size: Int get() = this@Int2IntHashMap.size
                override fun contains(element: Int): Boolean = containsKey(element)
                override fun iterator(): IntIterator = KeyIterator()
                override fun traverser(): IntTraverser = Traverser().asKeyTraverser()
            }
            .also { _keys = it }
    }

    private var _values: IntCollection? = null
    override val values: IntCollection get() {
        return _values ?:
            object : AbstractIntCollection() {
                override val size: Int get() = this@Int2IntHashMap.size
                override fun contains(element: Int): Boolean = containsValue(element)
                override fun iterator(): IntIterator = ValueIterator()
                override fun traverser(): IntTraverser = Traverser().asValueTraverser()
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
            if (kvArr !== EMPTY_ARRAY) {
                kvArr = EMPTY_ARRAY
                emptyEntry = ZERO_ENTRY
                threshold = MIN_INITIAL_CAPACITY.inv()
            }
            return
        }

        // for small capacities we force loadFactor to 1.0 to save memory (small array scans are likely to be fast)
        val actualLoadFactor = if (capacity <= FORCE_LOAD_FACTOR_MAX) 1.0 else 7.0/8.0

        val newLength = arraySize(capacity, actualLoadFactor)
        if (kvArr.size == newLength) return

        val newKvArr = LongArray(newLength)
        if (emptyEntry != ZERO_ENTRY) newKvArr.fill(emptyEntry)
        val newMask = newLength - 1

        for (slot in kvArr.indices) {
            val entry = kvArr[slot]
            if (entry != emptyEntry) setRehashing(newKvArr, newMask, entry)
        }

        kvArr = newKvArr

        // threshold must always maintain the invariant of at least 1 slot being open
        threshold = min((newLength * actualLoadFactor).toInt(), newMask) - size
    }

    // we can assume key doesn't exist in array and that we never insert emptyEntry
    private fun setRehashing(kvArr: LongArray, mask: Int, entry: Long) {
        var slot = entry.key().slot(mask)
        var distance = 0
        while (true) {
            var currEntry = kvArr[slot]
            if (currEntry == emptyEntry) {
                kvArr[slot] = entry
                return
            } else if (distance > currEntry.key().slotDistance(slot, mask)) {
                var newEntry = entry
                do {
                    kvArr[slot] = newEntry
                    newEntry = currEntry
                    slot = (slot + 1) and mask
                    currEntry = kvArr[slot]
                } while (currEntry != emptyEntry)

                kvArr[slot] = newEntry
                return
            }

            slot = (slot + 1) and mask
            distance += 1
        }
    }

    // changes emptyEntry to a value not currently in the map, rewriting all empty slots
    private fun changeEmptyEntry() {
        var candidate = ZERO
        while (candidate == emptyEntry.key() || containsKey(candidate)) {
            candidate = Random.nextInt()
        }

        val oldEmptyEntry = emptyEntry
        emptyEntry = arrayEntry(candidate, 0)

        val kvArr = kvArr
        for (i in kvArr.indices) {
            if (kvArr[i] == oldEmptyEntry) kvArr[i] = emptyEntry
        }
    }

    override operator fun iterator(): MutableIterator<MutableInt2IntMap.MutableEntry> = EntryIterator()

    override fun traverser(): MutableInt2IntTraverser = Traverser()

    private open inner class SlotIterator {
        private val kvArr = this@Int2IntHashMap.kvArr
        private var emptyEntry = this@Int2IntHashMap.emptyEntry
        private val mask = kvArr.size - 1

        private var slotsLeft = size
        private var slot = kvArr.size
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

        fun key(): Int = kvArr[previousSlot].key()
        fun value(): Int = kvArr[previousSlot].value()

        fun remove() {
            check(previousSlot != -1)
            if (kvArr !== this@Int2IntHashMap.kvArr) throw ConcurrentModificationException()

            removeSlot(previousSlot)
            previousSlot = -1

            // if removal wrapped all the way around to our next slot then we need to adjust
            if (kvArr[slot] == emptyEntry) {
                slot = (slot - 1) and mask
            }
        }

        private fun decrement() {
            do {
                slot = (slot - 1) and mask
            } while (kvArr[slot] == emptyEntry)
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

    private inner class ValueIterator : IntIterator() {
        private val it = SlotIterator()

        override fun hasNext(): Boolean = it.hasNext()

        override fun nextInt(): Int {
            it.nextSlot()
            return it.value()
        }
    }

    private inner class EntryIterator: SlotIterator(), MutableIterator<MutableInt2IntMap.MutableEntry> {
        override fun next(): MutableInt2IntMap.MutableEntry {
            nextSlot()
            return object: MutableInt2IntMap.AbstractMutableEntry() {
                override val key: Int = key()
                override var value: Int = value()
                    set(value) {
                        if (get(key) notEqualsRaw field) throw ConcurrentModificationException()
                        set(key, value)
                        field = value
                    }
            }
        }
    }

    private inner class Traverser : MutableInt2IntTraverser {
        private val kvArr = this@Int2IntHashMap.kvArr
        private val emptyEntry = this@Int2IntHashMap.emptyEntry
        private val mask = kvArr.size - 1

        private var slotsLeft = size
        private var slot = kvArr.size
        private var entry = emptyEntry

        override val key: Int get() {
            check(entry != emptyEntry)
            return entry.key()
        }
        override var value: Int
            get() {
                check(entry != emptyEntry)
                return entry.value()
            }
            set(value) {
                check(entry != emptyEntry)
                if (kvArr !== this@Int2IntHashMap.kvArr) throw ConcurrentModificationException()
                entry = arrayEntry(entry.key(), value)
                kvArr[slot] = entry
            }

        override fun forward(): Boolean {
            if (slotsLeft <= 0) {
                return false
            }
            if (kvArr !== this@Int2IntHashMap.kvArr) throw ConcurrentModificationException()

            while (true) {
                slot = (slot - 1) and mask
                entry = kvArr[slot]
                if (entry != emptyEntry) {
                    --slotsLeft
                    return true
                }
            }
        }

        override fun remove() {
            check(entry != emptyEntry)
            if (kvArr !== this@Int2IntHashMap.kvArr) throw ConcurrentModificationException()

            removeSlot(slot)
            entry = emptyEntry
        }
    }

    private companion object {
        // the value of a field in an uninitialized primitive array
        @Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
        private const val ZERO = 0.toInt()
        private const val ZERO_ENTRY = 0.toLong()

        private val EMPTY_ARRAY = longArrayOf(ZERO_ENTRY)

        private const val MIN_INITIAL_CAPACITY = 7 // may not be zero

        private const val CACHE_LINE_SIZE = 64 / Long.SIZE_BYTES
        private const val HALF_CACHE_LINE_SIZE = CACHE_LINE_SIZE / 2

        // we force the load factor to 1.0 up to the size of two cache lines
        private const val FORCE_LOAD_FACTOR_MAX = 2 * CACHE_LINE_SIZE

        private fun Int.slot(mask: Int): Int = Hash.mix(this) and mask
        private fun Int.slotDistance(slot: Int, mask: Int): Int = (slot - Hash.mix(this)) and mask

        private fun Long.key(): Int = toInt()
        private fun Long.value(): Int = (this shr (8 * Int.SIZE_BYTES)).toInt()
        private fun arrayEntry(key: Int, value: Int): Long = (value.toLong() shl (8 * Int.SIZE_BYTES)) or (key.toUInt().toLong())

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
