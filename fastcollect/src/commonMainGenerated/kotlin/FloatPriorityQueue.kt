/**
 * Methods for dealing with primitive PriorityQueues.
 */
@file:JvmName("PriorityQueues")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.math.max

public fun floatPriorityQueueOf(vararg elements: Float, descending: Boolean = false): FloatPriorityQueue {
    return FloatPriorityQueue(elements, descending = descending)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildFloatPriorityQueue(
    expectedSize: Int = 0,
    descending: Boolean = false,
    builderAction: FloatPriorityQueue.() -> Unit,
): FloatPriorityQueue {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val priorityQueue = FloatPriorityQueue(expectedSize, descending)
    priorityQueue.builderAction()
    return priorityQueue
}

/**
 * An abstract base class for priority queues of Floats, backed by a binary heap. Subclasses can override
 * [isHigherPriority] to specify the ordering, [onIndexChanged] to optionally maintain an element ⟷ index mapping, and
 * have access to the protected [updatePriority] and [heapify] methods to handle priority changes.
 */
public abstract class AbstractFloatPriorityQueue(initialCapacity: Int = 0) {

    public constructor(array: FloatArray, fromIndex: Int = 0, toIndex: Int = array.size) : this(0) {
        heap = array.copyOfRange(fromIndex, toIndex)
        size = toIndex - fromIndex
        for (i in 0..<size) onIndexChanged(heap[i], i)
        heapify()
    }

    public constructor(elements: FloatCollection) : this(elements.size) {
        addAll(elements)
    }

    public constructor(elements: Collection<Float>) : this(elements.size) {
        addAll(elements)
    }

    private var heap: FloatArray = if (initialCapacity == 0) EMPTY_ARRAY else FloatArray(initialCapacity)
    public var size: Int = 0
        private set

    public fun isEmpty(): Boolean = size == 0

    public fun ensureCapacity(capacity: Int) {
        if (capacity > heap.size) grow(capacity)
    }

    private fun grow(capacity: Int) {
        val oldCapacity = heap.size
        val newCapacity = if (oldCapacity > 0 || heap !== EMPTY_ARRAY) {
            growArraySize(oldCapacity, capacity - oldCapacity)
        } else {
            max(DEFAULT_CAPACITY, capacity)
        }
        heap = heap.copyOf(newCapacity)
    }

    public fun trimToSize() {
        if (size < heap.size) {
            heap = if (size == 0) EMPTY_ARRAY else heap.copyOf(size)
        }
    }

    /**
     * Returns true if [element1] has higher priority (should be closer to the head of the queue) than [element2].
     */
    protected abstract fun isHigherPriority(element1: Float, element2: Float): Boolean

    /**
     * Invoked whenever [element] is stored at [index] in the backing heap. Subclasses can use this to store element ⟷
     * index mappings, and in turn use the index to interact with [updatePriority].
     */
    protected open fun onIndexChanged(element: Float, index: Int) {}

    /**
     * Returns the head of this priority queue without removing it. Throws [NoSuchElementException] if the queue is
     * empty.
     */
    public fun first(): Float {
        if (isEmpty()) throw NoSuchElementException()
        return heap[0]
    }

    /**
     * Removes and returns the head of this priority queue.Throws [NoSuchElementException] if the queue is empty.
     */
    public fun removeFirst(): Float {
        if (isEmpty()) throw NoSuchElementException()
        val result = heap[0]
        --size
        if (size > 0) {
            setHeap(0, heap[size])
            siftDown(0)
        }
        return result
    }

    public fun add(element: Float) {
        ensureCapacity(size + 1)
        setHeap(size, element)
        siftUp(size)
        ++size
    }

    public fun remove(element: Float): Boolean {
        for (i in 0..<size) {
            if (heap[i] equalsBoxed element) {
                removeAt(i)
                return true
            }
        }
        return false
    }

    public fun contains(element: Float): Boolean {
        for (i in 0..<size) {
            if (heap[i] equalsBoxed element) {
                return true
            }
        }
        return false
    }

    public fun clear() {
        size = 0
    }

    public fun addAll(elements: FloatCollection) {
        ensureCapacity(size + elements.size)
        elements.foreach { element ->
            setHeap(size++, element)
        }
        heapify()
    }

    public fun addAll(elements: Collection<Float>) {
        if (elements is FloatCollection) {
            addAll(elements)
            return
        }

        ensureCapacity(size + elements.size)
        for (element in elements) {
            setHeap(size++, element)
        }
        heapify()
    }

    public fun removeAll(elements: FloatCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Float>): Boolean {
        return if (elements is FloatCollection) removeAll(elements) else filterInPlace { elements.contains(it) }
    }

    public fun retainAll(elements: FloatCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Float>): Boolean {
        return if (elements is FloatCollection) retainAll(elements) else filterInPlace { !elements.contains(it) }
    }

    @OptIn(ExperimentalContracts::class)
    internal inline fun filterInPlace(removePredicate: (Float) -> Boolean): Boolean {
        contract {
            callsInPlace(removePredicate, InvocationKind.UNKNOWN)
        }

        var index = 0
        while (true) {
            if (index == size) {
                return false
            } else if (removePredicate(heap[index])) {
                break
            }
            ++index
        }

        var newSize = index++
        while (index < size) {
            val element = heap[index]
            if (!removePredicate(element)) {
                setHeap(newSize++, element)
            }
            ++index
        }

        size = newSize
        heapify()
        return true
    }

    public fun iterator(): FloatIterator = object : FloatIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index < size
        override fun nextFloat(): Float {
            if (!hasNext()) throw NoSuchElementException()
            return heap[index++]
        }
    }

    /**
     * Re-establishes the heap invariant if the priority for the element at [index] has changed.
     */
    protected fun updatePriority(index: Int) {
        if (siftDown(index) == index) siftUp(index)
    }

    /**
     * Removes the element at the given index and re-establishes the heap invariant.
     */
    protected fun removeAt(index: Int) {
        if (--size != index) {
            setHeap(index, heap[size])
            updatePriority(index)
        }
    }

    protected fun heapify() {
        for (i in size / 2 - 1 downTo 0) {
            siftDown(i)
        }
    }

    override fun toString(): String = Iterable { iterator() }.joinToString(", ", "[", "]")

    private fun setHeap(index: Int, element: Float) {
        heap[index] = element
        onIndexChanged(element, index)
    }

    private fun siftUp(index: Int): Int {
        var i = index
        val element = heap[i]
        while (i > 0) {
            val parentIndex = (i - 1) / 2
            val parentElement = heap[parentIndex]
            if (!isHigherPriority(element, parentElement)) break
            setHeap(i, parentElement)
            i = parentIndex
        }
        setHeap(i, element)
        return i
    }

    private fun siftDown(index: Int): Int {
        var i = index
        val element = heap[i]
        val half = size / 2
        while (i < half) {
            var childIndex = 2 * i + 1
            var childElement = heap[childIndex]
            val rightIndex = childIndex + 1
            if (rightIndex < size && isHigherPriority(heap[rightIndex], childElement)) {
                childIndex = rightIndex
                childElement = heap[rightIndex]
            }
            if (!isHigherPriority(childElement, element)) break
            setHeap(i, childElement)
            i = childIndex
        }
        setHeap(i, element)
        return i
    }

    internal companion object {
        private val EMPTY_ARRAY = FloatArray(0)
        private const val DEFAULT_CAPACITY = 8
    }
}

public fun AbstractFloatPriorityQueue.removeAll(predicate: FloatPredicate): Boolean = filterInPlace { predicate.test(it) }
public fun AbstractFloatPriorityQueue.retainAll(predicate: FloatPredicate): Boolean = filterInPlace { !predicate.test(it) }

/**
 * A priority queue of Floats, backed by a binary heap, which can operate in descending (largest element at the front
 * of the queue) or ascending (smallest element at the front of the queue) order.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Methods all conform to normal array based binomial heap performance expectations, and [remove] operations take O(N)
 * (linear) time with respect to queue size. If an indirect heap implementation is desired (with faster remove
 * operations and the option to change an element's priority), one can be implemented by subclassing
 * [AbstractFloatPriorityQueue] and implementing [onIndexChanged] to store an element ⟷ index mapping.
 */
public class FloatPriorityQueue : AbstractFloatPriorityQueue {

    private val descending: Boolean

    public constructor(initialCapacity: Int = 0, descending: Boolean = false) : super(initialCapacity) {
        this.descending = descending
    }

    public constructor(array: FloatArray, fromIndex: Int = 0, toIndex: Int = array.size, descending: Boolean = false) : super(array, fromIndex, toIndex) {
        this.descending = descending
    }

    public constructor(elements: FloatCollection, descending: Boolean = false) : super(elements) {
        this.descending = descending
    }

    public constructor(elements: Collection<Float>, descending: Boolean = false) : super(elements) {
        this.descending = descending
    }

    override fun isHigherPriority(element1: Float, element2: Float): Boolean {
        return if (descending) element1 > element2 else element2 > element1
    }
}

/**
 * Returns a priority queue of Floats, using the given comparator.
 */
@JvmSynthetic
public inline fun FloatPriorityQueue(
    initialCapacity: Int = 0,
    crossinline isHigherPriority: (Float, Float) -> Boolean,
): AbstractFloatPriorityQueue {
    return object : AbstractFloatPriorityQueue(initialCapacity) {
        override fun isHigherPriority(element1: Float, element2: Float): Boolean = isHigherPriority(element1, element2)
    }
}
