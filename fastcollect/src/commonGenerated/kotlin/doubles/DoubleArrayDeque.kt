package io.github.sooniln.fastcollect.doubles

import io.github.sooniln.fastcollect.ArrayUtils
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max
import kotlin.math.min

/**
 * An array based [Deque](https://en.wikipedia.org/wiki/Double-ended_queue) implementation for storing {Double}s. Can be
 * used in place of the Kotlin standard library [ArrayList] and [ArrayDeque] implementations to improve performance and
 * memory usage. Has the same API contracts as the standard library [ArrayList] and [ArrayDeque] unless noted otherwise.
 *
 * Note that unfortunately some of the common Kotlin List methods may force primitive type boxing, and thus could incur
 * performance penalties. These methods have been marked as deprecated so they will be easily visible in IDEs. It is
 * encouraged to use the replacement methods this class offers in order to guarantee no unnecessary boxing will occur:
 *
 *   * Use [setAt] instead of [MutableList.set] or the indexed write operator.
 *
 * This implementation supports amortized O(1) `addFirst/addLast/removeFirst/removeLast` functionality. The
 * [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 */
public class DoubleArrayDeque private constructor(array: DoubleArray, size: Int = array.size) : AbstractMutableDoubleList(), RandomAccess {

    private var head: Int = 0
    private var ring: DoubleArray = array
    override var size: Int = size
        private set

    public constructor(capacity: Int = 0) : this(if (capacity == 0) EMPTY_ARRAY else DoubleArray(capacity), 0)

    public constructor(elements: Collection<Double>) : this(if (elements is DoubleList) elements.toDoubleArray() else elements.toDoubleArray())

    public constructor(elements: DoubleArray, fromIndex:Int = 0, toIndex: Int = elements.size) : this(elements.copyOfRange(fromIndex, toIndex))

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

        ring = copyFromRing(DoubleArray(newCapacity))
        head = 0
    }

    private fun copyFromRing(dest: DoubleArray): DoubleArray {
        check(dest.size >= size)
        val tail = head + size
        if (tail <= ring.size) {
            ring.copyInto(dest, 0, head, tail)
        } else {
            ring.copyInto(dest, 0, head, ring.size)
            ring.copyInto(dest, ring.size - head, 0, ring.positiveMod(tail))
        }
        return dest
    }

    public fun trimToSize() {
        if (size < ring.size) {
            ring = if (isEmpty()) EMPTY_ARRAY else copyFromRing(DoubleArray(size))
            head = 0
        }
    }

    override fun get(index: Int): Double {
        return ring[ring.position(rangeCheck(index))]
    }

    override fun setAt(index: Int, element: Double) {
        ring[ring.position(rangeCheck(index))] = element
    }

    override fun add(index: Int, element: Double) {
        rangeCheckForAdd(index)
        val newSize = size + 1
        ensureCapacity(newSize)

        if (index == size) {
            ring[ring.position(size)] = element
        } else if (index == 0) {
            head = ring.decrementPosition(head)
            ring[head] = element
        } else {
            // attempt to shift a minimal number of elements depending on where position falls within the array
            val position = ring.position(index)
            if (index < newSize shr 1) {
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
                    ring.copyInto(ring, position + 1, position, lastIndex)
                }
                ring[position] = element
            }
            ring[ring.position(index)] = element
        }
        size = newSize
    }

    override fun removeAt(index: Int): Double {
        rangeCheck(index)

        val element: Double

        val lastIndex = size - 1
        if (index == lastIndex) {
            element = ring[ring.position(lastIndex)]
        } else if (index == 0) {
            element = ring[head]
            head = ring.incrementPosition(head)
        } else {
            // attempt to shift a minimal number of elements depending on where position falls within the array
            val position = ring.position(index)
            element = ring[position]

            if (index < size shr 1) {
                // shift elements before position
                if (position >= head) {
                    // head before position
                    ring.copyInto(ring, head + 1, head, position)
                } else {
                    // head after position
                    ring.copyInto(ring, 1, 0, position)
                    ring[0] = ring[ring.size - 1]
                    ring.copyInto(ring, head + 1, head, ring.size - 1)
                }

                head = ring.incrementPosition(head)
            } else {
                // shift elements after position
                val tail = ring.position(lastIndex)
                if (position <= tail) {
                    // position before tail
                    ring.copyInto(ring, position, position + 1, tail + 1)
                } else {
                    // position after tail
                    ring.copyInto(ring, position, position + 1, ring.size)
                    ring[ring.size - 1] = ring[0]
                    ring.copyInto(ring, 0, 1, tail + 1)
                }
            }
        }

        size--
        return element
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        // TODO: would array copy operations be more efficient?
        require(fromIndex <= toIndex)
        rangeCheckForAdd(fromIndex)
        rangeCheckForAdd(toIndex)
        if (fromIndex == toIndex) return

        val removed = toIndex - fromIndex
        if (fromIndex <= size - toIndex) {
            // Shift [0, fromIndex) right by `removed`, then advance head.
            for (i in fromIndex - 1 downTo 0) {
                ring[ring.position(i + removed)] = ring[ring.position(i)]
            }
            head = (head + removed) % ring.size
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

    override fun indexOf(element: Double): Int {
        val tail = head + size
        if (tail <= ring.size) {
            for (i in head..<tail) {
                if (ring[i] == element) return i - head
            }
        } else {
            for (i in head..<ring.size) {
                if (ring[i] == element) return i - head
            }
            for (i in 0..<ring.positiveMod(tail)) {
                if (ring[i] == element) return i + ring.size - head
            }
        }
        return -1
    }

    override fun lastIndexOf(element: Double): Int {
        // as of 4/26 kotlin compiler produces highly inefficient byte-code for 'downTo'. instead we use manual while
        // loops and break them out into separate functions so they can be better optimized.
        val tail = head + size - 1
        return if (tail < ring.size) {
            lastIndexOfContinuous(tail, element)
        } else {
            lastIndexOfDiscrete(tail, element)
        }
    }

    private fun lastIndexOfContinuous(tail: Int, element: Double): Int {
        val head = head
        var i = tail
        while (i >= head) {
            if (ring[i] == element) return i - head
            i--
        }
        return -1
    }

    private fun lastIndexOfDiscrete(tail: Int, element: Double): Int {
        val head = head
        var i = ring.positiveMod(tail)
        while (i >= 0) {
            if (ring[i] == element) return i + ring.size - head
            i--
        }
        i = ring.size - 1
        while (i >= head) {
            if (ring[i] == element) return i - head
            i--
        }
        return -1
    }

    public fun addAll(elements: DoubleArrayDeque): Boolean {
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        val elementsTail = elements.head + elements.size
        if (elementsTail <= elements.ring.size) {
            addToRing(elements.ring, elements.head, elementsTail)
        } else {
            addToRing(elements.ring, elements.head, elements.ring.size)
            addToRing(elements.ring, 0, elements.ring.positiveMod(elementsTail))
        }
        return true
    }

    private fun addToRing(src: DoubleArray, fromIndex: Int, toIndex: Int) {
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
            src.copyInto(ring, ring.positiveMod(tail), fromIndex, toIndex)
        }
        size += srcLength
    }

    override fun addAll(elements: DoubleCollection): Boolean {
        if (elements is DoubleArrayDeque) return addAll(elements)
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    override fun addAll(elements: Collection<Double>): Boolean {
        if (elements is DoubleCollection) return addAll(elements)
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    public override fun removeAll(elements: Collection<Double>): Boolean {
        return filterInPlace { e -> elements.contains(e) }
    }

    public override fun retainAll(elements: Collection<Double>): Boolean {
        return filterInPlace { e -> !elements.contains(e) }
    }

    @OptIn(ExperimentalContracts::class)
    internal inline fun filterInPlace(removePredicate: (Double) -> Boolean): Boolean {
        contract {
            callsInPlace(removePredicate, InvocationKind.UNKNOWN)
        }

        var position = head
        val tail = ring.position(size)
        while (true) {
            if (position == tail) {
                return false
            } else if (removePredicate(ring[position])) {
                break
            }
            position = ring.incrementPosition(position)
        }

        var insertionPosition = position
        position = ring.incrementPosition(position)
        while (position != tail) {
            val element = ring[position]
            position = ring.incrementPosition(position)
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

    override fun toDoubleArray(): DoubleArray {
        return copyFromRing(DoubleArray(size))
    }

    override fun iterator(): MutableDoubleIterator = DequeIterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is List<*>) return false

        if (size != other.size) return false
        if (other is DoubleList && other is RandomAccess) {
            for (i in indices) {
                if (ring[ring.position(i)] != other[i]) return false
            }
        } else {
            val it = other.iterator()
            var i = 0
            if (it is DoubleIterator) {
                while (it.hasNext()) {
                    if (it.nextDouble() != this[i++]) return false
                }
            } else {
                while (it.hasNext()) {
                    if (it.next() != this[i++]) return false
                }
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

    private inner class DequeIterator : MutableDoubleIterator() {
        private val ring = this@DoubleArrayDeque.ring

        protected var remaining = size
        protected var position = head
        protected var previousPosition = -1

        override fun hasNext() = remaining > 0

        override fun nextDouble(): Double {
            if (remaining == 0) throw NoSuchElementException()

            --remaining
            previousPosition = position
            position = ring.incrementPosition(position)
            return ring[previousPosition]
        }

        override fun remove() {
            if (ring !== this@DoubleArrayDeque.ring) throw ConcurrentModificationException()
            check(previousPosition != -1)

            val removedIndex = ring.index(previousPosition)
            removeAt(removedIndex)
            position = ring.position(removedIndex)
            previousPosition = -1
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun DoubleArray.positiveMod(position: Int): Int = if (position < size) position else position - size

    @Suppress("NOTHING_TO_INLINE")
    private inline fun DoubleArray.negativeMod(position: Int): Int = if (position < 0) position + size else position

    @Suppress("NOTHING_TO_INLINE")
    private inline fun DoubleArray.position(index: Int): Int = positiveMod(head + index)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun DoubleArray.index(position: Int): Int = negativeMod(position - head)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun DoubleArray.incrementPosition(position: Int): Int = if (position == size - 1) 0 else position + 1

    @Suppress("NOTHING_TO_INLINE")
    private inline fun DoubleArray.decrementPosition(position: Int): Int = if (position == 0) size - 1 else position - 1

    internal companion object {
        private val EMPTY_ARRAY = DoubleArray(0)
        private const val DEFAULT_CAPACITY = 8

        fun wrap(array: DoubleArray): DoubleArrayDeque = DoubleArrayDeque(array)
    }
}

public fun DoubleArrayDeque.removeAll(predicate: (Double) -> Boolean): Boolean = filterInPlace(predicate)
public fun DoubleArrayDeque.retainAll(predicate: (Double) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }
