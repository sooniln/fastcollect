package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.ArrayUtils
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max
import kotlin.math.min

public typealias IntArrayList = IntArrayDeque

/**
 * An array based [Deque](https://en.wikipedia.org/wiki/Double-ended_queue) implementation for storing Ints. Can be
 * used in place of the Kotlin standard library [ArrayList] and [ArrayDeque] implementations to improve performance and
 * memory usage. Has the same API contracts as the standard library [ArrayList] and [ArrayDeque] unless noted otherwise.
 *
 * This implementation supports amortized O(1) `addFirst/addLast/removeFirst/removeLast` functionality. The
 * [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 */
public class IntArrayDeque private constructor(array: IntArray, size: Int = array.size) : AbstractMutableIntList(), RandomAccess {

    private var head: Int = 0
    private var ring: IntArray = array
    override var size: Int = size
        private set

    public constructor(capacity: Int = 0) : this(if (capacity == 0) EMPTY_ARRAY else IntArray(capacity), 0)

    public constructor(elements: IntCollection) : this(if (elements is IntList) elements.toIntArray() else elements.toIntArray())

    public constructor(elements: Collection<Int>) : this(if (elements is IntList) elements.toIntArray() else elements.toIntArray())

    public constructor(elements: IntArray, fromIndex:Int = 0, toIndex: Int = elements.size) : this(elements.copyOfRange(fromIndex, toIndex))

    public fun ensureCapacity(capacity: Int) {
        if (capacity > ring.size) grow(capacity)
    }

    private fun grow(capacity: Int) {
        val oldCapacity = ring.size
        val newCapacity = if (oldCapacity > 0 || ring !== EMPTY_ARRAY) {
            ArrayUtils.growArraySize(oldCapacity, capacity - oldCapacity)
        } else {
            max(DEFAULT_CAPACITY, capacity)
        }

        if (head == 0) {
            ring = ring.copyOf(newCapacity)
        } else {
            ring = copyFromRing(IntArray(newCapacity))
            head = 0
        }
    }

    private fun copyFromRing(dest: IntArray): IntArray {
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
            ring = if (isEmpty()) EMPTY_ARRAY else copyFromRing(IntArray(size))
            head = 0
        }
    }

    override fun get(index: Int): Int {
        return ring[ring.position(rangeCheck(index))]
    }

    override fun set(index: Int, element: Int) {
        ring[ring.position(rangeCheck(index))] = element
    }

    override fun addFirst(element: Int) {
        val newSize = size + 1
        ensureCapacity(newSize)
        head = ring.decrementPosition(head)
        ring[head] = element
        size = newSize
    }

    override fun addLast(element: Int) {
        val s = size
        val newSize = s + 1
        ensureCapacity(newSize)
        ring[ring.position(s)] = element
        size = newSize
    }

    override fun add(index: Int, element: Int) {
        rangeCheckInclusive(index)
        when (index) {
            size -> addLast(element)
            0 -> addFirst(element)
            else -> addMiddle(index, element)
        }
    }

    private fun addMiddle(index: Int, element: Int) {
        val newSize = size + 1
        ensureCapacity(newSize)

        // attempt to shift a minimal number of elements depending on where position falls within the array
        val position = ring.position(index)
        if (index < newSize shr 1) {
            // shift elements before position
            val actualPosition = ring.decrementPosition(position)
            val newHead = ring.decrementPosition(head)
            if (actualPosition >= head) {
                // head before position
                ring[newHead] = ring[head]  // first element could possibly roll over to the back of the array
                ring.copyInto(ring, head, head + 1, head + index)
            } else {
                // head after position
                ring.copyInto(ring, newHead, head, ring.size) // head can't be zero
                ring[ring.size - 1] = ring[0]
                ring.copyInto(ring, 0, 1, position)
            }
            ring[actualPosition] = element
            head = newHead
        } else {
            // shift elements after position
            val tail = ring.position(size)
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
        }
        size = newSize
    }

    override fun removeFirst(): Int {
        if (isEmpty()) throw NoSuchElementException()
        val element = ring[head]
        head = ring.incrementPosition(head)
        --size
        return element
    }

    override fun removeLast(): Int {
        if (isEmpty()) throw NoSuchElementException()
        return ring[ring.position(--size)]
    }

    override fun removeAt(index: Int): Int {
        rangeCheck(index)

        val position = ring.position(index)
        val element = ring[position]
        removeAtInternal(position)
        return element
    }

    // returns -1 if the back half was shifted left and 0 if the front half was shifted right
    private fun removeAtInternal(position: Int): Int {
        // attempt to shift a minimal number of elements depending on where position falls within the array
        if (ring.index(position) < size shr 1) {
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
            val tail = ring.position(size - 1)
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
                ring[ring.position(i + removed)] = ring[ring.position(i)]
            }
            head = ring.positiveMod(head + removed)
        } else {
            // Shift [toIndex, size) left by `removed`.
            for (i in toIndex until size) {
                ring[ring.position(i - removed)] = ring[ring.position(i)]
            }
        }
        size -= removed
    }

    override fun clear() {
        head = 0
        size = 0
    }

    override fun indexOf(element: Int): Int {
        val tail = head + size
        return if (tail <= ring.size) indexOfContinuous(tail, element) else indexOfDiscrete(tail, element)
    }

    private fun indexOfContinuous(tail: Int, element: Int): Int {
        for (i in head..<tail) {
            if (ring[i] == element) return i - head
        }
        return -1
    }

    private fun indexOfDiscrete(tail: Int, element: Int): Int {
        for (i in head..<ring.size) {
            if (ring[i] == element) return i - head
        }
        for (i in 0..<tail-ring.size) {
            if (ring[i] == element) return i + ring.size - head
        }
        return -1
    }

    override fun lastIndexOf(element: Int): Int {
        val tail = head + size - 1
        return if (tail < ring.size) {
            lastIndexOfContinuous(tail, element)
        } else {
            lastIndexOfDiscrete(tail, element)
        }
    }

    private fun lastIndexOfContinuous(tail: Int, element: Int): Int {
        // kotlin produces inefficient bytecode for downTo for some reason, so we use a manual loop
        val head = head
        var i = tail
        while (i >= head) {
            if (ring[i] == element) return i - head
            --i
        }
        return -1
    }

    private fun lastIndexOfDiscrete(tail: Int, element: Int): Int {
        // kotlin produces inefficient bytecode for downTo for some reason, so we use a manual loop
        val head = head
        var i = tail - ring.size
        while (i >= 0) {
            if (ring[i] == element) return i + ring.size - head
            --i
        }
        i = ring.size - 1
        while (i >= head) {
            if (ring[i] == element) return i - head
            --i
        }
        return -1
    }

    public fun addAll(elements: IntArrayDeque): Boolean {
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

    private fun addToRing(src: IntArray, fromIndex: Int, toIndex: Int) {
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

    override fun addAll(elements: IntCollection): Boolean {
        if (elements is IntArrayDeque) return addAll(elements)
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        if (elements is IntCollection) return addAll(elements)
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    public override fun removeAll(elements: Collection<Int>): Boolean {
        return filterInPlace { e -> elements.contains(e) }
    }

    public override fun retainAll(elements: Collection<Int>): Boolean {
        return filterInPlace { e -> !elements.contains(e) }
    }

    @OptIn(ExperimentalContracts::class)
    internal inline fun filterInPlace(removePredicate: (Int) -> Boolean): Boolean {
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
                head = head - end
                ring.copyInto(ring, head, 0, end)
            } else {
                ring.copyInto(ring, end, head, ring.size)
                head = 0
            }
        }
    }

    override fun fill(element: Int) {
        ring.fill(element, 0, size)
        head = 0
    }

    override fun reverse() {
        val midPoint = size / 2
        if (midPoint < 1) return
        var i = head
        var j = ring.position(size - 1)
        repeat(midPoint) {
            val tmp = ring[i]
            ring[i] = ring[j]
            ring[j] = tmp

            i = ring.incrementPosition(i)
            j = ring.decrementPosition(j)
        }
    }

    override fun toIntArray(): IntArray {
        return copyFromRing(IntArray(size))
    }

    override fun iterator(): MutableIntIterator {
        val tail = head + size
        return if (tail > ring.size) {
            DiscreteIterator()
        } else {
            ContinuousIterator(tail)
        }
    }

    override fun foreach(action: IntConsumer) {
        var remaining = size
        var position = head
        while (remaining > 0) {
            action.accept(ring[position])
            position = ring.incrementPosition(position)
            --remaining
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntList) return false

        if (size != other.size) return false
        if (other is RandomAccess) {
            for (i in indices) {
                if (ring[ring.position(i)] != other[i]) return false
            }
        } else {
            val it = other.iterator()
            var i = 0
            while (it.hasNext()) {
                if (it.nextInt() != this[i++]) return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var hashCode = 1
        if (!isEmpty()) {
            var position = head
            val end = ring.position(size)
            do {
                hashCode = 31 * hashCode + ring[position].hashCode()
                position = ring.incrementPosition(position)
            } while (position != end)
        }
        return hashCode
    }

    private inner class ContinuousIterator(private var tail: Int) : MutableIntIterator() {
        private val ring = this@IntArrayDeque.ring

        private var position = head
        private var previousPosition = -1

        init {
            check(tail <= ring.size)
        }

        override fun hasNext() = position < tail

        override fun nextInt(): Int {
            if (!hasNext()) throw NoSuchElementException()

            previousPosition = position++
            return ring[previousPosition]
        }

        override fun remove() {
            if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()
            check(previousPosition != -1)

            val d = removeAtInternal(previousPosition)
            tail = tail + d
            position = ring.negativeMod(position + d)
            previousPosition = -1
        }
    }

    private inner class DiscreteIterator : MutableIntIterator() {
        private val ring = this@IntArrayDeque.ring

        private var remaining = size
        private var position = head
        private var previousPosition = -1

        override fun hasNext() = remaining > 0

        override fun nextInt(): Int {
            if (!hasNext()) throw NoSuchElementException()

            --remaining
            previousPosition = position
            position = ring.incrementPosition(position)
            return ring[previousPosition]
        }

        override fun remove() {
            if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()
            check(previousPosition != -1)

            val d = removeAtInternal(previousPosition)
            position = ring.negativeMod(position + d)
            previousPosition = -1
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.positiveMod(position: Int): Int = if (position < size) position else position - size

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.negativeMod(position: Int): Int = if (position < 0) position + size else position

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.position(index: Int): Int = positiveMod(head + index)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.index(position: Int): Int = negativeMod(position - head)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.incrementPosition(position: Int): Int = if (position == size - 1) 0 else position + 1

    @Suppress("NOTHING_TO_INLINE")
    private inline fun IntArray.decrementPosition(position: Int): Int = if (position == 0) size - 1 else position - 1

    internal companion object {
        private val EMPTY_ARRAY = IntArray(0)
        private const val DEFAULT_CAPACITY = 8

        fun wrap(array: IntArray): IntArrayDeque = IntArrayDeque(array)
    }
}

public fun IntArrayDeque.removeAll(predicate: (Int) -> Boolean): Boolean = filterInPlace(predicate)
public fun IntArrayDeque.retainAll(predicate: (Int) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }
