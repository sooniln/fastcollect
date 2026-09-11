/**
 * Methods for dealing with DoubleLists.
 */
@file:JvmName("DoubleLists")
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

public fun emptyDoubleList(): DoubleList = EmptyDoubleList

public fun doubleListOf(): DoubleList = EmptyDoubleList
public fun doubleListOf(element: Double): DoubleList = SingletonDoubleList(element)
public fun doubleListOf(vararg elements: Double): DoubleList = DoubleArrayDeque.wrap(elements)

public fun mutableDoubleListOf(): MutableDoubleList = DoubleArrayDeque()
public fun mutableDoubleListOf(element: Double): MutableDoubleList = DoubleArrayDeque(1).apply { add(element) }
public fun mutableDoubleListOf(vararg elements: Double): MutableDoubleList = DoubleArrayDeque.wrap(elements)

public fun DoubleArray.asDoubleList(): DoubleList = DoubleArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildDoubleList(expectedSize: Int = 0, builderAction: MutableDoubleList.() -> Unit): DoubleList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = DoubleArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleList(size: Int, init: (index: Int) -> Double): DoubleList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableDoubleList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableDoubleList(size: Int, init: (index: Int) -> Double): MutableDoubleList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = DoubleArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A random-access list of Doubles.
 */
public interface DoubleList : DoubleCollection, RandomAccess {

    public fun listIterator(): DoubleListIterator = listIterator(0)
    public fun listIterator(index: Int): DoubleListIterator

    override fun contains(element: Double): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Double

    public fun first(): Double = if (isEmpty()) throw NoSuchElementException() else get(0)

    public fun last(): Double = if (isEmpty()) throw NoSuchElementException() else get(lastIndex)

    public fun indexOf(element: Double): Int {
        for (index in 0..<size) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Double): Int {
        for (index in (size - 1) downTo 0) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): DoubleList

    override fun copyInto(destination: DoubleArray, destinationOffset: Int): DoubleArray {
        return copyInto(destination, destinationOffset, 0, size)
    }

    /**
     * Copies the elements of this list in the range [[fromIndex], [toIndex]) into [destination], starting at
     * [destinationOffset], and returns [destination]. Throw [IndexOutOfBoundsException] if [destination] does not have
     * enough room for all elements in the given range.
     */
    public fun copyInto(destination: DoubleArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): DoubleArray {
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

public val DoubleList.lastIndex: Int @JvmSynthetic inline get() = size - 1

public fun DoubleList.indexCheck(index: Int): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun DoubleList.indexCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun DoubleList.rangeCheck(fromIndex: Int, toIndex: Int) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmOverloads
public fun DoubleList.binarySearch(element: Double, fromIndex: Int = 0, toIndex: Int = size): Int {
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
public inline fun <R> DoubleList.foldRight(initial: R, operation: (accumulator: R, Double) -> R): R {
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
public inline fun DoubleList.reduceRight(operation: (accumulator: Double, Double) -> Double): Double {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    if (isEmpty()) throw NoSuchElementException()
    var index = size - 1
    var accumulator = get(index--)
    while (index >= 0) {
        accumulator = operation(accumulator, get(index--))
    }
    return accumulator
}

public fun DoubleList.asList(): List<Double> = DoubleListWrapper(this)

/**
 * A mutable list of Doubles.
 */
public interface MutableDoubleList : DoubleList, MutableDoubleCollection {

    override fun listIterator(): MutableDoubleListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableDoubleListIterator

    public operator fun set(index: Int, element: Double)

    public fun replace(index: Int, element: Double): Double {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Double): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Double)

    public fun addFirst(element: Double): Unit = add(0, element)
    public fun addLast(element: Double): Unit = add(size, element)
    public fun removeFirst(): Double = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Double = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Double): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        } else {
            removeAt(index)
            return true
        }
    }

    public fun removeAt(index: Int): Double

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: DoubleCollection): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    override fun addAll(elements: Collection<Double>): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: DoubleCollection): Boolean {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: Collection<Double>): Boolean {
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

    public fun fill(element: Double) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableDoubleList
}

public fun MutableDoubleList.asList(): MutableList<Double> = MutableDoubleListWrapper(this)

public abstract class AbstractDoubleList : AbstractDoubleCollection(), DoubleList {

    override fun iterator(): DoubleIterator = IteratorImpl()
    override fun listIterator(index: Int): DoubleListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList = DoubleSubList(this, fromIndex, toIndex)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is DoubleList) return false
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

    private inner class IteratorImpl: DoubleIterator() {
        private val size = this@AbstractDoubleList.size
        private var index = 0

        override fun hasNext(): Boolean = index < size
        override fun nextDouble(): Double {
            if (index >= size) throw NoSuchElementException()
            if (size != this@AbstractDoubleList.size) throw ConcurrentModificationException()
            return get(index++)
        }
    }

    private inner class ListIteratorImpl(private var position: Int) : DoubleListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private val size = this@AbstractDoubleList.size

        override fun hasNext(): Boolean = position < size

        override fun nextDouble(): Double {
            if (position >= size) throw NoSuchElementException()
            if (size != this@AbstractDoubleList.size) throw ConcurrentModificationException()
            return get(position++)
        }

        override fun hasPrevious(): Boolean = position > 0

        override fun previousDouble(): Double {
            if (position <= 0) throw NoSuchElementException()
            if (size != this@AbstractDoubleList.size) throw ConcurrentModificationException()
            return get(--position)
        }

        override fun nextIndex(): Int = position
        override fun previousIndex(): Int = position - 1
    }

    private class DoubleSubList(private val list: DoubleList, fromIndex: Int, toIndex: Int) : AbstractDoubleList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Double {
            indexCheck(index)
            return list[index + offset]
        }

        override fun copyInto(destination: DoubleArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): DoubleArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

public abstract class AbstractMutableDoubleList : AbstractDoubleList(), MutableDoubleList {

    override fun iterator(): MutableDoubleIterator = IteratorImpl()
    override fun listIterator(index: Int): MutableDoubleListIterator = ListIteratorImpl(index)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val iterator = listIterator(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            iterator.previousDouble()
            iterator.remove()
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableDoubleList = DoubleSubList(this, fromIndex, toIndex)

    private inner class IteratorImpl: MutableDoubleIterator() {
        private var size = this@AbstractMutableDoubleList.size
        private var index = 0
        private var lastIndex = -1
        override fun hasNext(): Boolean = index != size
        override fun nextDouble(): Double {
            if (index == size) throw NoSuchElementException()
            if (size != this@AbstractMutableDoubleList.size) throw ConcurrentModificationException()
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

    private inner class ListIteratorImpl(private var position: Int) : MutableDoubleListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@AbstractMutableDoubleList.size
        private var index = -1

        override fun hasNext(): Boolean = position != size

        override fun nextDouble(): Double {
            if (position == size) throw NoSuchElementException()
            if (size != this@AbstractMutableDoubleList.size) throw ConcurrentModificationException()
            index = position++
            return get(index)
        }

        override fun hasPrevious(): Boolean = position != 0

        override fun previousDouble(): Double {
            if (position == 0) throw NoSuchElementException()
            if (size != this@AbstractMutableDoubleList.size) throw ConcurrentModificationException()
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

        override fun set(element: Double) {
            check(index != -1)
            set(index, element)
        }

        override fun add(element: Double) {
            add(position, element)
            ++position
            index = -1
            ++size
        }
    }

    private class DoubleSubList(private val list: MutableDoubleList, fromIndex: Int, toIndex: Int) : AbstractMutableDoubleList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Double) {
            return list.set(indexCheck(index) + offset, element)
        }

        override fun get(index: Int): Double {
            return list[indexCheck(index) + offset]
        }

        override fun add(index: Int, element: Double) {
            list.add(indexCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Double {
            val result = list.removeAt(indexCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: DoubleCollection): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun addAll(index: Int, elements: Collection<Double>): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun copyInto(destination: DoubleArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): DoubleArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

private object EmptyDoubleListIterator : DoubleListIterator() {
    override fun hasNext(): Boolean = false
    override fun nextDouble(): Double = throw NoSuchElementException()
    override fun hasPrevious(): Boolean = false
    override fun previousDouble(): Double = throw NoSuchElementException()
    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}

private object EmptyDoubleList : AbstractDoubleList() {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Double): Boolean = false
    override fun containsAll(elements: Collection<Double>): Boolean = elements.isEmpty()
    override fun containsAll(elements: DoubleCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Double = throw IndexOutOfBoundsException()
    override fun indexOf(element: Double): Int = -1
    override fun lastIndexOf(element: Double): Int = -1

    override fun iterator(): DoubleIterator = emptyDoubleIterator()
    override fun listIterator(index: Int): DoubleListIterator {
        indexCheckInclusive(index)
        return EmptyDoubleListIterator
    }

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        rangeCheck(fromIndex, toIndex)
        return EmptyDoubleList
    }
}

private class SingletonDoubleList(private val value: Double) : AbstractDoubleList() {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Double): Boolean = value equalsRaw element

    override fun get(index: Int): Double = if (index == 0) value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Double): Int = if (element equalsRaw value) 0 else -1
    override fun lastIndexOf(element: Double): Int = if (element equalsRaw value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyDoubleList
    }
}

private class DoubleArrayListWrapper(private val array: DoubleArray): AbstractDoubleList() {
    override val size: Int get() = array.size
    override fun get(index: Int): Double = array[index]

    override fun iterator(): DoubleIterator = object : DoubleIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index < array.size
        override fun nextDouble(): Double {
            if (index >= array.size) throw NoSuchElementException()
            return array[index++]
        }
    }

    override fun copyInto(destination: DoubleArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): DoubleArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return array.copyInto(destination, destinationOffset, fromIndex, toIndex)
    }
}

private class DoubleListWrapper(private val list: DoubleList) : AbstractList<Double>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Double) = list.contains(element)

    override fun indexOf(element: Double) = list.indexOf(element)
    override fun lastIndexOf(element: Double) = list.lastIndexOf(element)

    override fun iterator(): Iterator<Double> = IteratorWrapper(list.iterator())
    override fun listIterator(): ListIterator<Double> = ListIteratorWrapper(list.listIterator())
    override fun listIterator(index: Int): ListIterator<Double> = ListIteratorWrapper(list.listIterator(index))

    // wrappers exist to remove mutable operations from the underlying types
    private class IteratorWrapper(iterator: Iterator<Double>): Iterator<Double> by iterator
    private class ListIteratorWrapper(iterator: ListIterator<Double>): ListIterator<Double> by iterator
}

private class MutableDoubleListWrapper(private val list: MutableDoubleList) : AbstractMutableList<Double>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Double) = list.contains(element)

    override fun indexOf(element: Double) = list.indexOf(element)
    override fun lastIndexOf(element: Double) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Double> = list.iterator()
    override fun listIterator(index: Int): MutableListIterator<Double> = list.listIterator(index)

    override fun set(index: Int, element: Double) = list.replace(index, element)

    override fun add(element: Double) = list.add(element)
    override fun add(index: Int, element: Double) = list.add(index, element)

    override fun remove(element: Double): Boolean = list.remove(element)
    override fun removeAt(index: Int): Double = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Double> = list.subList(fromIndex, toIndex).asList()
}
