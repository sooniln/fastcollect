/**
 * Methods for dealing with FloatLists.
 */
@file:JvmName("FloatLists")
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

public fun emptyFloatList(): FloatList = EmptyFloatList

public fun floatListOf(): FloatList = EmptyFloatList
public fun floatListOf(element: Float): FloatList = SingletonFloatList(element)
public fun floatListOf(vararg elements: Float): FloatList = FloatArrayDeque.wrap(elements)

public fun mutableFloatListOf(): MutableFloatList = FloatArrayDeque()
public fun mutableFloatListOf(element: Float): MutableFloatList = FloatArrayDeque(1).apply { add(element) }
public fun mutableFloatListOf(vararg elements: Float): MutableFloatList = FloatArrayDeque.wrap(elements)

public fun FloatArray.asFloatList(): FloatList = FloatArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildFloatList(expectedSize: Int = 0, builderAction: MutableFloatList.() -> Unit): FloatList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = FloatArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatList(size: Int, init: (index: Int) -> Float): FloatList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableFloatList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableFloatList(size: Int, init: (index: Int) -> Float): MutableFloatList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = FloatArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A random-access list of Floats.
 */
public interface FloatList : FloatCollection, RandomAccess {

    public fun listIterator(): FloatListIterator = listIterator(0)
    public fun listIterator(index: Int): FloatListIterator

    override fun contains(element: Float): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Float

    public fun first(): Float = if (isEmpty()) throw NoSuchElementException() else get(0)

    public fun last(): Float = if (isEmpty()) throw NoSuchElementException() else get(lastIndex)

    public fun indexOf(element: Float): Int {
        for (index in 0..<size) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Float): Int {
        for (index in (size - 1) downTo 0) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): FloatList

    override fun copyInto(destination: FloatArray, destinationOffset: Int): FloatArray {
        return copyInto(destination, destinationOffset, 0, size)
    }

    /**
     * Copies the elements of this list in the range [[fromIndex], [toIndex]) into [destination], starting at
     * [destinationOffset], and returns [destination]. Throw [IndexOutOfBoundsException] if [destination] does not have
     * enough room for all elements in the given range.
     */
    public fun copyInto(destination: FloatArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): FloatArray {
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

public val FloatList.lastIndex: Int @JvmSynthetic inline get() = size - 1

public fun FloatList.indexCheck(index: Int): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun FloatList.indexCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun FloatList.rangeCheck(fromIndex: Int, toIndex: Int) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmOverloads
public fun FloatList.binarySearch(element: Float, fromIndex: Int = 0, toIndex: Int = size): Int {
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
public inline fun <R> FloatList.foldRight(initial: R, operation: (accumulator: R, Float) -> R): R {
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
public inline fun FloatList.reduceRight(operation: (accumulator: Float, Float) -> Float): Float {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    if (isEmpty()) throw NoSuchElementException()
    var index = size - 1
    var accumulator = get(index--)
    while (index >= 0) {
        accumulator = operation(accumulator, get(index--))
    }
    return accumulator
}

public fun FloatList.asList(): List<Float> = FloatListWrapper(this)

/**
 * A mutable list of Floats.
 */
public interface MutableFloatList : FloatList, MutableFloatCollection {

    override fun listIterator(): MutableFloatListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableFloatListIterator

    public operator fun set(index: Int, element: Float)

    public fun replace(index: Int, element: Float): Float {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Float): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Float)

    public fun addFirst(element: Float): Unit = add(0, element)
    public fun addLast(element: Float): Unit = add(size, element)
    public fun removeFirst(): Float = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Float = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Float): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        } else {
            removeAt(index)
            return true
        }
    }

    public fun removeAt(index: Int): Float

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: FloatCollection): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    override fun addAll(elements: Collection<Float>): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: FloatCollection): Boolean {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: Collection<Float>): Boolean {
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

    public fun fill(element: Float) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableFloatList
}

public fun MutableFloatList.asList(): MutableList<Float> = MutableFloatListWrapper(this)

public abstract class AbstractFloatList : AbstractFloatCollection(), FloatList {

    override fun iterator(): FloatIterator = IteratorImpl()
    override fun listIterator(index: Int): FloatListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): FloatList = FloatSubList(this, fromIndex, toIndex)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FloatList) return false
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

    private inner class IteratorImpl: FloatIterator() {
        private val size = this@AbstractFloatList.size
        private var index = 0

        override fun hasNext(): Boolean = index < size
        override fun nextFloat(): Float {
            if (index >= size) throw NoSuchElementException()
            if (size != this@AbstractFloatList.size) throw ConcurrentModificationException()
            return get(index++)
        }
    }

    private inner class ListIteratorImpl(private var position: Int) : FloatListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private val size = this@AbstractFloatList.size

        override fun hasNext(): Boolean = position < size

        override fun nextFloat(): Float {
            if (position >= size) throw NoSuchElementException()
            if (size != this@AbstractFloatList.size) throw ConcurrentModificationException()
            return get(position++)
        }

        override fun hasPrevious(): Boolean = position > 0

        override fun previousFloat(): Float {
            if (position <= 0) throw NoSuchElementException()
            if (size != this@AbstractFloatList.size) throw ConcurrentModificationException()
            return get(--position)
        }

        override fun nextIndex(): Int = position
        override fun previousIndex(): Int = position - 1
    }

    private class FloatSubList(private val list: FloatList, fromIndex: Int, toIndex: Int) : AbstractFloatList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Float {
            indexCheck(index)
            return list[index + offset]
        }

        override fun copyInto(destination: FloatArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): FloatArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

public abstract class AbstractMutableFloatList : AbstractFloatList(), MutableFloatList {

    override fun iterator(): MutableFloatIterator = IteratorImpl()
    override fun listIterator(index: Int): MutableFloatListIterator = ListIteratorImpl(index)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val iterator = listIterator(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            iterator.previousFloat()
            iterator.remove()
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableFloatList = FloatSubList(this, fromIndex, toIndex)

    private inner class IteratorImpl: MutableFloatIterator() {
        private var size = this@AbstractMutableFloatList.size
        private var index = 0
        private var lastIndex = -1
        override fun hasNext(): Boolean = index != size
        override fun nextFloat(): Float {
            if (index == size) throw NoSuchElementException()
            if (size != this@AbstractMutableFloatList.size) throw ConcurrentModificationException()
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

    private inner class ListIteratorImpl(private var position: Int) : MutableFloatListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@AbstractMutableFloatList.size
        private var index = -1

        override fun hasNext(): Boolean = position != size

        override fun nextFloat(): Float {
            if (position == size) throw NoSuchElementException()
            if (size != this@AbstractMutableFloatList.size) throw ConcurrentModificationException()
            index = position++
            return get(index)
        }

        override fun hasPrevious(): Boolean = position != 0

        override fun previousFloat(): Float {
            if (position == 0) throw NoSuchElementException()
            if (size != this@AbstractMutableFloatList.size) throw ConcurrentModificationException()
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

        override fun set(element: Float) {
            check(index != -1)
            set(index, element)
        }

        override fun add(element: Float) {
            add(position, element)
            ++position
            index = -1
            ++size
        }
    }

    private class FloatSubList(private val list: MutableFloatList, fromIndex: Int, toIndex: Int) : AbstractMutableFloatList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Float) {
            return list.set(indexCheck(index) + offset, element)
        }

        override fun get(index: Int): Float {
            return list[indexCheck(index) + offset]
        }

        override fun add(index: Int, element: Float) {
            list.add(indexCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Float {
            val result = list.removeAt(indexCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: FloatCollection): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun addAll(index: Int, elements: Collection<Float>): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun copyInto(destination: FloatArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): FloatArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

private object EmptyFloatListIterator : FloatListIterator() {
    override fun hasNext(): Boolean = false
    override fun nextFloat(): Float = throw NoSuchElementException()
    override fun hasPrevious(): Boolean = false
    override fun previousFloat(): Float = throw NoSuchElementException()
    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}

private object EmptyFloatList : AbstractFloatList() {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Float): Boolean = false
    override fun containsAll(elements: Collection<Float>): Boolean = elements.isEmpty()
    override fun containsAll(elements: FloatCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Float = throw IndexOutOfBoundsException()
    override fun indexOf(element: Float): Int = -1
    override fun lastIndexOf(element: Float): Int = -1

    override fun iterator(): FloatIterator = emptyFloatIterator()
    override fun listIterator(index: Int): FloatListIterator {
        indexCheckInclusive(index)
        return EmptyFloatListIterator
    }

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        rangeCheck(fromIndex, toIndex)
        return EmptyFloatList
    }
}

private class SingletonFloatList(private val value: Float) : AbstractFloatList() {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Float): Boolean = value equalsRaw element

    override fun get(index: Int): Float = if (index == 0) value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Float): Int = if (element equalsRaw value) 0 else -1
    override fun lastIndexOf(element: Float): Int = if (element equalsRaw value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyFloatList
    }
}

private class FloatArrayListWrapper(private val array: FloatArray): AbstractFloatList() {
    override val size: Int get() = array.size
    override fun get(index: Int): Float = array[index]

    override fun iterator(): FloatIterator = object : FloatIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index < array.size
        override fun nextFloat(): Float {
            if (index >= array.size) throw NoSuchElementException()
            return array[index++]
        }
    }

    override fun copyInto(destination: FloatArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): FloatArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return array.copyInto(destination, destinationOffset, fromIndex, toIndex)
    }
}

private class FloatListWrapper(private val list: FloatList) : AbstractList<Float>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Float) = list.contains(element)

    override fun indexOf(element: Float) = list.indexOf(element)
    override fun lastIndexOf(element: Float) = list.lastIndexOf(element)

    override fun iterator(): Iterator<Float> = IteratorWrapper(list.iterator())
    override fun listIterator(): ListIterator<Float> = ListIteratorWrapper(list.listIterator())
    override fun listIterator(index: Int): ListIterator<Float> = ListIteratorWrapper(list.listIterator(index))

    // wrappers exist to remove mutable operations from the underlying types
    private class IteratorWrapper(iterator: Iterator<Float>): Iterator<Float> by iterator
    private class ListIteratorWrapper(iterator: ListIterator<Float>): ListIterator<Float> by iterator
}

private class MutableFloatListWrapper(private val list: MutableFloatList) : AbstractMutableList<Float>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Float) = list.contains(element)

    override fun indexOf(element: Float) = list.indexOf(element)
    override fun lastIndexOf(element: Float) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Float> = list.iterator()
    override fun listIterator(index: Int): MutableListIterator<Float> = list.listIterator(index)

    override fun set(index: Int, element: Float) = list.replace(index, element)

    override fun add(element: Float) = list.add(element)
    override fun add(index: Int, element: Float) = list.add(index, element)

    override fun remove(element: Float): Boolean = list.remove(element)
    override fun removeAt(index: Int): Float = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Float> = list.subList(fromIndex, toIndex).asList()
}
