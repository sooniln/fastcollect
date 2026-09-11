/**
 * Methods for dealing with ByteArrayDeques.
 */
@file:JvmName("ByteArrayDeques")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
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
@Suppress("INAPPLICABLE_JVM_NAME")
public class ByteArrayDeque private constructor(array: ByteArray, size: Int) : AbstractMutableByteList() {

    @PublishedApi
    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var head: Int = 0

    @PublishedApi
    @get:JvmSynthetic
    @set:JvmSynthetic
    internal var ring: ByteArray = array

    @get:JvmName("size")
    override var size: Int = size
        private set

    public constructor() : this(EMPTY_ARRAY, size = 0)

    public constructor(capacity: Int) : this(if (capacity == 0) EMPTY_ARRAY else { require(capacity > 0); ByteArray(capacity) }, 0)

    public constructor(elements: ByteCollection) : this(elements.toArray(), size = elements.size)

    public constructor(elements: Collection<Byte>) : this(elements.toByteArray(), size = elements.size)

    public constructor(elements: ByteArray) : this(elements.copyOf(), size = elements.size)

    public constructor(elements: ByteArray, fromIndex:Int, toIndex: Int) : this(elements.copyOfRange(fromIndex, toIndex), size = toIndex - fromIndex)

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
            ring = copyIntoInternal(ByteArray(newCapacity))
            head = 0
        }
    }

    public fun trimToSize() {
        if (size < ring.size) {
            ring = if (isEmpty()) EMPTY_ARRAY else copyIntoInternal(ByteArray(size))
            head = 0
        }
    }

    override fun get(index: Int): Byte {
        return ring[ring.position(head, indexCheck(index))]
    }

    override fun set(index: Int, element: Byte) {
        ring[ring.position(head, indexCheck(index))] = element
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
        indexCheckInclusive(index)
        when (index) {
            size -> addLast(element)
            0 -> addFirst(element)
            else -> addMiddle(index, element)
        }
    }

    private fun addMiddle(index: Int, element: Byte) {
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

    override fun indexOf(element: Byte): Int {
        val tail = head + size
        return if (tail <= ring.size) indexOfContinuous(tail, element) else indexOfDiscrete(tail, element)
    }

    private fun indexOfContinuous(tail: Int, element: Byte): Int {
        for (i in head..<tail) {
            if (ring[i] equalsRaw element) return i - head
        }
        return -1
    }

    private fun indexOfDiscrete(tail: Int, element: Byte): Int {
        for (i in head..<ring.size) {
            if (ring[i] equalsRaw element) return i - head
        }
        for (i in 0..<tail-ring.size) {
            if (ring[i] equalsRaw element) return i + ring.size - head
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
            if (ring[i] equalsRaw element) return i - head
            --i
        }
        return -1
    }

    private fun lastIndexOfDiscrete(tail: Int, element: Byte): Int {
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

    public fun addAll(elements: ByteArrayDeque): Boolean {
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

    public override fun removeAll(elements: ByteCollection): Boolean {
        return filterInPlace { e -> elements.contains(e) }
    }

    public override fun removeAll(elements: Collection<Byte>): Boolean {
        return filterInPlace { e -> elements.contains(e) }
    }

    public override fun retainAll(elements: ByteCollection): Boolean {
        return filterInPlace { e -> !elements.contains(e) }
    }

    public override fun retainAll(elements: Collection<Byte>): Boolean {
        return filterInPlace { e -> !elements.contains(e) }
    }

    @JvmSynthetic
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

    override fun copyInto(destination: ByteArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): ByteArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return copyIntoInternal(destination, destinationOffset, fromIndex, toIndex)
    }

    private fun copyIntoInternal(dest: ByteArray, destinationOffset: Int = 0, fromIndex: Int = 0, toIndex: Int = size): ByteArray {
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

    override fun iterator(): MutableByteIterator = IteratorImpl()
    override fun listIterator(index: Int): MutableByteListIterator = ListIteratorImpl(index)

    /** Guaranteed to be as fast or faster than using [iterator] to iterate. */
    @JvmSynthetic
    public inline fun forEach(action: (Byte) -> Unit) {
        val ring = ring
        val tail = head + size
        if (tail > ring.size) {
            for (i in head..<ring.size) {
                action(ring[i])
            }
            for (i in 0..<(tail - ring.size)) {
                action(ring[i])
            }
        } else {
            for (i in head..<tail) {
                action(ring[i])
            }
        }
    }

    /** Guaranteed to be as fast or faster than using [listIterator] to iterate. */
    @JvmSynthetic
    public inline fun forEachReverse(action: (Byte) -> Unit) {
        val ring = ring
        val tail = head + size
        if (tail > ring.size) {
            var i = (tail - ring.size) - 1
            while (i >= 0) {
                action(ring[i--])
            }
            i = ring.size - 1
            while (i >= head) {
                action(ring[i--])
            }
        } else {
            var i = tail - 1
            while (i >= head) {
                action(ring[i--])
            }
        }
    }

    private inner class IteratorImpl : MutableByteIterator() {
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

    private inner class ListIteratorImpl(private var position: Int) : MutableByteListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private var ring = this@ByteArrayDeque.ring
        private var cursor = if (position == 0) head - 1 else ring.position(head, position - 1)
        private var ringPosition = if (position == 0) -1 else cursor

        override fun hasNext(): Boolean = position < this@ByteArrayDeque.size

        override fun nextByte(): Byte {
            if (position >= this@ByteArrayDeque.size) throw NoSuchElementException()
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()

            cursor = ring.incrementPosition(cursor)
            ringPosition = cursor
            ++position
            return ring[ringPosition]
        }

        override fun hasPrevious(): Boolean = position > 0

        override fun previousByte(): Byte {
            if (position <= 0) throw NoSuchElementException()
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()

            ringPosition = cursor
            cursor = ring.decrementPosition(cursor)
            --position
            return ring[ringPosition]
        }

        override fun nextIndex(): Int = position
        override fun previousIndex(): Int = position - 1

        override fun remove() {
            check(ringPosition >= 0)
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()

            val index = ring.index(head, ringPosition)
            val d = removeAtInternal(ringPosition)
            position = index
            cursor = ring.negativeMod(ringPosition + d)
            ringPosition = -1
        }

        override fun set(element: Byte) {
            check(ringPosition >= 0)
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()
            ring[ringPosition] = element
        }

        override fun add(element: Byte) {
            if (ring !== this@ByteArrayDeque.ring) throw ConcurrentModificationException()
            add(position, element)
            ring = this@ByteArrayDeque.ring
            cursor = ring.position(head, position++)
            ringPosition = -1
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

        @JvmSynthetic
        internal fun wrap(array: ByteArray): ByteArrayDeque = ByteArrayDeque(array, array.size)
    }
}

public fun ByteArrayDeque.removeAll(predicate: BytePredicate): Boolean = filterInPlace { predicate.test(it) }
public fun ByteArrayDeque.retainAll(predicate: BytePredicate): Boolean = filterInPlace { !predicate.test(it) }
