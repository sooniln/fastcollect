/**
 * Methods for dealing with IntArrayDeques.
 */
@file:JvmName("IntArrayDeques")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.math.max
import kotlin.math.min

public typealias IntArrayList = IntArrayDeque

/**
 * An array based [Deque](https://en.wikipedia.org/wiki/Double-ended_queue) implementation for storing Ints.
 *
 * This implementation supports amortized O(1) `addFirst/addLast/removeFirst/removeLast` functionality. The
 * [ensureCapacity]/[trimToSize] methods can be used to manage the size of the backing array.
 *
 * The extension method [asList] produces a thin wrapper around this class which exposes it as Kotlin list which can be
 * used anywhere a Kotlin list is expected. Using this wrapper may incur boxing penalties.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public class IntArrayDeque private constructor(array: IntArray, size: Int) : AbstractMutableIntList(), RandomAccess {

    private var head: Int = 0
    private var ring: IntArray = array

    @get:JvmName("size")
    override var size: Int = size
        private set

    public constructor() : this(EMPTY_ARRAY, size = 0)

    public constructor(capacity: Int) : this(if (capacity == 0) EMPTY_ARRAY else { require(capacity > 0); IntArray(capacity) }, 0)

    public constructor(elements: IntCollection) : this(elements.toArray(), size = elements.size)

    public constructor(elements: Collection<Int>) : this(elements.toIntArray(), size = elements.size)

    public constructor(elements: IntArray) : this(elements.copyOf(), size = elements.size)

    public constructor(elements: IntArray, fromIndex:Int, toIndex: Int) : this(elements.copyOfRange(fromIndex, toIndex), size = toIndex - fromIndex)

    public fun ensureCapacity(capacity: Int) {
        if (capacity > ring.size) grow(capacity)
    }

    private fun grow(capacity: Int) {
        val oldCapacity = ring.size
        val newCapacity = if (oldCapacity > 0) {
            growArraySize(oldCapacity, capacity - oldCapacity)
        } else {
            max(DEFAULT_CAPACITY, capacity)
        }

        if (head == 0) {
            ring = ring.copyOf(newCapacity)
        } else {
            ring = copyIntoInternal(IntArray(newCapacity))
            head = 0
        }
    }

    public fun trimToSize() {
        if (size < ring.size) {
            ring = if (isEmpty()) EMPTY_ARRAY else copyIntoInternal(IntArray(size))
            head = 0
        }
    }

    override fun get(index: Int): Int {
        return ring[ring.position(head, indexCheck(index))]
    }

    override fun set(index: Int, element: Int) {
        ring[ring.position(head, indexCheck(index))] = element
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
        ring[ring.position(head, s)] = element
        size = newSize
    }

    override fun add(index: Int, element: Int) {
        indexCheckInclusive(index)
        when (index) {
            size -> addLast(element)
            0 -> addFirst(element)
            else -> addMiddle(index, element)
        }
    }

    private fun addMiddle(index: Int, element: Int) {
        val newSize = size + 1
        ensureCapacity(newSize)

        // attempt to shift a minimal number of elements depending on where index falls within the deque
        if (index < newSize shr 1) {
            // retreat head, then shift [1, index + 1) down onto [0, index)
            head = ring.decrementPosition(head)
            moveWithinRing(1, 0, index)
        } else {
            // shift [index, size) up onto [index + 1, size + 1)
            moveWithinRing(index, index + 1, size - index)
        }
        ring[ring.position(head, index)] = element
        size = newSize
    }

    private fun moveWithinRing(srcIndex: Int, dstIndex: Int, count: Int) {
        if (count == 0) return

        if (dstIndex < srcIndex) {
            // copy front to back
            var src = ring.position(head, srcIndex)
            var dst = ring.position(head, dstIndex)
            var remaining = count
            while (remaining > 0) {
                val chunk = min(remaining, min(ring.size - src, ring.size - dst))
                ring.copyInto(ring, dst, src, src + chunk)
                remaining -= chunk
                src = ring.positiveMod(src + chunk)
                dst = ring.positiveMod(dst + chunk)
            }
        } else {
            // copy back to front. positions are exclusive ends, so the end of the array is ring.size not 0.
            var srcEnd = head + srcIndex + count
            if (srcEnd > ring.size) srcEnd -= ring.size
            var dstEnd = head + dstIndex + count
            if (dstEnd > ring.size) dstEnd -= ring.size
            var remaining = count
            while (remaining > 0) {
                val chunk = min(remaining, min(srcEnd, dstEnd))
                srcEnd -= chunk
                dstEnd -= chunk
                ring.copyInto(ring, dstEnd, srcEnd, srcEnd + chunk)
                remaining -= chunk
                if (srcEnd == 0) srcEnd = ring.size
                if (dstEnd == 0) dstEnd = ring.size
            }
        }
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
        return ring[ring.position(head, --size)]
    }

    override fun removeAt(index: Int): Int {
        indexCheck(index)
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
        val index = ring.index(head, position)
        // attempt to shift a minimal number of elements depending on where index falls within the deque
        if (index < size shr 1) {
            // shift [0, index) up onto [1, index + 1), then advance head
            moveWithinRing(0, 1, index)
            head = ring.incrementPosition(head)
            --size
            return 0
        } else {
            // shift [index + 1, size) down onto [index, size - 1)
            moveWithinRing(index + 1, index, size - index - 1)
            --size
            return -1
        }
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val sizeDelta = toIndex - fromIndex
        if (sizeDelta == 0) return

        // attempt to shift a minimal number of elements depending on where the range falls within the deque
        if (fromIndex <= size - toIndex) {
            // shift [0, fromIndex) up onto [sizeDelta, toIndex), then advance head
            moveWithinRing(0, sizeDelta, fromIndex)
            head = ring.position(head, sizeDelta)
        } else {
            // shift [toIndex, size) down onto [fromIndex, fromIndex + remaining)
            moveWithinRing(toIndex, fromIndex, size - toIndex)
        }
        size -= sizeDelta
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
            if (ring[i] equalsRaw element) return i - head
        }
        return -1
    }

    private fun indexOfDiscrete(tail: Int, element: Int): Int {
        for (i in head..<ring.size) {
            if (ring[i] equalsRaw element) return i - head
        }
        for (i in 0..<tail-ring.size) {
            if (ring[i] equalsRaw element) return i + ring.size - head
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
            if (ring[i] equalsRaw element) return i - head
            --i
        }
        return -1
    }

    private fun lastIndexOfDiscrete(tail: Int, element: Int): Int {
        // kotlin produces inefficient bytecode for downTo for some reason, so we use a manual loop
        val head = head
        var i = tail - ring.size
        while (i >= 0) {
            if (ring[i] equalsRaw element) return i + ring.size - head
            --i
        }
        i = ring.size - 1
        while (i >= head) {
            if (ring[i] equalsRaw element) return i - head
            --i
        }
        return -1
    }

    public fun addAll(elements: IntArrayDeque): Boolean {
        val count = elements.size
        if (count == 0) return false

        val newSize = size + count
        ensureCapacity(newSize)

        // the free space starting at the tail is contiguous until the end of the ring
        val start = ring.position(head, size)
        val firstRun = ring.size - start
        if (count <= firstRun) {
            elements.copyIntoInternal(ring, start, 0, count)
        } else {
            elements.copyIntoInternal(ring, start, 0, firstRun)
            elements.copyIntoInternal(ring, 0, firstRun, count)
        }
        size = newSize

        return true
    }

    override fun addAll(elements: IntCollection): Boolean {
        if (elements is IntArrayDeque) return addAll(elements)
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        elements.foreach { element ->
            addLast(element)
        }
        return true
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        if (elements.isEmpty()) return false

        ensureCapacity(size + elements.size)
        for (element in elements) {
            addLast(element)
        }
        return true
    }

    public override fun removeAll(elements: IntCollection): Boolean {
        return filterInPlace { e -> elements.contains(e) }
    }

    public override fun removeAll(elements: Collection<Int>): Boolean {
        return filterInPlace { e -> elements.contains(e) }
    }

    public override fun retainAll(elements: IntCollection): Boolean {
        return filterInPlace { e -> !elements.contains(e) }
    }

    public override fun retainAll(elements: Collection<Int>): Boolean {
        return filterInPlace { e -> !elements.contains(e) }
    }

    @JvmSynthetic
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
                head -= end
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
        var j = ring.position(head, size - 1)
        repeat(midPoint) {
            val tmp = ring[i]
            ring[i] = ring[j]
            ring[j] = tmp

            i = ring.incrementPosition(i)
            j = ring.decrementPosition(j)
        }
    }

    override fun copyInto(destination: IntArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): IntArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return copyIntoInternal(destination, destinationOffset, fromIndex, toIndex)
    }

    private fun copyIntoInternal(dest: IntArray, destinationOffset: Int = 0, fromIndex: Int = 0, toIndex: Int = size): IntArray {
        if (toIndex == fromIndex) return dest

        val length = toIndex - fromIndex
        val start = ring.position(head, fromIndex)
        val end = ring.size - start
        if (length <= end) {
            ring.copyInto(dest, destinationOffset, start, start + length)
        } else {
            ring.copyInto(dest, destinationOffset, start, ring.size)
            ring.copyInto(dest, destinationOffset + end, 0, length - end)
        }
        return dest
    }

    override fun iterator(): MutableIntIterator {
        val tail = head + size
        return if (tail > ring.size) {
            DiscreteIterator()
        } else {
            ContinuousIterator(tail)
        }
    }

    override fun traverser(): MutableIntTraverser {
        val tail = head + size
        return if (tail > ring.size) {
            DiscreteTraverser()
        } else {
            ContinuousTraverser(tail)
        }
    }

    override fun traverser(position: Int): MutableIntListTraverser = ListTraverser(position)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IntList) return false

        if (size != other.size) return false
        if (other is RandomAccess) {
            for (i in 0..<size) {
                if (!(ring[ring.position(head, i)] equalsRaw other[i])) return false
            }
        } else {
            val it = other.iterator()
            var i = 0
            while (it.hasNext()) {
                if (!(it.nextInt() equalsRaw this[i++])) return false
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

    private inner class ContinuousIterator(private var tail: Int) : MutableIntIterator() {
        private val ring = this@IntArrayDeque.ring

        init {
            check(tail <= ring.size)
        }

        private var position = head
        private var previousPosition = -1

        override fun hasNext() = position < tail

        override fun nextInt(): Int {
            if (!hasNext()) throw NoSuchElementException()
            if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()

            previousPosition = position++
            return ring[previousPosition]
        }

        override fun remove() {
            check(previousPosition >= 0)
            if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()

            val d = removeAtInternal(previousPosition)
            tail += d
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
            if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()

            --remaining
            previousPosition = position
            position = ring.incrementPosition(position)
            return ring[previousPosition]
        }

        override fun remove() {
            check(previousPosition >= 0)
            if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()

            val d = removeAtInternal(previousPosition)
            position = ring.negativeMod(position + d)
            previousPosition = -1
        }
    }

    private inner class ContinuousTraverser(tail: Int) : MutableIntTraverser {
         private val ring = this@IntArrayDeque.ring

         init {
             check(tail <= ring.size)
         }

         private var last = tail - 1
         private var cursor = head - 1
         private var position = -1

         override val value: Int get() {
             check(position >= 0)
             return ring[position]
         }

         override fun forward(): Boolean {
             if (cursor == last) {
                 return false
             }

             if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()
             position = ++cursor
             return true
         }

         override fun remove() {
             check(position >= 0)
             if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()

             val d = removeAtInternal(position)
             last = ring.negativeMod(last + d)
             cursor = ring.negativeMod(cursor + d)
             position = -1
         }
     }

     private inner class DiscreteTraverser : MutableIntTraverser {
         private val ring = this@IntArrayDeque.ring

         private var remaining = size
         private var cursor = head - 1
         private var position = -1

         override val value: Int get() {
             check(position >= 0)
             return ring[position]
         }

         override fun forward(): Boolean {
             if (remaining <= 0) {
                 return false
             }

             if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()
             --remaining
             cursor = ring.incrementPosition(cursor)
             position = cursor
             return true
         }

         override fun remove() {
             check(position >= 0)
             if (ring !== this@IntArrayDeque.ring) throw ConcurrentModificationException()

             val d = removeAtInternal(position)
             cursor = ring.negativeMod(cursor + d)
             position = -1
         }
     }

    private inner class ListTraverser(position: Int) : MutableIntListTraverser {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@IntArrayDeque.size
        private var cursor = if (position == 0) head - 1 else ring.position(head, position - 1)
        private var ringPosition = if (position == 0) -1 else cursor

        override var position: Int = position
            private set

        override val value: Int get() {
            check(ringPosition >= 0)
            return ring[ringPosition]
        }

        override fun forward(): Boolean {
            if (position >= size) return false
            if (size != this@IntArrayDeque.size) throw ConcurrentModificationException()

            cursor = ring.incrementPosition(cursor)
            ringPosition = cursor
            ++position
            return true
        }

        override fun backward(): Boolean {
            if (position <= 0) return false
            if (size != this@IntArrayDeque.size) throw ConcurrentModificationException()

            ringPosition = cursor
            cursor = ring.decrementPosition(cursor)
            --position
            return true
        }

        override fun remove() {
            check(ringPosition >= 0)
            if (size != this@IntArrayDeque.size) throw ConcurrentModificationException()

            val index = ring.index(head, ringPosition)
            val d = removeAtInternal(ringPosition)
            position = index
            cursor = ring.negativeMod(ringPosition + d)
            ringPosition = -1
            --size
        }

        override fun set(value: Int) {
            check(ringPosition >= 0)
            if (size != this@IntArrayDeque.size) throw ConcurrentModificationException()
            ring[ringPosition] = value
        }

        override fun insert(value: Int) {
            if (size != this@IntArrayDeque.size) throw ConcurrentModificationException()
            add(position, value)
            cursor = ring.position(head, position++)
            ringPosition = -1
            ++size
        }
    }

    internal companion object {
        private val EMPTY_ARRAY = IntArray(0)
        private const val DEFAULT_CAPACITY = 8

        private fun IntArray.positiveMod(position: Int): Int = if (position < size) position else position - size

        private fun IntArray.negativeMod(position: Int): Int = if (position < 0) position + size else position

        private fun IntArray.position(head: Int, index: Int): Int = positiveMod(head + index)

        private fun IntArray.index(head: Int, position: Int): Int = negativeMod(position - head)

        private fun IntArray.incrementPosition(position: Int): Int {
            val next = position + 1
            return if (next == size) 0 else next
        }

        private fun IntArray.decrementPosition(position: Int): Int = if (position == 0) size - 1 else position - 1
    }
}


public fun IntArrayDeque.removeAll(predicate: IntPredicate): Boolean = filterInPlace { predicate.test(it) }
public fun IntArrayDeque.retainAll(predicate: IntPredicate): Boolean = filterInPlace { !predicate.test(it) }

