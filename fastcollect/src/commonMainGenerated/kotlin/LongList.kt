/**
 * Methods for dealing with LongLists.
 */
@file:JvmName("LongLists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.random.Random

public fun emptyLongList(): LongList = EmptyLongList

public fun longListOf(): LongList = EmptyLongList
public fun longListOf(element: Long): LongList = SingletonLongList(element)
public fun longListOf(vararg elements: Long): LongList = LongArrayDeque.wrap(elements)

public fun mutableLongListOf(): MutableLongList = LongArrayDeque()
public fun mutableLongListOf(element: Long): MutableLongList = LongArrayDeque(1).apply { add(element) }
public fun mutableLongListOf(vararg elements: Long): MutableLongList = LongArrayDeque.wrap(elements)

public fun LongArray.asLongList(): LongList = LongArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildLongList(expectedSize: Int = 0, builderAction: MutableLongList.() -> Unit): LongList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = LongArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongList(size: Int, init: (index: Int) -> Long): LongList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableLongList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableLongList(size: Int, init: (index: Int) -> Long): MutableLongList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = LongArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A random-access list of Longs.
 */
public interface LongList : LongCollection, RandomAccess {

    public fun listIterator(): LongListIterator = listIterator(0)
    public fun listIterator(index: Int): LongListIterator

    override fun contains(element: Long): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Long

    public fun first(): Long = if (isEmpty()) throw NoSuchElementException() else get(0)

    public fun last(): Long = if (isEmpty()) throw NoSuchElementException() else get(lastIndex)

    public fun indexOf(element: Long): Int {
        for (index in 0..<size) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Long): Int {
        for (index in (size - 1) downTo 0) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): LongList

    override fun copyInto(destination: LongArray, destinationOffset: Int): LongArray {
        return copyInto(destination, destinationOffset, 0, size)
    }

    /**
     * Copies the elements of this list in the range [[fromIndex], [toIndex]) into [destination], starting at
     * [destinationOffset], and returns [destination]. Throw [IndexOutOfBoundsException] if [destination] does not have
     * enough room for all elements in the given range.
     */
    public fun copyInto(destination: LongArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): LongArray {
        rangeCheck(fromIndex, toIndex)
        val destinationToIndex = destinationOffset + toIndex - fromIndex
        destination.rangeCheck(destinationOffset, destinationToIndex)

        var destinationIndex = destinationOffset
        var index = fromIndex
        while (destinationIndex < destinationToIndex) {
            destination[destinationIndex++] = get(index++)
        }
        return destination
    }
}

public val LongList.lastIndex: Int @JvmSynthetic inline get() = size - 1

public fun LongList.indexCheck(index: Int): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun LongList.indexCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun LongList.rangeCheck(fromIndex: Int, toIndex: Int) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmOverloads
public fun LongList.binarySearch(element: Long, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(fromIndex, toIndex)

    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = low + (high - low) / 2
        val c = element.compareTo(get(mid))

        if (c > 0) {
            low = mid + 1
        } else if (c < 0) {
            high = mid - 1
        } else {
            return mid
        }
    }
    return -(low + 1)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> LongList.foldRight(initial: R, operation: (accumulator: R, Long) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    var accumulator = initial
    var index = size - 1
    while (index >= 0) {
        accumulator = operation(accumulator, get(index--))
    }
    return accumulator
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongList.reduceRight(operation: (accumulator: Long, Long) -> Long): Long {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    if (isEmpty()) throw NoSuchElementException()
    var index = size - 1
    var accumulator = get(index--)
    while (index >= 0) {
        accumulator = operation(accumulator, get(index--))
    }
    return accumulator
}

public fun LongList.asList(): List<Long> = LongListWrapper(this)

/**
 * A mutable list of Longs.
 */
public interface MutableLongList : LongList, MutableLongCollection {

    override fun listIterator(): MutableLongListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableLongListIterator

    public operator fun set(index: Int, element: Long)

    public fun replace(index: Int, element: Long): Long {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Long): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Long)

    public fun addFirst(element: Long): Unit = add(0, element)
    public fun addLast(element: Long): Unit = add(size, element)
    public fun removeFirst(): Long = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Long = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Long): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        } else {
            removeAt(index)
            return true
        }
    }

    public fun removeAt(index: Int): Long

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: LongCollection): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    override fun addAll(elements: Collection<Long>): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: LongCollection): Boolean {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: Collection<Long>): Boolean {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
        return !elements.isEmpty()
    }

    public fun sort() {
        val sorted = toArray().also { it.sort() }
        for (index in 0..<sorted.size) {
            set(index, sorted[index])
        }
    }

    public fun sortDescending() {
        val sorted = toArray().also { it.sortDescending() }
        for (index in 0..<sorted.size) {
            set(index, sorted[index])
        }
    }

    public fun fill(element: Long) {
        for (index in 0..<size) {
            set(index, element)
        }
    }

    @JvmSynthetic
    public fun shuffle(random: Random) {
        for (i in lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = get(i)
            set(i, get(j))
            set(j, tmp)
        }
    }

    public fun shuffle() { shuffle(Random) }

    public fun reverse() {
        val midPoint = (size / 2)
        var j = size - 1
        for (i in 0..<midPoint) {
            val tmp = get(i)
            set(i, get(j))
            set(j, tmp)
            --j
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableLongList
}

public fun MutableLongList.asList(): MutableList<Long> = MutableLongListWrapper(this)

public abstract class AbstractLongList : AbstractLongCollection(), LongList {

    override fun iterator(): LongIterator = IteratorImpl()
    override fun listIterator(index: Int): LongListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): LongList = LongSubList(this, fromIndex, toIndex)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is LongList) return false
        if (size != other.size) return false

        for (i in 0..<size) {
            if (this[i] notEqualsRaw other[i]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var hashCode = 1
        for (element in this) {
            hashCode = 31 * hashCode + element.hashCode()
        }
        return hashCode
    }

    private inner class IteratorImpl: LongIterator() {
        private val size = this@AbstractLongList.size
        private var index = 0

        override fun hasNext(): Boolean = index < size
        override fun nextLong(): Long {
            if (index >= size) throw NoSuchElementException()
            if (size != this@AbstractLongList.size) throw ConcurrentModificationException()
            return get(index++)
        }
    }

    private inner class ListIteratorImpl(private var position: Int) : LongListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private val size = this@AbstractLongList.size

        override fun hasNext(): Boolean = position < size

        override fun nextLong(): Long {
            if (position >= size) throw NoSuchElementException()
            if (size != this@AbstractLongList.size) throw ConcurrentModificationException()
            return get(position++)
        }

        override fun hasPrevious(): Boolean = position > 0

        override fun previousLong(): Long {
            if (position <= 0) throw NoSuchElementException()
            if (size != this@AbstractLongList.size) throw ConcurrentModificationException()
            return get(--position)
        }

        override fun nextIndex(): Int = position
        override fun previousIndex(): Int = position - 1
    }

    private class LongSubList(private val list: LongList, fromIndex: Int, toIndex: Int) : AbstractLongList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Long {
            indexCheck(index)
            return list[index + offset]
        }

        override fun copyInto(destination: LongArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): LongArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

public abstract class AbstractMutableLongList : AbstractLongList(), MutableLongList {

    override fun iterator(): MutableLongIterator = IteratorImpl()
    override fun listIterator(index: Int): MutableLongListIterator = ListIteratorImpl(index)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val iterator = listIterator(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            iterator.previousLong()
            iterator.remove()
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableLongList = LongSubList(this, fromIndex, toIndex)

    private inner class IteratorImpl: MutableLongIterator() {
        private var size = this@AbstractMutableLongList.size
        private var index = 0
        private var lastIndex = -1
        override fun hasNext(): Boolean = index != size
        override fun nextLong(): Long {
            if (index == size) throw NoSuchElementException()
            if (size != this@AbstractMutableLongList.size) throw ConcurrentModificationException()
            lastIndex = index++
            return get(lastIndex)
        }
        override fun remove() {
            check(lastIndex != -1)
            removeAt(lastIndex)
            index--
            lastIndex = -1
            --size
        }
    }

    private inner class ListIteratorImpl(private var position: Int) : MutableLongListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@AbstractMutableLongList.size
        private var index = -1

        override fun hasNext(): Boolean = position != size

        override fun nextLong(): Long {
            if (position == size) throw NoSuchElementException()
            if (size != this@AbstractMutableLongList.size) throw ConcurrentModificationException()
            index = position++
            return get(index)
        }

        override fun hasPrevious(): Boolean = position != 0

        override fun previousLong(): Long {
            if (position == 0) throw NoSuchElementException()
            if (size != this@AbstractMutableLongList.size) throw ConcurrentModificationException()
            index = --position
            return get(index)
        }

        override fun nextIndex(): Int = position
        override fun previousIndex(): Int = position - 1

        override fun remove() {
            check(index != -1)
            removeAt(index)
            if (position > index) --position
            index = -1
            --size
        }

        override fun set(element: Long) {
            check(index != -1)
            set(index, element)
        }

        override fun add(element: Long) {
            add(position, element)
            ++position
            index = -1
            ++size
        }
    }

    private class LongSubList(private val list: MutableLongList, fromIndex: Int, toIndex: Int) : AbstractMutableLongList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Long) {
            return list.set(indexCheck(index) + offset, element)
        }

        override fun get(index: Int): Long {
            return list[indexCheck(index) + offset]
        }

        override fun add(index: Int, element: Long) {
            list.add(indexCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Long {
            val result = list.removeAt(indexCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: LongCollection): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun addAll(index: Int, elements: Collection<Long>): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun copyInto(destination: LongArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): LongArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

private object EmptyLongListIterator : LongListIterator() {
    override fun hasNext(): Boolean = false
    override fun nextLong(): Long = throw NoSuchElementException()
    override fun hasPrevious(): Boolean = false
    override fun previousLong(): Long = throw NoSuchElementException()
    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}

private object EmptyLongList : AbstractLongList() {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Long): Boolean = false
    override fun containsAll(elements: Collection<Long>): Boolean = elements.isEmpty()
    override fun containsAll(elements: LongCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Long = throw IndexOutOfBoundsException()
    override fun indexOf(element: Long): Int = -1
    override fun lastIndexOf(element: Long): Int = -1

    override fun iterator(): LongIterator = emptyLongIterator()
    override fun listIterator(index: Int): LongListIterator {
        indexCheckInclusive(index)
        return EmptyLongListIterator
    }

    override fun subList(fromIndex: Int, toIndex: Int): LongList {
        rangeCheck(fromIndex, toIndex)
        return EmptyLongList
    }
}

private class SingletonLongList(private val value: Long) : AbstractLongList() {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Long): Boolean = value equalsRaw element

    override fun get(index: Int): Long = if (index == 0) value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Long): Int = if (element equalsRaw value) 0 else -1
    override fun lastIndexOf(element: Long): Int = if (element equalsRaw value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): LongList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyLongList
    }
}

private class LongArrayListWrapper(private val array: LongArray): AbstractLongList() {
    override val size: Int get() = array.size
    override fun get(index: Int): Long = array[index]

    override fun iterator(): LongIterator = object : LongIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index < array.size
        override fun nextLong(): Long {
            if (index >= array.size) throw NoSuchElementException()
            return array[index++]
        }
    }

    override fun copyInto(destination: LongArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): LongArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return array.copyInto(destination, destinationOffset, fromIndex, toIndex)
    }
}

private class LongListWrapper(private val list: LongList) : AbstractList<Long>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Long) = list.contains(element)

    override fun indexOf(element: Long) = list.indexOf(element)
    override fun lastIndexOf(element: Long) = list.lastIndexOf(element)

    override fun iterator(): Iterator<Long> = IteratorWrapper(list.iterator())
    override fun listIterator(): ListIterator<Long> = ListIteratorWrapper(list.listIterator())
    override fun listIterator(index: Int): ListIterator<Long> = ListIteratorWrapper(list.listIterator(index))

    // wrappers exist to remove mutable operations from the underlying types
    private class IteratorWrapper(iterator: Iterator<Long>): Iterator<Long> by iterator
    private class ListIteratorWrapper(iterator: ListIterator<Long>): ListIterator<Long> by iterator
}

private class MutableLongListWrapper(private val list: MutableLongList) : AbstractMutableList<Long>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Long) = list.contains(element)

    override fun indexOf(element: Long) = list.indexOf(element)
    override fun lastIndexOf(element: Long) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Long> = list.iterator()
    override fun listIterator(index: Int): MutableListIterator<Long> = list.listIterator(index)

    override fun set(index: Int, element: Long) = list.replace(index, element)

    override fun add(element: Long) = list.add(element)
    override fun add(index: Int, element: Long) = list.add(index, element)

    override fun remove(element: Long): Boolean = list.remove(element)
    override fun removeAt(index: Int): Long = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Long> = list.subList(fromIndex, toIndex).asList()
}
