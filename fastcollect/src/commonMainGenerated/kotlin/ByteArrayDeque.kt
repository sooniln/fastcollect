/**
 * Methods for dealing with primitive ArrayDeques.
 */
@file:JvmName("ArrayDeques")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.math.max
import kotlin.math.min

public typealias ByteArrayList = ByteArrayDeque

/**
 * An array based [Deque](https://en.wikipedia.org/wiki/Double-ended_queue) implementation for storing Bytes.
 *
 * This implementation supports amortized O(1) `addFirst/addLast/removeFirst/removeLast` functionality. The
 * [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * The extension method [asList] produces a thin wrapper around this class which exposes it as Kotlin list which can be
 * used anywhere a Kotlin list is expected. Using this wrapper may incur boxing penalties.
 */
public class ByteArrayDeque private constructor(array: ByteArray, size: Int = array.size) : AbstractMutableByteList(), RandomAccess {

    private var head: Int = 0
    private var ring: ByteArray = array
    override var size: Int = size
        private set

    public constructor(capacity: Int = 0) : this(if (capacity == 0) EMPTY_ARRAY else ByteArray(capacity), 0)

    public constructor(elements: ByteCollection) : this(if (elements is ByteList) elements.toByteArray() else elements.toByteArray())

    public constructor(elements: Collection<Byte>) : this(if (elements is ByteList) elements.toByteArray() else elements.toByteArray())

    public constructor(elements: ByteArray, fromIndex:Int = 0, toIndex: Int = elements.size) : this(elements.copyOfRange(fromIndex, toIndex))

    public fun ensureCapacity(capacity: Int) {
        if (capacity > ring.size) grow(capacity)
    }

    private fun grow(capacity: Int) {
        val oldCapacity = ring.size
        val newCapacity = if (oldCapacity > 0 || ring !== EMPTY_ARRAY) {
            growArraySize(oldCapacity, capacity - oldCapacity)
        } else {
            max(DEFAULT_CAPACITY, capacity)
        }

        if (head == 0) {
            ring = ring.copyOf(newCapacity)
        } else {
            ring = copyFromRing(ByteArray(newCapacity))
            head = 0
        }
    }

    private fun copyFromRing(dest: ByteArray): ByteArray {
        check(dest.size >= size)
        val tail = head + size
        if (tail <= ring.size) {
            ring.copyInto(dest, 0, head, tail)
        } else {
            ring.copyInto(dest, 0, head, ring.size)
            ring.copyInto(dest, ring.size - head, 0, tail - ring.size)
        }
        return dest
    }

    public fun trimToSize() {
        if (size < ring.size) {
            ring = if (isEmpty()) EMPTY_ARRAY else copyFromRing(ByteArray(size))
            head = 0
        }
    }

    override fun get(index: Int): Byte {
        return ring[ring.position(head, rangeCheck(index))]
    }

    override fun set(index: Int, element: Byte) {
        ring[ring.position(head, rangeCheck(index))] = element
    }

    override fun addFirst(element: Byte) {
        val newSize = size + 1
        ensureCapacity(newSize)
        head = ring.decrementPosition(head)
        ring[head] = element
        size = newSize
    }

    override fun addLast(element: Byte) {
        val s = size
        val newSize = s + 1
        ensureCapacity(newSize)
        ring[ring.position(head, s)] = element
        size = newSize
    }

    override fun add(index: Int, element: Byte) {
        rangeCheckInclusive(index)
        when (index) {
            size -> addLast(element)
            0 -> addFirst(element)
            else -> addInternal(ring.position(head, index), element)
        }
    }

    // returns 1 if the back half was shifted right and 0 if the front half was shifted left
    private fun addInternal(position: Int, element: Byte): Int {
        val newSize = size + 1
        ensureCapacity(newSize)

        // attempt to shift a minimal number of elements depending on where position falls within the array
        if (ring.index(head, position) < newSize shr 1) {
            // shift elements before position
            val actualPosition = ring.decrementPosition(position)
            val newHead = ring.decrementPosition(head)
            if (actualPosition >= head) {
                // head before position
                ring[newHead] = ring[head]  // first element could possibly roll over to the back of the array
                ring.copyInto(ring, head, head + 1, position)
            } else {
                // head after position
                ring.copyInto(ring, newHead, head, ring.size) // head can't be zero
                ring[ring.size - 1] = ring[0]
                ring.copyInto(ring, 0, 1, position)
            }
            ring[actualPosition] = element
            head = newHead
            size = newSize
            return 0
        } else {
            // shift elements after position
            val tail = ring.position(head, size)
            if (position < tail) {
                // position before tail
                ring.copyInto(ring, position + 1, position, tail)
            } else {
                // position after tail
                val lastIndex = ring.size - 1
                ring.copyInto(ring, 1, 0, tail)
                ring[0] = ring[lastIndex]
                ring.copyInto(ring, ring.incrementPosition(position), position, lastIndex)
            }
            ring[position] = element
            size = newSize
            return 1
        }
    }

    override fun removeFirst(): Byte {
        if (isEmpty()) throw NoSuchElementException()
        val element = ring[head]
        head = ring.incrementPosition(head)
        --size
        return element
    }

    override fun removeLast(): Byte {
        if (isEmpty()) throw NoSuchElementException()
        return ring[ring.position(head, --size)]
    }

    override fun removeAt(index: Int): Byte {
        rangeCheck(index)
        return when (index) {
            lastIndex -> removeLast()
            0 -> removeFirst()
            else -> {
                val position = ring.position(head, index)
                val element = ring[position]
                removeAtInternal(position)
                element
            }
        }
    }

    // returns -1 if the back half was shifted left and 0 if the front half was shifted right
    private fun removeAtInternal(position: Int): Int {
        // attempt to shift a minimal number of elements depending on where position falls within the array
        if (ring.index(head, position) < size shr 1) {
            // shift front half right, then advance head
            if (position > head) {
                ring.copyInto(ring, head + 1, head, position)
            } else if (position < head) {
                // wrapped: position is in the lower part of the ring
                ring.copyInto(ring, 1, 0, position)
                ring[0] = ring[ring.size - 1]
                ring.copyInto(ring, head + 1, head, ring.size - 1)
            }
            // else: position == head (first element), no copy needed
            head = ring.incrementPosition(head)
            --size
            return 0
        } else {
            // shift back half left
            val tail = ring.position(head, size - 1)
            if (position < tail) {
                ring.copyInto(ring, position, position + 1, tail + 1)
            } else if (position > tail) {
                // wrapped: position is in the upper part of the ring
                ring.copyInto(ring, position, position + 1, ring.size)
                ring[ring.size - 1] = ring[0]
                ring.copyInto(ring, 0, 1, tail + 1)
            }
            // else: position == tail (last element), no copy needed
            --size
            return -1
        }
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        // TODO: would array copy operations be more efficient?
        require(fromIndex <= toIndex)
        rangeCheckInclusive(fromIndex)
        rangeCheckInclusive(toIndex)
        if (fromIndex == toIndex) return

        val removed = toIndex - fromIndex
        if (fromIndex <= size - toIndex) {
            // Shift [0, fromIndex) right by `removed`, then advance head.
            for (i in fromIndex - 1 downTo 0) {
                ring[ring.position(head, i + removed)] = ring[ring.position(head, i)]
            }
            head = ring.positiveMod(head + removed)
        } else {
            // Shift [toIndex, size) left by `removed`.
            for (i in toIndex until size) {
                ring[ring.position(head, i - removed)] = ring[ring.position(head, i)]
            }
        }
        size -= removed
    }

    override fun clear() {
        head = 0
        size = 0
    }

    override fun indexOf(element: Byte): Int {
        val tail = head + size
        return if (tail <= ring.size) indexOfContinuous(tail, element) else indexOfDiscrete(tail, element)
    }

    private fun indexOfContinuous(tail: Int, element: Byte): Int {
        for (i in head..<tail) {
            if (ring[i] equalsBoxed element) return i - head
        }
        return -1
    }

    private fun indexOfDiscrete(tail: Int, element: Byte): Int {
        for (i in head..<ring.size) {
            if (ring[i] equalsBoxed element) return i - head
        }
        for (i in 0..<tail-ring.size) {
            if (ring[i] equalsBoxed element) return i + ring.size - head
        }
        return -1
    }

    override fun lastIndexOf(element: Byte): Int {
        val tail = head + size - 1
        return if (tail < ring.size) {
            lastIndexOfContinuous(tail, element)
        } else {
            lastIndexOfDiscrete(tail, element)
        }
    }

    private fun lastIndexOfContinuous(tail: Int, element: Byte): Int {
        // kotlin produces inefficient bytecode for downTo for some reason, so we use a manual loop
        val head = head
        var i = tail
        while (i >= head) {
            if (ring[i] equalsBoxed element) return i - head
            --i
        }
        return -1
    }

    private fun lastIndexOfDiscrete(tail: Int, element: Byte): Int {
        // kotlin produces inefficient bytecode for downTo for some reason, so we use a manual loop
        val head = head
        var i = tail - ring.size
        while (i >= 0) {
            if (ring[i] equalsBoxed element) return i + ring.size - head
            --i
        }
        i = ring.size - 1
        while (i >= head) {
            if (ring[i] equalsBoxed element) return i - head
            --i
        }
        return -1
    }

    public fun addAll(elements: ByteArrayDeque): Boolean {
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        val elementsTail = elements.head + elements.size
        if (elementsTail <= elements.ring.size) {
            addToRing(elements.ring, elements.head, elementsTail)
        } else {
            addToRing(elements.ring, elements.head, elements.ring.size)
            addToRing(elements.ring, 0, elementsTail - elements.ring.size)
        }
        return true
    }

    private fun addToRing(src: ByteArray, fromIndex: Int, toIndex: Int) {
        val srcLength = toIndex - fromIndex
        check(srcLength >= 0 && srcLength <= ring.size - size)

        val tail = head + size
        if (tail <= ring.size) {
            val intermediateIndex = min(toIndex, fromIndex + ring.size - tail)
            src.copyInto(ring, tail, fromIndex, intermediateIndex)
            if (intermediateIndex != toIndex) {
                src.copyInto(ring, 0, intermediateIndex, toIndex)
            }
        } else {
            src.copyInto(ring, tail - ring.size, fromIndex, toIndex)
        }
        size += srcLength
    }

    override fun addAll(elements: ByteCollection): Boolean {
        if (elements is ByteArrayDeque) return addAll(elements)
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    override fun addAll(elements: Collection<Byte>): Boolean {
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    public override fun removeAll(elements: Collection<Byte>): Boolean {
        return filterInPlace { e -> elements.contains(e) }
    }

    public override fun retainAll(elements: Collection<Byte>): Boolean {
        return filterInPlace { e -> !elements.contains(e) }
    }

    @OptIn(ExperimentalContracts::class)
    internal inline fun filterInPlace(removePredicate: (Byte) -> Boolean): Boolean {
        contract {
            callsInPlace(removePredicate, InvocationKind.UNKNOWN)
        }

        var position = head
        var remaining = size
        while (true) {
            if (remaining == 0) {
                return false
            } else if (removePredicate(ring[position])) {
                break
            }
            position = ring.incrementPosition(position)
            --remaining
        }

        var insertionPosition = position
        position = ring.incrementPosition(position)
        --remaining
        while (remaining > 0) {
            val element = ring[position]
            position = ring.incrementPosition(position)
            --remaining
            if (!removePredicate(element)) {
                ring[insertionPosition] = element
                insertionPosition = ring.incrementPosition(insertionPosition)
            }
        }
        size = insertionPosition - head
        if (size < 0) {
            size += ring.size
        }
        return true
    }

    override fun sort() {
        makeContinuousUnordered()
        ring.sort(head, head + size)
    }

    override fun sortDescending() {
        makeContinuousUnordered()
        ring.sortDescending(head, head + size)
    }

    private fun makeContinuousUnordered() {
        val tail = head + size
        if (tail > ring.size) {
            val end = tail - ring.size
            if (ring.size - head > end) {
                head -= end
                ring.copyInto(ring, head, 0, end)
            } else {
                ring.copyInto(ring, end, head, ring.size)
                head = 0
            }
        }
    }

    override fun fill(element: Byte) {
        ring.fill(element, 0, size)
        head = 0
    }

    override fun reverse() {
        val midPoint = size / 2
        if (midPoint < 1) return
        var i = head
        var j = ring.position(head, size - 1)
        repeat(midPoint) {
            val tmp = ring[i]
            ring[i] = ring[j]
            ring[j] = tmp

            i = ring.incrementPosition(i)
            j = ring.decrementPosition(j)
        }
    }

    override fun toByteArray(): ByteArray {
        return copyFromRing(ByteArray(size))
    }

    override fun iterator(): MutableByteIterator {
        val tail = head + size
        return if (tail > ring.size) {
            DiscreteIterator()
        } else {
            ContinuousIterator(tail)
        }
    }

    override fun traverser(): MutableByteTraverser = ListTraverser(0)
    override fun traverser(position: Int): MutableByteListTraverser = ListTraverser(position)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ByteList) return false

        if (size != other.size) return false
        if (other is RandomAccess) {
            for (i in indices) {
                if (!(ring[ring.position(head, i)] equalsBoxed other[i])) return false
            }
        } else {
            val it = other.iterator()
            var i = 0
            while (it.hasNext()) {
                if (!(it.nextByte() equalsBoxed this[i++])) return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var hashCode = 1
        if (!isEmpty()) {
            var position = head
            val end = ring.position(head, size)
            do {
                hashCode = 31 * hashCode + ring[position].hashCode()
                position = ring.incrementPosition(position)
            } while (position != end)
        }
        return hashCode
    }

    private inner class ContinuousIterator(private var tail: Int) : MutableByteIterator() {
        private val ring = this@ByteArrayDeque.ring

        init {
            check(tail <= ring.size)
        }

        private var position = head
        private var previousPosition = -1

        override fun hasNext() = position < tail

        override fun nextByte(): Byte {
            if (!hasNext()) throw NoSuchElementException()
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()

            previousPosition = position++
            return ring[previousPosition]
        }

        override fun remove() {
            check(previousPosition >= 0)
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()

            val d = removeAtInternal(previousPosition)
            tail += d
            position = ring.negativeMod(position + d)
            previousPosition = -1
        }
    }

    private inner class DiscreteIterator : MutableByteIterator() {
        private val ring = this@ByteArrayDeque.ring

        private var remaining = size
        private var position = head
        private var previousPosition = -1

        override fun hasNext() = remaining > 0

        override fun nextByte(): Byte {
            if (!hasNext()) throw NoSuchElementException()
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()

            --remaining
            previousPosition = position
            position = ring.incrementPosition(position)
            return ring[previousPosition]
        }

        override fun remove() {
            check(previousPosition >= 0)
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()

            val d = removeAtInternal(previousPosition)
            position = ring.negativeMod(position + d)
            previousPosition = -1
        }
    }

    private inner class ListTraverser(position: Int) : MutableByteListTraverser {
        private val ring = this@ByteArrayDeque.ring
        private var head: Int = this@ByteArrayDeque.head

        init {
            check(position in 0..size)
        }

        private var cursor = if (position == 0) head - 1 else ring.position(head, position - 1)
        private var ringPosition = if (position == 0) -1 else cursor

        override var position: Int = position
            private set

        override val value: Byte get() {
            check(ringPosition >= 0)
            return ring[ringPosition]
        }

        override fun forward(): Boolean {
            if (position >= size) {
                return false
            }

            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()
            cursor = ring.incrementPosition(cursor)
            ringPosition = cursor
            ++position
            return true
        }

        override fun backward(): Boolean {
            if (position <= 0) {
                return false
            }

            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()
            ringPosition = cursor
            cursor = ring.decrementPosition(cursor)
            --position
            return true
        }

        override fun remove() {
            check(ringPosition >= 0)
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()

            val index = ring.index(head, ringPosition)
            val d = removeAtInternal(ringPosition)
            head = ring.positiveMod(head + d + 1)
            position = index
            ringPosition = -1
            cursor = if (index == 0) head - 1 else ring.position(head, index - 1)
        }

        override fun set(value: Byte) {
            check(ringPosition >= 0)
            ring[ringPosition] = value
        }

        override fun insert(value: Byte) {
            val d = addInternal(ring.position(head, position), value)
            head = ring.negativeMod(head + d - 1)
            cursor = ring.position(head, position++)
            ringPosition = cursor
        }
    }

    internal companion object {
        private val EMPTY_ARRAY = ByteArray(0)
        private const val DEFAULT_CAPACITY = 8

        private fun ByteArray.positiveMod(position: Int): Int = if (position < size) position else position - size

        private fun ByteArray.negativeMod(position: Int): Int = if (position < 0) position + size else position

        private fun ByteArray.position(head: Int, index: Int): Int = positiveMod(head + index)

        private fun ByteArray.index(head: Int, position: Int): Int = negativeMod(position - head)

        private fun ByteArray.incrementPosition(position: Int): Int {
            val next = position + 1
            return if (next == size) 0 else next
        }

        private fun ByteArray.decrementPosition(position: Int): Int = if (position == 0) size - 1 else position - 1

        fun wrap(array: ByteArray): ByteArrayDeque = ByteArrayDeque(array)
    }
}


public fun ByteArrayDeque.removeAll(predicate: BytePredicate): Boolean = filterInPlace { predicate.test(it) }
public fun ByteArrayDeque.retainAll(predicate: BytePredicate): Boolean = filterInPlace { !predicate.test(it) }

