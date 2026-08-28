/**
 * Methods for dealing with BytePriorityQueues.
 */
@file:JvmName("BytePriorityQueues")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.math.max

public fun bytePriorityQueueOf(vararg elements: Byte): BytePriorityQueue {
    return BytePriorityQueue(elements, descending = false)
}

public fun byteDescendingPriorityQueueOf(vararg elements: Byte): BytePriorityQueue {
    return BytePriorityQueue(elements, descending = true)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildBytePriorityQueue(
    expectedSize: Int = 0,
    descending: Boolean = false,
    builderAction: BytePriorityQueue.() -> Unit,
): BytePriorityQueue {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val priorityQueue = BytePriorityQueue(expectedSize, descending)
    priorityQueue.builderAction()
    return priorityQueue
}

/**
 * An abstract base class for priority queues of Bytes, backed by a binary heap. Subclasses can override
 * [isHigherPriority] to specify the ordering, [onIndexChanged] to optionally maintain an element ⟷ index mapping, and
 * have access to the protected [updatePriority] and [heapify] methods to handle priority changes.
 *
 * The extension method `asQueue()` produces a thin wrapper around this class which exposes it as Kotlin queue which can
 * be used anywhere a Kotlin queue is expected. Using this wrapper may incur boxing penalties.
 */
public abstract class AbstractBytePriorityQueue(initialCapacity: Int): ByteCollection {

    public constructor() : this(0)

    @JvmOverloads
    public constructor(array: ByteArray, fromIndex: Int = 0, toIndex: Int = array.size) : this(0) {
        addAll(array, fromIndex, toIndex)
    }

    public constructor(elements: ByteCollection) : this(elements.size) {
        addAll(elements)
    }

    public constructor(elements: Collection<Byte>) : this(elements.size) {
        addAll(elements)
    }

    private var heap: ByteArray = if (initialCapacity == 0) EMPTY_ARRAY else ByteArray(initialCapacity)
    final override var size: Int = 0
        private set

    public fun ensureCapacity(capacity: Int) {
        if (capacity > heap.size) grow(capacity)
    }

    private fun grow(capacity: Int) {
        val oldCapacity = heap.size
        val newCapacity = if (oldCapacity > 0) {
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
     * Returns true if [element1] has higher priority (should be closer to the head of the queue) than [element2]. Be
     * aware that certain constructors may invoke this function before subclass constructors run / subclass fields are
     * initialized - plan accordingly.
     */
    protected abstract fun isHigherPriority(element1: Byte, element2: Byte): Boolean

    /**
     * Invoked whenever [element] is stored at [index] in the backing heap. Subclasses can use this to store element ⟷
     * index mappings, and in turn use the index to interact with [updatePriority]. The heap is not in a consistent
     * state when this method is invoked - you cannot trust heap properties such as [size], nor are you allowed to
     * interact with the heap at all.
     */
    protected open fun onIndexChanged(element: Byte, index: Int) {}

    /**
     * Invoked after [element] is removed from the heap, where [index] is where it was stored prior to remove.
     * Subclasses can use this to maintain element ⟷ index mappings. The heap is not in a consistent state when this
     * method is invoked - you cannot trust heap properties such as [size], nor are you allowed to interact with the
     * heap at all.
     */
    protected open fun onRemoved(element: Byte, index: Int) {}

    /**
     * Returns the head of this priority queue without removing it. Throws [NoSuchElementException] if the queue is
     * empty.
     */
    public fun first(): Byte {
        if (isEmpty()) throw NoSuchElementException()
        return heap[0]
    }

    /**
     * Removes and returns the head of this priority queue. Throws [NoSuchElementException] if the queue is empty.
     */
    public fun removeFirst(): Byte {
        if (isEmpty()) throw NoSuchElementException()
        val result = heap[0]
        onRemoved(result, 0)
        var index = --size
        if (index > 0) {
            val element = heap[index]
            heap[0] = element
            index = siftDown(0)
            onIndexChanged(element, index)
        }
        return result
    }

    public fun add(element: Byte) {
        ensureCapacity(size + 1)
        var index = size++
        heap[index] = element
        index = siftUp(index)
        onIndexChanged(element, index)
    }

    public fun remove(element: Byte): Boolean {
        for (i in 0..<size) {
            if (heap[i] equalsRaw element) {
                removeAt(i)
                return true
            }
        }
        return false
    }

    final override fun contains(element: Byte): Boolean {
        for (i in 0..<size) {
            if (heap[i] equalsRaw element) {
                return true
            }
        }
        return false
    }

    public fun clear() {
        for (i in 0..<size) {
            onRemoved(heap[i], i)
        }
        size = 0
    }

    @JvmOverloads
    public fun addAll(array: ByteArray, fromIndex: Int = 0, toIndex: Int = array.size) {
        val newSize = size + toIndex - fromIndex
        if (newSize == size) return

        ensureCapacity(newSize)
        array.copyInto(heap, size, fromIndex, toIndex)
        for (i in size..<newSize) onIndexChanged(heap[i], i)
        size = newSize
        heapify()
    }

    public fun addAll(elements: ByteCollection) {
        ensureCapacity(size + elements.size)
        elements.foreach { element ->
            heap[size] = element
            onIndexChanged(element, size)
            ++size
        }
        heapify()
    }

    public fun addAll(elements: Collection<Byte>) {
        ensureCapacity(size + elements.size)
        for (element in elements) {
            heap[size] = element
            onIndexChanged(element, size)
            ++size
        }
        heapify()
    }

    public fun removeAll(elements: ByteCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Byte>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: ByteCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Byte>): Boolean = filterInPlace { !elements.contains(it) }

    @OptIn(ExperimentalContracts::class)
    internal inline fun filterInPlace(removePredicate: (Byte) -> Boolean): Boolean {
        contract {
            callsInPlace(removePredicate, InvocationKind.UNKNOWN)
        }

        var index = 0
        while (true) {
            if (index == size) {
                return false
            } else {
                val element = heap[index]
                if (removePredicate(element)) {
                    onRemoved(element, index)
                    break
                }
            }
            ++index
        }

        var newSize = index++
        while (index < size) {
            val element = heap[index]
            if (!removePredicate(element)) {
                heap[newSize] = element
                onIndexChanged(element, newSize)
                ++newSize
            } else {
                onRemoved(element, index)
            }
            ++index
        }

        size = newSize
        heapify()
        return true
    }

    final override fun copyInto(destination: ByteArray, destinationOffset: Int): ByteArray {
        destination.rangeCheck(destinationOffset, destinationOffset + size)
        return heap.copyInto(destination, destinationOffset, 0, size)
    }

    final override fun iterator(): ByteIterator = object : ByteIterator() {
        private val size = this@AbstractBytePriorityQueue.size
        private var index = 0
        override fun hasNext(): Boolean = index < size
        override fun nextByte(): Byte {
            if (!hasNext()) throw NoSuchElementException()
            if (size != this@AbstractBytePriorityQueue.size) throw ConcurrentModificationException()
            return heap[index++]
        }
    }

    final override fun traverser(): ByteTraverser = object : ByteTraverser {
        private val last = size - 1
        private var position: Int = -1

        override val value: Byte get() {
            check(position >= 0)
            return heap[position]
        }

        override fun forward(): Boolean {
            if (position >= last) return false
            if (last != size - 1) throw ConcurrentModificationException()
            ++position
            return true
        }
    }

    /**
     * Re-establishes the heap invariant if the priority for the element at [index] has changed.
     */
    protected fun updatePriority(index: Int) {
        val finalIndex = updatePriorityInternal(index)
        if (finalIndex != index) {
            onIndexChanged(heap[finalIndex], finalIndex)
        }
    }

    private fun updatePriorityInternal(index: Int): Int {
        var finalIndex = siftDown(index)
        if (finalIndex == index) {
            finalIndex = siftUp(index)
        }
        return finalIndex
    }

    /**
     * Removes the element at the given index and re-establishes the heap invariant.
     */
    protected fun removeAt(index: Int) {
        onRemoved(heap[index], index)
        if (--size != index) {
            val element = heap[size]
            heap[index] = element
            onIndexChanged(element, updatePriorityInternal(index))
        }
    }

    protected fun heapify() {
        for (i in size / 2 - 1 downTo 0) {
            val index = siftDown(i)
            if (index != i) {
                onIndexChanged(heap[index], index)
            }
        }
    }

    final override fun toString(): String = Iterable { iterator() }.joinToString(", ", "[", "]")

    private fun siftUp(index: Int): Int {
        var i = index
        val element = heap[i]
        while (i > 0) {
            val parentIndex = (i - 1) / 2
            val parentElement = heap[parentIndex]
            if (!isHigherPriority(element, parentElement)) break
            heap[i] = parentElement
            onIndexChanged(parentElement, i)
            i = parentIndex
        }
        heap[i] = element
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
            heap[i] = childElement
            onIndexChanged(childElement, i)
            i = childIndex
        }
        heap[i] = element
        return i
    }

    internal companion object {
        private val EMPTY_ARRAY = ByteArray(0)
        private const val DEFAULT_CAPACITY = 8
    }
}

public fun AbstractBytePriorityQueue.removeAll(predicate: BytePredicate): Boolean = filterInPlace { predicate.test(it) }
public fun AbstractBytePriorityQueue.retainAll(predicate: BytePredicate): Boolean = filterInPlace { !predicate.test(it) }

/**
 * A priority queue of Bytes, backed by a binary heap, which can operate in descending (largest element at the front
 * of the queue) or ascending (smallest element at the front of the queue) order.
 *
 * The [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * Methods all conform to normal array based binary heap performance expectations, and [remove] operations take O(N)
 * (linear) time with respect to queue size. If an indirect heap implementation is desired (with faster remove
 * operations and the option to change an element's priority), one can be implemented by subclassing
 * [AbstractBytePriorityQueue] and implementing [onIndexChanged] to store an element ⟷ index mapping.
 */
public class BytePriorityQueue(private val descending: Boolean) : AbstractBytePriorityQueue() {

    public constructor() : this(false)

    @JvmOverloads
    public constructor(initialCapacity: Int, descending: Boolean = false) : this(descending) {
        ensureCapacity(initialCapacity)
    }

    @JvmOverloads
    public constructor(array: ByteArray, fromIndex: Int = 0, toIndex: Int = array.size, descending: Boolean = false) : this(descending) {
        addAll(array, fromIndex, toIndex)
    }

    @JvmOverloads
    public constructor(elements: ByteCollection, descending: Boolean = false) : this(descending) {
        addAll(elements)
    }

    @JvmOverloads
    public constructor(elements: Collection<Byte>, descending: Boolean = false) : this(descending) {
        addAll(elements)
    }

    override fun isHigherPriority(element1: Byte, element2: Byte): Boolean {
        val d = element1.compareTo(element2)
        return if (descending) d > 0 else d < 0
    }
}

/**
 * Returns a priority queue of Bytes using the given priority function.
 */
@JvmSynthetic
public inline fun BytePriorityQueue(
    initialCapacity: Int = 0,
    crossinline isHigherPriority: (Byte, Byte) -> Boolean,
): AbstractBytePriorityQueue {
    return object : AbstractBytePriorityQueue(initialCapacity) {
        override fun isHigherPriority(element1: Byte, element2: Byte): Boolean {
            return isHigherPriority(element1, element2)
        }
    }
}
