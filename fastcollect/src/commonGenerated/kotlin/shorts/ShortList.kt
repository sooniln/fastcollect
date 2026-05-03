package io.github.sooniln.fastcollect.shorts

import io.github.sooniln.fastcollect.assertBoxing
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.random.Random

public fun emptyShortList(): ShortList = EmptyShortList

public fun shortListOf(): ShortList = EmptyShortList
public fun shortListOf(element: Short): ShortList = SingletonShortList(element)
public fun shortListOf(vararg elements: Short): ShortList = ShortArrayDeque.wrap(elements)

public fun mutableShortListOf(): MutableShortList = ShortArrayDeque()
public fun mutableShortListOf(element: Short): MutableShortList = ShortArrayDeque(1).apply { add(element) }
public fun mutableShortListOf(vararg elements: Short): MutableShortList = ShortArrayDeque.wrap(elements)

public fun ShortArray.asShortList(): ShortList = ShortArrayListWrapper(this)

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildShortList(expectedSize: Int = 0, builderAction: MutableShortList.() -> Unit): ShortList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = ShortArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun ShortList(size: Int, init: (index: Int) -> Short): ShortList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableShortList(size, init)
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableShortList(size: Int, init: (index: Int) -> Short): MutableShortList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = ShortArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Shorts which inherits from [List].
 */
public interface ShortList : List<Short>, ShortCollection {
    override fun listIterator(): ShortListIterator
    override fun listIterator(index: Int): ShortListIterator

    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Short): Boolean {
        return indexOf(element) != -1
    }

    @Deprecated(
        message = "Use getAt(index) instead.",
        replaceWith = ReplaceWith("getAt(index)"),
        level = DeprecationLevel.WARNING)
    override fun get(index: Int): Short = getAt(index)

    public fun getAt(index: Int): Short

    override fun containsAll(elements: Collection<Short>): Boolean = super.containsAll(elements)

    override fun indexOf(element: Short): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextShort() == element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    override fun lastIndexOf(element: Short): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previous() == element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    override fun subList(fromIndex: Int, toIndex: Int): ShortList
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> ShortList.foldRight(initial: R, operation: (Short, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previous(), accumulated)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun ShortList.reduceRight(operation: (accumulated: Short, Short) -> Short) : Short {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousShort()
    while (it.hasPrevious()) {
        accumulated = operation(accumulated, it.previousShort())
    }
    return accumulated
}

/**
 * A mutable list of Shorts which inherits from [MutableList].
 */
public interface MutableShortList : ShortList, MutableShortCollection, MutableList<Short> {
    override fun listIterator(): MutableShortListIterator
    override fun listIterator(index: Int): MutableShortListIterator

    @Deprecated(
        message = "Use setAt(index, element) instead.",
        replaceWith = ReplaceWith("setAt(index, element)"),
        level = DeprecationLevel.WARNING)
    override fun set(index: Int, element: Short): Short {
        assertBoxing()
        val value = getAt(index)
        setAt(index, element)
        return value
    }

    public fun setAt(index: Int, element: Short)

    override fun add(element: Short): Boolean {
        addLast(element)
        return true
    }

    override fun add(index: Int, element: Short)

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

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear() {
        removeRange(0, size)
    }

    override fun addAll(elements: ShortCollection): Boolean {
        return addAll(size, elements)
    }

    override fun addAll(elements: Collection<Short>): Boolean {
        return addAll(size, elements)
    }

    override fun removeAll(elements: Collection<Short>): Boolean {
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<Short>): Boolean {
        return super.retainAll(elements)
    }

    public fun addAll(index: Int, elements: ShortCollection): Boolean
    override fun addAll(index: Int, elements: Collection<Short>): Boolean

    public fun sort() {
        val sorted = toShortArray().also { sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toShortArray().also { sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Short) {
        if (this is RandomAccess) {
            for (index in 0..lastIndex) {
                setAt(index, element)
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
            val tmp = getAt(i)
            setAt(i, getAt(j))
            setAt(j, tmp)
        }
    }

    public fun reverse() {
        val midPoint = (size / 2)
        if (midPoint < 1) return
        var j = size - 1
        for (i in 0..<midPoint) {
            val tmp = getAt(i)
            setAt(i, getAt(j))
            setAt(j, tmp)
            --j
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableShortList
}

public abstract class AbstractShortList : AbstractShortCollection(), ShortList {

    override fun iterator(): ShortIterator {
        return IteratorImpl()
    }

    override fun listIterator(): ShortListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): ShortListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): ShortList {
        return if (this is RandomAccess) {
            RandomAccessShortSubList(this, fromIndex, toIndex)
        } else {
            ShortSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        if (otherIt is ShortIterator) {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextShort() != otherIt.nextShort()) {
                    return false
                }
            }
        } else {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextShort() != otherIt.next()) {
                    return false
                }
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

    protected fun rangeCheck(index: Int): Int {
        if (index !in indices) throw IndexOutOfBoundsException("index=$index, size=$size")
        return index
    }

    protected fun rangeCheck(fromIndex: Int, toIndex: Int) {
        require(fromIndex <= toIndex)
        if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
        if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
    }

    private inner class IteratorImpl(private var index: Int = 0): ShortIterator() {

        override fun nextShort(): Short {
            val value = getAt(index)
            index++
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }
    }

    private inner class ListIteratorImpl(private var index: Int = 0): ShortListIterator() {

        override fun previousShort(): Short {
            val i = index - 1
            val value = getAt(i)
            index = i
            return value
        }

        override fun nextShort(): Short {
            val value = getAt(index)
            index++
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }

        override fun hasPrevious(): Boolean {
            return index != 0
        }

        override fun nextIndex(): Int {
            return index
        }

        override fun previousIndex(): Int {
            return index - 1
        }
    }

    private open class ShortSubList(private val list: ShortList, fromIndex: Int, toIndex: Int) : AbstractShortList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun getAt(index: Int): Short {
            return list.getAt(index + offset)
        }
    }

    private class RandomAccessShortSubList(list: ShortList, fromIndex: Int, toIndex: Int) : ShortSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableShortList : AbstractShortList(), MutableShortList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        require(fromIndex <= toIndex)
        rangeCheck(fromIndex)
        rangeCheckForAdd(toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextShort()
            it.remove()
        }
    }

    override fun addAll(index: Int, elements: ShortCollection): Boolean {
        var index = rangeCheckForAdd(index)
        var modified = false
        for (element in elements) {
            add(index++, element)
            modified = true
        }
        return modified
    }

    override fun addAll(index: Int, elements: Collection<Short>): Boolean {
        if (elements is ShortCollection) {
            return addAll(index, elements)
        }

        var index = rangeCheckForAdd(index)
        var modified = false
        for (element in elements) {
            add(index++, element)
            modified = true
        }
        return modified
    }

    override fun iterator(): MutableShortIterator {
        return IteratorImpl()
    }

    override fun listIterator(): MutableShortListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): MutableShortListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableShortList {
        return if (this is RandomAccess) {
            RandomAccessShortSubList(this, fromIndex, toIndex)
        } else {
            ShortSubList(this, fromIndex, toIndex)
        }
    }

    protected fun rangeCheckForAdd(index: Int): Int {
        if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
        return index
    }

    private inner class IteratorImpl(private var index: Int = 0): MutableShortIterator() {
        private var lastIndex = -1

        override fun nextShort(): Short {
            val i = index
            val value = getAt(i)
            lastIndex = i
            index = i + 1
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }

        override fun remove() {
            check(lastIndex >= 0)

            removeAt(lastIndex)
            if (lastIndex < index) index--
            lastIndex = -1
        }
    }

    private inner class ListIteratorImpl(private var index: Int = 0): MutableShortListIterator() {

        private var lastIndex = -1

        override fun previousShort(): Short {
            val i = index - 1
            val value = getAt(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextShort(): Short {
            val i = index
            val value = getAt(i)
            lastIndex = i
            index = i + 1
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }

        override fun hasPrevious(): Boolean {
            return index != 0
        }

        override fun nextIndex(): Int {
            return index
        }

        override fun previousIndex(): Int {
            return index - 1
        }

        override fun remove() {
            check(lastIndex >= 0)

            removeAt(lastIndex)
            if (lastIndex < index) index--
            lastIndex = -1
        }

        override fun set(element: Short) {
            check(lastIndex >= 0)
            setAt(lastIndex, element)
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
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun setAt(index: Int, element: Short) {
            return list.setAt(index + offset, element)
        }

        override fun getAt(index: Int): Short {
            return list.getAt(index + offset)
        }

        override fun add(index: Int, element: Short) {
            list.add(index + offset, element)
            size++
        }

        override fun removeAt(index: Int): Short {
            val result = list.removeAt(index + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            list.removeRange(fromIndex + offset, toIndex + offset)
        }

        override fun addAll(index: Int, elements: ShortCollection): Boolean {
            if (elements.isEmpty()) return false

            list.addAll(offset + index, elements)
            size += elements.size
            return true
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

    override fun getAt(index: Int): Short = throw IndexOutOfBoundsException()
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
    override fun contains(element: Short): Boolean = value == element

    override fun getAt(index: Int): Short = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Short): Int = if (element == value) 0 else -1
    override fun lastIndexOf(element: Short): Int = if (element == value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): ShortList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyShortList
    }
}

private class ShortArrayListWrapper(private val array: ShortArray): AbstractShortList(), RandomAccess {
    override val size: Int get() = array.size
    override fun getAt(index: Int): Short = array[index]
}
