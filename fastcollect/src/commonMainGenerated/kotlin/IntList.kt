/**
 * Methods for dealing with IntLists.
 */
@file:JvmName("IntLists")
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

public fun emptyIntList(): IntList = EmptyIntList

public fun intListOf(): IntList = EmptyIntList
public fun intListOf(element: Int): IntList = SingletonIntList(element)
public fun intListOf(vararg elements: Int): IntList = IntArrayDeque.wrap(elements)

public fun mutableIntListOf(): MutableIntList = IntArrayDeque()
public fun mutableIntListOf(element: Int): MutableIntList = IntArrayDeque(1).apply { add(element) }
public fun mutableIntListOf(vararg elements: Int): MutableIntList = IntArrayDeque.wrap(elements)

public fun IntArray.asIntList(): IntList = IntArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildIntList(expectedSize: Int = 0, builderAction: MutableIntList.() -> Unit): IntList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = IntArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntList(size: Int, init: (index: Int) -> Int): IntList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableIntList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableIntList(size: Int, init: (index: Int) -> Int): MutableIntList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = IntArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A random-access list of Ints.
 */
public interface IntList : IntCollection, RandomAccess {

    public fun listIterator(): IntListIterator = listIterator(0)
    public fun listIterator(index: Int): IntListIterator

    override fun contains(element: Int): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Int

    public fun first(): Int = if (isEmpty()) throw NoSuchElementException() else get(0)

    public fun last(): Int = if (isEmpty()) throw NoSuchElementException() else get(lastIndex)

    public fun indexOf(element: Int): Int {
        for (index in 0..<size) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Int): Int {
        for (index in (size - 1) downTo 0) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): IntList

    override fun copyInto(destination: IntArray, destinationOffset: Int): IntArray {
        return copyInto(destination, destinationOffset, 0, size)
    }

    /**
     * Copies the elements of this list in the range [[fromIndex], [toIndex]) into [destination], starting at
     * [destinationOffset], and returns [destination]. Throw [IndexOutOfBoundsException] if [destination] does not have
     * enough room for all elements in the given range.
     */
    public fun copyInto(destination: IntArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): IntArray {
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

public val IntList.lastIndex: Int @JvmSynthetic inline get() = size - 1

public fun IntList.indexCheck(index: Int): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun IntList.indexCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun IntList.rangeCheck(fromIndex: Int, toIndex: Int) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmOverloads
public fun IntList.binarySearch(element: Int, fromIndex: Int = 0, toIndex: Int = size): Int {
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
public inline fun <R> IntList.foldRight(initial: R, operation: (accumulator: R, Int) -> R): R {
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
public inline fun IntList.reduceRight(operation: (accumulator: Int, Int) -> Int): Int {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    if (isEmpty()) throw NoSuchElementException()
    var index = size - 1
    var accumulator = get(index--)
    while (index >= 0) {
        accumulator = operation(accumulator, get(index--))
    }
    return accumulator
}

public fun IntList.asList(): List<Int> = IntListWrapper(this)

/**
 * A mutable list of Ints.
 */
public interface MutableIntList : IntList, MutableIntCollection {

    override fun listIterator(): MutableIntListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableIntListIterator

    public operator fun set(index: Int, element: Int)

    public fun replace(index: Int, element: Int): Int {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Int): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Int)

    public fun addFirst(element: Int): Unit = add(0, element)
    public fun addLast(element: Int): Unit = add(size, element)
    public fun removeFirst(): Int = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Int = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Int): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        } else {
            removeAt(index)
            return true
        }
    }

    public fun removeAt(index: Int): Int

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: IntCollection): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: IntCollection): Boolean {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: Collection<Int>): Boolean {
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

    public fun fill(element: Int) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableIntList
}

public fun MutableIntList.asList(): MutableList<Int> = MutableIntListWrapper(this)

public abstract class AbstractIntList : AbstractIntCollection(), IntList {

    override fun iterator(): IntIterator = IteratorImpl()
    override fun listIterator(index: Int): IntListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): IntList = IntSubList(this, fromIndex, toIndex)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is IntList) return false
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

    private inner class IteratorImpl: IntIterator() {
        private val size = this@AbstractIntList.size
        private var index = 0

        override fun hasNext(): Boolean = index < size
        override fun nextInt(): Int {
            if (index >= size) throw NoSuchElementException()
            if (size != this@AbstractIntList.size) throw ConcurrentModificationException()
            return get(index++)
        }
    }

    private inner class ListIteratorImpl(private var position: Int) : IntListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private val size = this@AbstractIntList.size

        override fun hasNext(): Boolean = position < size

        override fun nextInt(): Int {
            if (position >= size) throw NoSuchElementException()
            if (size != this@AbstractIntList.size) throw ConcurrentModificationException()
            return get(position++)
        }

        override fun hasPrevious(): Boolean = position > 0

        override fun previousInt(): Int {
            if (position <= 0) throw NoSuchElementException()
            if (size != this@AbstractIntList.size) throw ConcurrentModificationException()
            return get(--position)
        }

        override fun nextIndex(): Int = position
        override fun previousIndex(): Int = position - 1
    }

    private class IntSubList(private val list: IntList, fromIndex: Int, toIndex: Int) : AbstractIntList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Int {
            indexCheck(index)
            return list[index + offset]
        }

        override fun copyInto(destination: IntArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): IntArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

public abstract class AbstractMutableIntList : AbstractIntList(), MutableIntList {

    override fun iterator(): MutableIntIterator = IteratorImpl()
    override fun listIterator(index: Int): MutableIntListIterator = ListIteratorImpl(index)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val iterator = listIterator(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            iterator.previousInt()
            iterator.remove()
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableIntList = IntSubList(this, fromIndex, toIndex)

    private inner class IteratorImpl: MutableIntIterator() {
        private var size = this@AbstractMutableIntList.size
        private var index = 0
        private var lastIndex = -1
        override fun hasNext(): Boolean = index != size
        override fun nextInt(): Int {
            if (index == size) throw NoSuchElementException()
            if (size != this@AbstractMutableIntList.size) throw ConcurrentModificationException()
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

    private inner class ListIteratorImpl(private var position: Int) : MutableIntListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@AbstractMutableIntList.size
        private var index = -1

        override fun hasNext(): Boolean = position != size

        override fun nextInt(): Int {
            if (position == size) throw NoSuchElementException()
            if (size != this@AbstractMutableIntList.size) throw ConcurrentModificationException()
            index = position++
            return get(index)
        }

        override fun hasPrevious(): Boolean = position != 0

        override fun previousInt(): Int {
            if (position == 0) throw NoSuchElementException()
            if (size != this@AbstractMutableIntList.size) throw ConcurrentModificationException()
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

        override fun set(element: Int) {
            check(index != -1)
            set(index, element)
        }

        override fun add(element: Int) {
            add(position, element)
            ++position
            index = -1
            ++size
        }
    }

    private class IntSubList(private val list: MutableIntList, fromIndex: Int, toIndex: Int) : AbstractMutableIntList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Int) {
            return list.set(indexCheck(index) + offset, element)
        }

        override fun get(index: Int): Int {
            return list[indexCheck(index) + offset]
        }

        override fun add(index: Int, element: Int) {
            list.add(indexCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Int {
            val result = list.removeAt(indexCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: IntCollection): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun addAll(index: Int, elements: Collection<Int>): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun copyInto(destination: IntArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): IntArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

private object EmptyIntListIterator : IntListIterator() {
    override fun hasNext(): Boolean = false
    override fun nextInt(): Int = throw NoSuchElementException()
    override fun hasPrevious(): Boolean = false
    override fun previousInt(): Int = throw NoSuchElementException()
    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}

private object EmptyIntList : AbstractIntList() {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Int): Boolean = false
    override fun containsAll(elements: Collection<Int>): Boolean = elements.isEmpty()
    override fun containsAll(elements: IntCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Int = throw IndexOutOfBoundsException()
    override fun indexOf(element: Int): Int = -1
    override fun lastIndexOf(element: Int): Int = -1

    override fun iterator(): IntIterator = emptyIntIterator()
    override fun listIterator(index: Int): IntListIterator {
        indexCheckInclusive(index)
        return EmptyIntListIterator
    }

    override fun subList(fromIndex: Int, toIndex: Int): IntList {
        rangeCheck(fromIndex, toIndex)
        return EmptyIntList
    }
}

private class SingletonIntList(private val value: Int) : AbstractIntList() {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Int): Boolean = value equalsRaw element

    override fun get(index: Int): Int = if (index == 0) value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Int): Int = if (element equalsRaw value) 0 else -1
    override fun lastIndexOf(element: Int): Int = if (element equalsRaw value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): IntList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyIntList
    }
}

private class IntArrayListWrapper(private val array: IntArray): AbstractIntList() {
    override val size: Int get() = array.size
    override fun get(index: Int): Int = array[index]

    override fun iterator(): IntIterator = object : IntIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index < array.size
        override fun nextInt(): Int {
            if (index >= array.size) throw NoSuchElementException()
            return array[index++]
        }
    }

    override fun copyInto(destination: IntArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): IntArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return array.copyInto(destination, destinationOffset, fromIndex, toIndex)
    }
}

private class IntListWrapper(private val list: IntList) : AbstractList<Int>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Int) = list.contains(element)

    override fun indexOf(element: Int) = list.indexOf(element)
    override fun lastIndexOf(element: Int) = list.lastIndexOf(element)

    override fun iterator(): Iterator<Int> = IteratorWrapper(list.iterator())
    override fun listIterator(): ListIterator<Int> = ListIteratorWrapper(list.listIterator())
    override fun listIterator(index: Int): ListIterator<Int> = ListIteratorWrapper(list.listIterator(index))

    // wrappers exist to remove mutable operations from the underlying types
    private class IteratorWrapper(iterator: Iterator<Int>): Iterator<Int> by iterator
    private class ListIteratorWrapper(iterator: ListIterator<Int>): ListIterator<Int> by iterator
}

private class MutableIntListWrapper(private val list: MutableIntList) : AbstractMutableList<Int>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Int) = list.contains(element)

    override fun indexOf(element: Int) = list.indexOf(element)
    override fun lastIndexOf(element: Int) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Int> = list.iterator()
    override fun listIterator(index: Int): MutableListIterator<Int> = list.listIterator(index)

    override fun set(index: Int, element: Int) = list.replace(index, element)

    override fun add(element: Int) = list.add(element)
    override fun add(index: Int, element: Int) = list.add(index, element)

    override fun remove(element: Int): Boolean = list.remove(element)
    override fun removeAt(index: Int): Int = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Int> = list.subList(fromIndex, toIndex).asList()
}
