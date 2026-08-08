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

public fun emptyShortList(): ShortList = EmptyShortList

public fun shortListOf(): ShortList = EmptyShortList
public fun shortListOf(element: Short): ShortList = SingletonShortList(element)
public fun shortListOf(vararg elements: Short): ShortList = ShortArrayDeque(elements)

public fun mutableShortListOf(): MutableShortList = ShortArrayDeque()
public fun mutableShortListOf(element: Short): MutableShortList = ShortArrayDeque(1).apply { add(element) }
public fun mutableShortListOf(vararg elements: Short): MutableShortList = ShortArrayDeque(elements)

public fun ShortArray.asShortList(): ShortList = ShortArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildShortList(expectedSize: Int = 0, builderAction: MutableShortList.() -> Unit): ShortList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = ShortArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun ShortList(size: Int, init: (index: Int) -> Short): ShortList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableShortList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableShortList(size: Int, init: (index: Int) -> Short): MutableShortList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = ShortArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Shorts.
 */
public interface ShortList : ShortCollection {
    public fun listIterator(): ShortListIterator
    public fun listIterator(index: Int): ShortListIterator

    override fun contains(element: Short): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Short

    override fun containsAll(elements: Collection<Short>): Boolean = super.containsAll(elements)

    public fun indexOf(element: Short): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextShort() equalsBoxed element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Short): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previousShort() equalsBoxed element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): ShortList
}


public val ShortList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun ShortList.first(): Short = if (isEmpty()) throw NoSuchElementException() else this[0]

@JvmSynthetic
public fun ShortList.last(): Short = if (isEmpty()) throw NoSuchElementException() else this[lastIndex]

public fun ShortList.rangeCheck(index: Int, size: Int = this.size): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun ShortList.rangeCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun ShortList.rangeCheck(fromIndex: Int, toIndex: Int, size: Int = this.size) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> ShortList.foldRight(initial: R, operation: (Short, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previousShort(), accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun ShortList.reduceRight(operation: (Short, accumulated: Short) -> Short) : Short {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousShort()
    while (it.hasPrevious()) {
        accumulated = operation(it.previousShort(), accumulated)
    }
    return accumulated
}

public fun ShortList.asList(): List<Short> = ShortListWrapper(this)

/**
 * A mutable list of Shorts.
 */
public interface MutableShortList : ShortList, MutableShortCollection {
    override fun listIterator(): MutableShortListIterator
    override fun listIterator(index: Int): MutableShortListIterator

    public operator fun set(index: Int, element: Short)

    public fun replace(index: Int, element: Short): Short {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Short): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Short)

    public fun addFirst(element: Short): Unit = add(0, element)
    public fun addLast(element: Short): Unit = add(size, element)
    public fun removeFirst(): Short = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Short = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Short): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        } else {
            removeAt(index)
            return true
        }
    }

    public fun removeAt(index: Int): Short

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: ShortCollection): Boolean {
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Short>): Boolean {
        if (elements is ShortCollection) return addAll(elements)
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: ShortCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Short>) {
        if (elements is ShortCollection) {
            addAll(index, elements)
            return
        }
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    override fun removeAll(elements: Collection<Short>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Short>): Boolean = super.retainAll(elements)

    public fun sort() {
        val sorted = toShortArray().also { it.sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toShortArray().also { it.sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Short) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableShortList
}

public fun MutableShortList.asMutableList(): MutableList<Short> = MutableShortListWrapper(this)

public abstract class AbstractShortList : AbstractShortCollection(), ShortList {

    override fun iterator(): ShortIterator = IteratorImpl()
    override fun listIterator(): ShortListIterator = listIterator(0)
    override fun listIterator(index: Int): ShortListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): ShortList {
        return if (this is RandomAccess) {
            RandomAccessShortSubList(this, fromIndex, toIndex)
        } else {
            ShortSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ShortList) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        while (it.hasNext() && otherIt.hasNext()) {
            if (!(it.nextShort() equalsBoxed otherIt.nextShort())) {
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

    private inner class IteratorImpl: ShortIterator() {

        private var index = 0

        override fun nextShort(): Short {
            if (index == size) throw NoSuchElementException()
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean = index != size
    }

    private inner class ListIteratorImpl(private var index: Int = 0): ShortListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        override fun previousShort(): Short {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            return value
        }

        override fun nextShort(): Short {
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

    private open class ShortSubList(private val list: ShortList, fromIndex: Int, toIndex: Int) : AbstractShortList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Short {
            rangeCheck(index)
            return list.get(index + offset)
        }
    }

    private class RandomAccessShortSubList(list: ShortList, fromIndex: Int, toIndex: Int) : ShortSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableShortList : AbstractShortList(), MutableShortList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextShort()
            it.remove()
        }
    }

    override fun iterator(): MutableShortIterator = IteratorImpl()
    override fun listIterator(): MutableShortListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableShortListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableShortList {
        return if (this is RandomAccess) {
            RandomAccessShortSubList(this, fromIndex, toIndex)
        } else {
            ShortSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableShortIterator() {
        private var index = 0
        private var lastIndex = -1

        override fun nextShort(): Short {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableShortListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        private var lastIndex = -1

        override fun previousShort(): Short {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextShort(): Short {
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

        override fun set(element: Short) {
            check(lastIndex >= 0)
            set(lastIndex, element)
        }

        override fun add(element: Short) {
            val i = index
            add(i, element)
            lastIndex = -1
            index = i + 1
        }
    }

    private open class ShortSubList(private val list: MutableShortList, fromIndex: Int, toIndex: Int) : AbstractMutableShortList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Short) {
            return list.set(rangeCheck(index) + offset, element)
        }

        override fun get(index: Int): Short {
            return list.get(rangeCheck(index) + offset)
        }

        override fun add(index: Int, element: Short) {
            list.add(rangeCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Short {
            val result = list.removeAt(rangeCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: ShortCollection) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Short>) {
            if (elements is ShortCollection) {
                addAll(index, elements)
                return
            }
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }
    }

    private class RandomAccessShortSubList(list: MutableShortList, fromIndex: Int, toIndex: Int) : ShortSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyShortList : ShortList, RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Short): Boolean = false
    override fun containsAll(elements: Collection<Short>): Boolean = elements.isEmpty()
    override fun containsAll(elements: ShortCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Short = throw IndexOutOfBoundsException()
    override fun indexOf(element: Short): Int = -1
    override fun lastIndexOf(element: Short): Int = -1

    override fun iterator(): ShortIterator = emptyShortIterator()
    override fun listIterator(): ShortListIterator = emptyShortIterator()
    override fun listIterator(index: Int): ShortListIterator = if (index == 0) listIterator() else throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): ShortList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyShortList
    }
}

private class SingletonShortList(private val value: Short) : AbstractShortList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Short): Boolean = value equalsBoxed element

    override fun get(index: Int): Short = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Short): Int = if (element equalsBoxed value) 0 else -1
    override fun lastIndexOf(element: Short): Int = if (element equalsBoxed value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): ShortList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyShortList
    }
}

private class ShortArrayListWrapper(private val array: ShortArray): AbstractShortList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Short = array[index]
}

private class ShortListWrapper(private val list: ShortList) : AbstractList<Short>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Short) = list.contains(element)

    override fun indexOf(element: Short) = list.indexOf(element)
    override fun lastIndexOf(element: Short) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
}

private class MutableShortListWrapper(private val list: MutableShortList) : AbstractMutableList<Short>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Short) = list.contains(element)

    override fun indexOf(element: Short) = list.indexOf(element)
    override fun lastIndexOf(element: Short) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)

    override fun set(index: Int, element: Short) = list.replace(index, element)

    override fun add(element: Short) = list.add(element)
    override fun add(index: Int, element: Short) = list.add(index, element)

    override fun remove(element: Short): Boolean = list.remove(element)
    override fun removeAt(index: Int): Short = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Short> = list.subList(fromIndex, toIndex).asMutableList()
}
