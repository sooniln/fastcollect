/**
 * Methods for dealing with primitive Lists.
 */
@file:JvmName("Lists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.random.Random

public fun emptyDoubleList(): DoubleList = EmptyDoubleList

public fun doubleListOf(): DoubleList = EmptyDoubleList
public fun doubleListOf(element: Double): DoubleList = SingletonDoubleList(element)
public fun doubleListOf(vararg elements: Double): DoubleList = DoubleArrayDeque(elements)

public fun mutableDoubleListOf(): MutableDoubleList = DoubleArrayDeque()
public fun mutableDoubleListOf(element: Double): MutableDoubleList = DoubleArrayDeque(1).apply { add(element) }
public fun mutableDoubleListOf(vararg elements: Double): MutableDoubleList = DoubleArrayDeque(elements)

public fun DoubleArray.asDoubleList(): DoubleList = DoubleArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildDoubleList(expectedSize: Int = 0, builderAction: MutableDoubleList.() -> Unit): DoubleList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = DoubleArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun DoubleList(size: Int, init: (index: Int) -> Double): DoubleList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableDoubleList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableDoubleList(size: Int, init: (index: Int) -> Double): MutableDoubleList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = DoubleArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Doubles.
 */
public interface DoubleList : DoubleCollection {
    public fun listIterator(): DoubleListIterator
    public fun listIterator(index: Int): DoubleListIterator

    override fun contains(element: Double): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Double

    override fun containsAll(elements: Collection<Double>): Boolean = super.containsAll(elements)

    public fun indexOf(element: Double): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextDouble() equalsBoxed element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Double): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previousDouble() equalsBoxed element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): DoubleList
}

public val DoubleList.indices: IntRange @JvmSynthetic inline get() = 0..<size

public val DoubleList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun DoubleList.first(): Double = if (isEmpty()) throw NoSuchElementException() else this[0]

@JvmSynthetic
public fun DoubleList.last(): Double = if (isEmpty()) throw NoSuchElementException() else this[lastIndex]

public fun DoubleList.rangeCheck(index: Int, size: Int = this.size): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun DoubleList.rangeCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun DoubleList.rangeCheck(fromIndex: Int, toIndex: Int, size: Int = this.size) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> DoubleList.foldRight(initial: R, operation: (Double, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previousDouble(), accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun DoubleList.reduceRight(operation: (Double, accumulated: Double) -> Double) : Double {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousDouble()
    while (it.hasPrevious()) {
        accumulated = operation(it.previousDouble(), accumulated)
    }
    return accumulated
}

public fun DoubleList.asList(): List<Double> = DoubleListWrapper(this)

/**
 * A mutable list of Doubles.
 */
public interface MutableDoubleList : DoubleList, MutableDoubleCollection {
    override fun listIterator(): MutableDoubleListIterator
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
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Double>): Boolean {
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: DoubleCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Double>) {
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    override fun removeAll(elements: Collection<Double>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Double>): Boolean = super.retainAll(elements)

    public fun sort() {
        val sorted = toDoubleArray().also { it.sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toDoubleArray().also { it.sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Double) {
        if (this is RandomAccess) {
            for (index in 0..lastIndex) {
                set(index, element)
            }
        } else {
            val it = listIterator()
            while (it.hasNext()) {
                it.next()
                it.set(element)
            }
        }
    }

    public fun shuffle() {
        for (i in lastIndex downTo 1) {
            val j = Random.nextInt(i + 1)
            val tmp = get(i)
            set(i, get(j))
            set(j, tmp)
        }
    }

    public fun reverse() {
        val midPoint = (size / 2)
        if (midPoint < 1) return
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

public fun MutableDoubleList.asMutableList(): MutableList<Double> = MutableDoubleListWrapper(this)

public abstract class AbstractDoubleList : AbstractDoubleCollection(), DoubleList {

    override fun iterator(): DoubleIterator = IteratorImpl()
    override fun listIterator(): DoubleListIterator = listIterator(0)
    override fun listIterator(index: Int): DoubleListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        return if (this is RandomAccess) {
            RandomAccessDoubleSubList(this, fromIndex, toIndex)
        } else {
            DoubleSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is DoubleList) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        while (it.hasNext() && otherIt.hasNext()) {
            if (!(it.nextDouble() equalsBoxed otherIt.nextDouble())) {
                return false
            }
        }
        return !(it.hasNext() || otherIt.hasNext())
    }

    override fun hashCode(): Int {
        var hashCode = 1
        for (element in this) {
            hashCode = 31 * hashCode + element.hashCode()
        }
        return hashCode
    }

    private inner class IteratorImpl: DoubleIterator() {

        private var index = 0

        override fun nextDouble(): Double {
            if (index == size) throw NoSuchElementException()
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean = index != size
    }

    private inner class ListIteratorImpl(private var index: Int = 0): DoubleListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        override fun previousDouble(): Double {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            return value
        }

        override fun nextDouble(): Double {
            if (index == size) throw NoSuchElementException()
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean = index != size
        override fun hasPrevious(): Boolean = index != 0

        override fun nextIndex(): Int = index
        override fun previousIndex(): Int = index - 1
    }

    private open class DoubleSubList(private val list: DoubleList, fromIndex: Int, toIndex: Int) : AbstractDoubleList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Double {
            rangeCheck(index)
            return list.get(index + offset)
        }
    }

    private class RandomAccessDoubleSubList(list: DoubleList, fromIndex: Int, toIndex: Int) : DoubleSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableDoubleList : AbstractDoubleList(), MutableDoubleList {

    override fun add(element: Double): Boolean = super<MutableDoubleList>.add(element)
    override fun remove(element: Double): Boolean = super<MutableDoubleList>.remove(element)
    override fun clear(): Unit = super<MutableDoubleList>.clear()
    override fun addAll(elements: Collection<Double>): Boolean = super<MutableDoubleList>.addAll(elements)
    override fun removeAll(elements: Collection<Double>): Boolean = super<MutableDoubleList>.removeAll(elements)
    override fun retainAll(elements: Collection<Double>): Boolean = super<MutableDoubleList>.retainAll(elements)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextDouble()
            it.remove()
        }
    }

    override fun iterator(): MutableDoubleIterator = IteratorImpl()
    override fun listIterator(): MutableDoubleListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableDoubleListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableDoubleList {
        return if (this is RandomAccess) {
            RandomAccessDoubleSubList(this, fromIndex, toIndex)
        } else {
            DoubleSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableDoubleIterator() {
        private var index = 0
        private var lastIndex = -1

        override fun nextDouble(): Double {
            if (index == size) throw NoSuchElementException()
            val i = index
            val value = get(i)
            lastIndex = i
            index = i + 1
            return value
        }

        override fun hasNext(): Boolean = index != size

        override fun remove() {
            check(lastIndex >= 0)

            removeAt(lastIndex)
            if (lastIndex < index) index--
            lastIndex = -1
        }
    }

    private inner class ListIteratorImpl(private var index: Int = 0): MutableDoubleListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        private var lastIndex = -1

        override fun previousDouble(): Double {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextDouble(): Double {
            if (index == size) throw NoSuchElementException()
            val i = index
            val value = get(i)
            lastIndex = i
            index = i + 1
            return value
        }

        override fun hasNext(): Boolean = index != size
        override fun hasPrevious(): Boolean = index != 0

        override fun nextIndex(): Int = index
        override fun previousIndex(): Int = index - 1

        override fun remove() {
            check(lastIndex >= 0)

            removeAt(lastIndex)
            if (lastIndex < index) index--
            lastIndex = -1
        }

        override fun set(element: Double) {
            check(lastIndex >= 0)
            set(lastIndex, element)
        }

        override fun add(element: Double) {
            val i = index
            add(i, element)
            lastIndex = -1
            index = i + 1
        }
    }

    private open class DoubleSubList(private val list: MutableDoubleList, fromIndex: Int, toIndex: Int) : AbstractMutableDoubleList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Double) {
            return list.set(rangeCheck(index) + offset, element)
        }

        override fun get(index: Int): Double {
            return list.get(rangeCheck(index) + offset)
        }

        override fun add(index: Int, element: Double) {
            list.add(rangeCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Double {
            val result = list.removeAt(rangeCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: DoubleCollection) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Double>) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }
    }

    private class RandomAccessDoubleSubList(list: MutableDoubleList, fromIndex: Int, toIndex: Int) : DoubleSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyDoubleList : DoubleList, RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Double): Boolean = false
    override fun containsAll(elements: Collection<Double>): Boolean = elements.isEmpty()
    override fun containsAll(elements: DoubleCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Double = throw IndexOutOfBoundsException()
    override fun indexOf(element: Double): Int = -1
    override fun lastIndexOf(element: Double): Int = -1

    override fun iterator(): DoubleIterator = emptyDoubleIterator()
    override fun listIterator(): DoubleListIterator = emptyDoubleIterator()
    override fun listIterator(index: Int): DoubleListIterator = if (index == 0) listIterator() else throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyDoubleList
    }
}

private class SingletonDoubleList(private val value: Double) : AbstractDoubleList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Double): Boolean = value equalsBoxed element

    override fun get(index: Int): Double = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Double): Int = if (element equalsBoxed value) 0 else -1
    override fun lastIndexOf(element: Double): Int = if (element equalsBoxed value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyDoubleList
    }
}

private class DoubleArrayListWrapper(private val array: DoubleArray): AbstractDoubleList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Double = array[index]
}

private class DoubleListWrapper(private val list: DoubleList) : AbstractList<Double>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Double) = list.contains(element)

    override fun indexOf(element: Double) = list.indexOf(element)
    override fun lastIndexOf(element: Double) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
}

private class MutableDoubleListWrapper(private val list: MutableDoubleList) : AbstractMutableList<Double>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Double) = list.contains(element)

    override fun indexOf(element: Double) = list.indexOf(element)
    override fun lastIndexOf(element: Double) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)

    override fun set(index: Int, element: Double) = list.replace(index, element)

    override fun add(element: Double) = list.add(element)
    override fun add(index: Int, element: Double) = list.add(index, element)

    override fun remove(element: Double): Boolean = list.remove(element)
    override fun removeAt(index: Int): Double = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Double> = list.subList(fromIndex, toIndex).asMutableList()
}
