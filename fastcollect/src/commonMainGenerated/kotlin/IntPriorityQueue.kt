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

public fun intPriorityQueueOf(vararg elements: Int, descending: Boolean = false): IntPriorityQueue {
    return IntPriorityQueue(elements, descending = descending)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildIntPriorityQueue(
    expectedSize: Int = 0,
    descending: Boolean = false,
    builderAction: IntPriorityQueue.() -> Unit,
): IntPriorityQueue {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val priorityQueue = IntPriorityQueue(expectedSize, descending)
    priorityQueue.builderAction()
    return priorityQueue
}

/**
 * An abstract base class for priority queues of Ints, backed by a binary heap. Subclasses can override
 * [isHigherPriority] to specify the ordering, [onIndexChanged] to optionally maintain an element ⟷ index mapping, and
 * have access to the protected [updatePriority] and [heapify] methods to handle priority changes.
 *
 * The extension method `asQueue()` produces a thin wrapper around this class which exposes it as Kotlin queue which can
 * be used anywhere a Kotlin queue is expected. Using this wrapper may incur boxing penalties.
 */
public abstract class AbstractIntPriorityQueue(initialCapacity: Int = 0): IntTraversable {

    public constructor(array: IntArray, fromIndex: Int = 0, toIndex: Int = array.size) : this(0) {
        heap = array.copyOfRange(fromIndex, toIndex)
        size = toIndex - fromIndex
        for (i in 0..<size) onIndexChanged(heap[i], i)
        heapify()
    }

    public constructor(elements: IntCollection) : this(elements.size) {
        addAll(elements)
    }

    public constructor(elements: Collection<Int>) : this(elements.size) {
        addAll(elements)
    }

    private var heap: IntArray = if (initialCapacity == 0) EMPTY_ARRAY else IntArray(initialCapacity)
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
    protected abstract fun isHigherPriority(element1: Int, element2: Int): Boolean

    /**
     * Invoked whenever [element] is stored at [index] in the backing heap. Subclasses can use this to store element ⟷
     * index mappings, and in turn use the index to interact with [updatePriority].
     */
    protected open fun onIndexChanged(element: Int, index: Int) {}

    /**
     * Returns the head of this priority queue without removing it. Throws [NoSuchElementException] if the queue is
     * empty.
     */
    public fun first(): Int {
        if (isEmpty()) throw NoSuchElementException()
        return heap[0]
    }

    /**
     * Removes and returns the head of this priority queue.Throws [NoSuchElementException] if the queue is empty.
     */
    public fun removeFirst(): Int {
        if (isEmpty()) throw NoSuchElementException()
        val result = heap[0]
        --size
        if (size > 0) {
            setHeap(0, heap[size])
            siftDown(0)
        }
        return result
    }

    public fun add(element: Int) {
        ensureCapacity(size + 1)
        setHeap(size, element)
        siftUp(size)
        ++size
    }

    public fun remove(element: Int): Boolean {
        for (i in 0..<size) {
            if (heap[i] equalsBoxed element) {
                removeAt(i)
                return true
            }
        }
        return false
    }

    public fun contains(element: Int): Boolean {
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

    public fun addAll(elements: IntCollection) {
        ensureCapacity(size + elements.size)
        elements.foreach { element ->
            setHeap(size++, element)
        }
        heapify()
    }

    public fun addAll(elements: Collection<Int>) {
        ensureCapacity(size + elements.size)
        for (element in elements) {
            setHeap(size++, element)
        }
        heapify()
    }

    public fun removeAll(elements: IntCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Int>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: IntCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Int>): Boolean = filterInPlace { !elements.contains(it) }

    @OptIn(ExperimentalContracts::class)
    internal inline fun filterInPlace(removePredicate: (Int) -> Boolean): Boolean {
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

    public fun iterator(): IntIterator = object : IntIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index < size
        override fun nextInt(): Int {
            if (!hasNext()) throw NoSuchElementException()
            return heap[index++]
        }
    }

    override fun traverse(): IntTraverser = object : IntTraverser {
        private val last = size - 1
        private var position: Int = -1

        override val value: Int get() = heap[position]

        override fun advance(): Boolean {
            if (position == last) return false
            if (last != size - 1) throw ConcurrentModificationException()
            ++position
            return true
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

    private fun setHeap(index: Int, element: Int) {
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
        private val EMPTY_ARRAY = IntArray(0)
        private const val DEFAULT_CAPACITY = 8
    }
}

public fun AbstractIntPriorityQueue.removeAll(predicate: IntPredicate): Boolean = filterInPlace { predicate.test(it) }
public fun AbstractIntPriorityQueue.retainAll(predicate: IntPredicate): Boolean = filterInPlace { !predicate.test(it) }

/**
 * A priority queue of Ints, backed by a binary heap, which can operate in descending (largest element at the front
 * of the queue) or ascending (smallest element at the front of the queue) order.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Methods all conform to normal array based binomial heap performance expectations, and [remove] operations take O(N)
 * (linear) time with respect to queue size. If an indirect heap implementation is desired (with faster remove
 * operations and the option to change an element's priority), one can be implemented by subclassing
 * [AbstractIntPriorityQueue] and implementing [onIndexChanged] to store an element ⟷ index mapping.
 */
public class IntPriorityQueue : AbstractIntPriorityQueue {

    private val descending: Boolean

    public constructor(initialCapacity: Int = 0, descending: Boolean = false) : super(initialCapacity) {
        this.descending = descending
    }

    public constructor(array: IntArray, fromIndex: Int = 0, toIndex: Int = array.size, descending: Boolean = false) : super(array, fromIndex, toIndex) {
        this.descending = descending
    }

    public constructor(elements: IntCollection, descending: Boolean = false) : super(elements) {
        this.descending = descending
    }

    public constructor(elements: Collection<Int>, descending: Boolean = false) : super(elements) {
        this.descending = descending
    }

    override fun isHigherPriority(element1: Int, element2: Int): Boolean {
        return if (descending) element1 > element2 else element2 > element1
    }
}

/**
 * Returns a priority queue of Ints, using the given comparator.
 */
@JvmSynthetic
public inline fun IntPriorityQueue(
    initialCapacity: Int = 0,
    crossinline isHigherPriority: (Int, Int) -> Boolean,
): AbstractIntPriorityQueue {
    return object : AbstractIntPriorityQueue(initialCapacity) {
        override fun isHigherPriority(element1: Int, element2: Int): Boolean {
            return isHigherPriority(element1, element2)
        }
    }
}
