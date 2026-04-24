package io.github.sooniln.fastcollect.floats

import io.github.sooniln.fastcollect.ArrayUtils
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.max
import kotlin.math.min

public fun mutableFloatDequeOf(): FloatArrayDeque = FloatArrayDeque()
public fun mutableFloatDequeOf(vararg elements: Float): FloatArrayDeque = FloatArrayDeque(elements)

public class FloatArrayDeque private constructor(array: FloatArray, size: Int = array.size) : AbstractMutableFloatList(), RandomAccess {

    private var head: Int = 0
    private var ring: FloatArray = array
    override var size: Int = size
        private set

    public constructor(capacity: Int = 0) : this(if (capacity == 0) EMPTY_ARRAY else FloatArray(capacity), 0)

    public constructor(elements: Collection<Float>) : this(if (elements is FloatList) elements.toFloatArray() else elements.toFloatArray())

    public constructor(elements: FloatArray, fromIndex:Int = 0, toIndex: Int = elements.size) : this(elements.copyOfRange(fromIndex, toIndex))

    private fun positiveMod(position: Int): Int = if (position < ring.size) position else position - ring.size

    private fun position(index: Int): Int = positiveMod(head + index)

    private fun incrementPosition(position: Int): Int = if (position == ring.lastIndex) 0 else position + 1

    private fun decrementPosition(position: Int): Int = if (position == 0) ring.lastIndex else position - 1

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

        ring = copyFromRing(FloatArray(newCapacity))
        head = 0
    }

    private fun copyFromRing(dest: FloatArray): FloatArray {
        check(dest.size >= size)
        val tail = head + size
        if (tail <= ring.size) {
            ring.copyInto(dest, 0, head, tail)
        } else {
            ring.copyInto(dest, 0, head, ring.size)
            ring.copyInto(dest, ring.size - head, 0, positiveMod(tail))
        }
        return dest
    }

    public fun trimToSize() {
        if (size < ring.size) {
            ring = if (isEmpty()) EMPTY_ARRAY else copyFromRing(FloatArray(size))
            head = 0
        }
    }

    override fun get(index: Int): Float {
        return ring[position(rangeCheck(index))]
    }

    override fun setAt(index: Int, element: Float) {
        ring[position(rangeCheck(index))] = element
    }

    override fun add(index: Int, element: Float) {
        rangeCheckForAdd(index)
        val newSize = size + 1
        ensureCapacity(newSize)

        if (index == size) {
            ring[position(size)] = element
        } else if (index == 0) {
            head = decrementPosition(head)
            ring[head] = element
        } else {
            // attempt to shift a minimal number of elements depending on where position falls within the array
            val position = position(index)
            if (index < newSize shr 1) {
                // shift elements before position
                val actualPosition = decrementPosition(position)
                val newHead = decrementPosition(head)
                if (actualPosition >= head) {
                    // head before position
                    ring[newHead] = ring[head]  // first element could possibly roll over to the back of the array
                    ring.copyInto(ring, head, head + 1, position)
                } else {
                    // head after position
                    ring.copyInto(ring, newHead, head, ring.size) // head can't be zero
                    ring[ring.lastIndex] = ring[0]
                    ring.copyInto(ring, 0, 1, position)
                }
                ring[actualPosition] = element
                head = newHead
            } else {
                // shift elements after position
                val tail = position(size)
                if (position < tail) {
                    // position before tail
                    ring.copyInto(ring, position + 1, position, tail)
                } else {
                    // position after tail
                    val lastIndex = ring.lastIndex
                    ring.copyInto(ring, 1, 0, tail)
                    ring[0] = ring[lastIndex]
                    ring.copyInto(ring, position + 1, position, lastIndex)
                }
                ring[position] = element
            }
            ring[position(index)] = element
        }
        size = newSize
    }

    override fun removeAt(index: Int): Float {
        rangeCheck(index)

        val element: Float

        val lastIndex = lastIndex
        if (index == lastIndex) {
            element = ring[position(lastIndex)]
        } else if (index == 0) {
            element = ring[head]
            head = incrementPosition(head)
        } else {
            // attempt to shift a minimal number of elements depending on where position falls within the array
            val position = position(index)
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

                head = incrementPosition(head)
            } else {
                // shift elements after position
                val tail = position(lastIndex)
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
        // TODO: better
        require(fromIndex <= toIndex)
        rangeCheckForAdd(fromIndex)
        rangeCheckForAdd(toIndex)
        if (fromIndex == toIndex) return

        val removed = toIndex - fromIndex
        if (fromIndex <= size - toIndex) {
            // Shift [0, fromIndex) right by `removed`, then advance head.
            for (i in fromIndex - 1 downTo 0) {
                ring[position(i + removed)] = ring[position(i)]
            }
            head = (head + removed) % ring.size
        } else {
            // Shift [toIndex, size) left by `removed`.
            for (i in toIndex until size) {
                ring[position(i - removed)] = ring[position(i)]
            }
        }
        size -= removed
    }

    override fun clear() {
        head = 0
        size = 0
    }

    override fun indexOf(element: Float): Int {
        val tail = head + size
        if (tail <= ring.size) {
            for (i in head..<tail) {
                if (ring[i] == element) return i - head
            }
        } else {
            for (i in head..<ring.size) {
                if (ring[i] == element) return i - head
            }
            for (i in 0..<positiveMod(tail)) {
                if (ring[i] == element) return i + ring.size - head
            }
        }
        return -1
    }

    override fun lastIndexOf(element: Float): Int {
        // as of 4/26 kotlin compiler produces highly inefficient byte-code for 'downTo'. instead we use manual while
        // loops and break them out into separate functions so they can be better optimized.
        val tail = head + size - 1
        return if (tail < ring.size) {
            lastIndexOfContinuous(tail, element)
        } else {
            lastIndexOfDiscrete(tail, element)
        }
    }

    private fun lastIndexOfContinuous(tail: Int, element: Float): Int {
        val head = head
        var i = tail
        while (i >= head) {
            if (ring[i] == element) return i - head
            i--
        }
        return -1
    }

    private fun lastIndexOfDiscrete(tail: Int, element: Float): Int {
        val head = head
        var i = positiveMod(tail)
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

    public fun addAll(elements: FloatArrayDeque): Boolean {
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        val elementsTail = elements.head + elements.size
        if (elementsTail <= elements.ring.size) {
            addToRing(elements.ring, elements.head, elementsTail)
        } else {
            addToRing(elements.ring, elements.head, elements.ring.size)
            addToRing(elements.ring, 0, elements.positiveMod(elementsTail))
        }
        return true
    }

    private fun addToRing(src: FloatArray, fromIndex: Int, toIndex: Int) {
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
            src.copyInto(ring, positiveMod(tail), fromIndex, toIndex)
        }
        size += srcLength
    }

    override fun addAll(elements: FloatCollection): Boolean {
        if (elements is FloatArrayDeque) return addAll(elements)
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    override fun addAll(elements: Collection<Float>): Boolean {
        if (elements is FloatCollection) return addAll(elements)
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    public override fun removeAll(elements: Collection<Float>): Boolean {
        return filterInPlace { e -> elements.contains(e) }
    }

    public override fun retainAll(elements: Collection<Float>): Boolean {
        return filterInPlace { e -> !elements.contains(e) }
    }

    @OptIn(ExperimentalContracts::class)
    internal inline fun filterInPlace(removePredicate: (Float) -> Boolean): Boolean {
        contract {
            callsInPlace(removePredicate, InvocationKind.UNKNOWN)
        }

        var position = head
        val tail = position(size)
        while (true) {
            if (position == tail) {
                return false
            } else if (removePredicate(ring[position])) {
                break
            }
            position = incrementPosition(position)
        }

        var insertionPosition = position
        position = incrementPosition(position)
        while (position != tail) {
            val element = ring[position]
            position = incrementPosition(position)
            if (!removePredicate(element)) {
                ring[insertionPosition] = element
                insertionPosition = incrementPosition(insertionPosition)
            }
        }
        size = insertionPosition - head
        if (size < 0) {
            size += ring.size
        }
        return true
    }

    override fun toFloatArray(): FloatArray {
        return copyFromRing(FloatArray(size))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is List<*>) return false

        if (size != other.size) return false
        if (other is FloatList && other is RandomAccess) {
            for (i in indices) {
                if (ring[position(i)] != other[i]) return false
            }
        } else {
            val it = other.iterator()
            var i = 0
            if (it is FloatIterator) {
                while (it.hasNext()) {
                    if (it.nextFloat() != this[i++]) return false
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
            val end = position(lastIndex)
            do {
                hashCode = 31 * hashCode + ring[position].hashCode()
                position = incrementPosition(position)
            } while (position != end)
        }
        return hashCode
    }

    internal companion object {
        private val EMPTY_ARRAY = FloatArray(0)
        private const val DEFAULT_CAPACITY = 8

        fun wrap(array: FloatArray): FloatArrayDeque = FloatArrayDeque(array)
    }
}

public fun FloatArrayDeque.removeAll(predicate: (Float) -> Boolean): Boolean = filterInPlace(predicate)
public fun FloatArrayDeque.retainAll(predicate: (Float) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }
