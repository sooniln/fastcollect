package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.assertBoxing
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.random.Random

public fun emptyIntList(): IntList = EmptyIntList

public fun intListOf(): IntList = EmptyIntList
public fun intListOf(element: Int): IntList = SingletonIntList(element)
public fun intListOf(vararg elements: Int): IntList = IntArrayDeque.wrap(elements)

public fun mutableIntListOf(): MutableIntList = IntArrayDeque()
public fun mutableIntListOf(element: Int): MutableIntList = IntArrayDeque(1).apply { add(element) }
public fun mutableIntListOf(vararg elements: Int): MutableIntList = IntArrayDeque.wrap(elements)

public fun IntArray.asIntList(): IntList = IntArrayListWrapper(this)

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildIntList(expectedSize: Int = 0, builderAction: MutableIntList.() -> Unit): IntList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val list = IntArrayDeque(expectedSize)
    list.builderAction()
    return list
}

public inline fun IntList(size: Int, init: (index: Int) -> Int): IntList = MutableIntList(size, init)

public inline fun MutableIntList(size: Int, init: (index: Int) -> Int): MutableIntList {
    val list = IntArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Ints which inherits from [List].
 */
public interface IntList : List<Int>, IntCollection {
    override fun listIterator(): IntListIterator
    override fun listIterator(index: Int): IntListIterator

    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Int): Boolean {
        return indexOf(element) != -1
    }

    @Deprecated(
        message = "Use getAt(index) instead.",
        replaceWith = ReplaceWith("getAt(index)"),
        level = DeprecationLevel.WARNING)
    override fun get(index: Int): Int = getAt(index)

    public fun getAt(index: Int): Int

    override fun containsAll(elements: Collection<Int>): Boolean = super.containsAll(elements)

    override fun indexOf(element: Int): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextInt() == element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    override fun lastIndexOf(element: Int): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previous() == element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    override fun subList(fromIndex: Int, toIndex: Int): IntList
}

/**
 * A mutable list of Ints which inherits from [MutableList].
 */
public interface MutableIntList : IntList, MutableIntCollection, MutableList<Int> {
    override fun listIterator(): MutableIntListIterator
    override fun listIterator(index: Int): MutableIntListIterator

    @Deprecated(
        message = "Use setAt(index, element) instead.",
        replaceWith = ReplaceWith("setAt(index, element)"),
        level = DeprecationLevel.WARNING)
    override fun set(index: Int, element: Int): Int {
        assertBoxing()
        val value = getAt(index)
        setAt(index, element)
        return value
    }

    public fun setAt(index: Int, element: Int)

    override fun add(element: Int): Boolean {
        add(size, element)
        return true
    }

    override fun add(index: Int, element: Int)

    public fun addFirst(element: Int): Unit = add(0, element)
    public fun addLast(element: Int): Unit = add(size, element)
    public fun removeFirst(): Int = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Int = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Int): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        }

        removeAt(index)
        return true
    }

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear() {
        removeRange(0, size)
    }

    override fun addAll(elements: IntCollection): Boolean {
        return addAll(size, elements)
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        return addAll(size, elements)
    }

    override fun removeAll(elements: Collection<Int>): Boolean {
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<Int>): Boolean {
        return super.retainAll(elements)
    }

    public fun addAll(index: Int, elements: IntCollection): Boolean
    override fun addAll(index: Int, elements: Collection<Int>): Boolean

    public fun sort() {
        val sorted = toIntArray().also { sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toIntArray().also { sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Int) {
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
            val copy = this.getAt(i)
            this.setAt(i, this.getAt(j))
            this.setAt(j, copy)
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableIntList
}

public abstract class AbstractIntList : AbstractIntCollection(), IntList {

    override fun iterator(): IntIterator {
        return IteratorImpl()
    }

    override fun listIterator(): IntListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): IntListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): IntList {
        return if (this is RandomAccess) {
            RandomAccessIntSubList(this, fromIndex, toIndex)
        } else {
            IntSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        if (otherIt is IntIterator) {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextInt() != otherIt.nextInt()) {
                    return false
                }
            }
        } else {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextInt() != otherIt.next()) {
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

    private inner class IteratorImpl(private var index: Int = 0): IntIterator() {

        override fun nextInt(): Int {
            val value = getAt(index)
            index++
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }
    }

    private inner class ListIteratorImpl(private var index: Int = 0): IntListIterator() {

        override fun previousInt(): Int {
            val i = index - 1
            val value = getAt(i)
            index = i
            return value
        }

        override fun nextInt(): Int {
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

    private open class IntSubList(private val list: IntList, fromIndex: Int, toIndex: Int) : AbstractIntList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun getAt(index: Int): Int {
            return list.getAt(index + offset)
        }
    }

    private class RandomAccessIntSubList(list: IntList, fromIndex: Int, toIndex: Int) : IntSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableIntList : AbstractIntList(), MutableIntList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        require(fromIndex <= toIndex)
        rangeCheck(fromIndex)
        rangeCheckForAdd(toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextInt()
            it.remove()
        }
    }

    override fun addAll(index: Int, elements: IntCollection): Boolean {
        var index = rangeCheckForAdd(index)
        var modified = false
        for (element in elements) {
            add(index++, element)
            modified = true
        }
        return modified
    }

    override fun addAll(index: Int, elements: Collection<Int>): Boolean {
        if (elements is IntCollection) {
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

    override fun iterator(): MutableIntIterator {
        return IteratorImpl()
    }

    override fun listIterator(): MutableIntListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): MutableIntListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableIntList {
        return if (this is RandomAccess) {
            RandomAccessIntSubList(this, fromIndex, toIndex)
        } else {
            IntSubList(this, fromIndex, toIndex)
        }
    }

    protected fun rangeCheckForAdd(index: Int): Int {
        if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
        return index
    }

    private inner class IteratorImpl(private var index: Int = 0): MutableIntIterator() {
        private var lastIndex = -1

        override fun nextInt(): Int {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableIntListIterator() {

        private var lastIndex = -1

        override fun previousInt(): Int {
            val i = index - 1
            val value = getAt(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextInt(): Int {
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

        override fun set(element: Int) {
            check(lastIndex >= 0)
            setAt(lastIndex, element)
        }

        override fun add(element: Int) {
            val i = index
            add(i, element)
            lastIndex = -1
            index = i + 1
        }
    }

    private open class IntSubList(private val list: MutableIntList, fromIndex: Int, toIndex: Int) : AbstractMutableIntList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun setAt(index: Int, element: Int) {
            return list.setAt(index + offset, element)
        }

        override fun getAt(index: Int): Int {
            return list.getAt(index + offset)
        }

        override fun add(index: Int, element: Int) {
            list.add(index + offset, element)
            size++
        }

        override fun removeAt(index: Int): Int {
            val result = list.removeAt(index + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            list.removeRange(fromIndex + offset, toIndex + offset)
        }

        override fun addAll(index: Int, elements: IntCollection): Boolean {
            if (elements.isEmpty()) return false

            list.addAll(offset + index, elements)
            size += elements.size
            return true
        }
    }

    private class RandomAccessIntSubList(list: MutableIntList, fromIndex: Int, toIndex: Int) : IntSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyIntList : IntList, RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Int): Boolean = false
    override fun containsAll(elements: Collection<Int>): Boolean = elements.isEmpty()
    override fun containsAll(elements: IntCollection): Boolean = elements.isEmpty()

    override fun getAt(index: Int): Int = throw IndexOutOfBoundsException()
    override fun indexOf(element: Int): Int = -1
    override fun lastIndexOf(element: Int): Int = -1

    override fun iterator(): IntIterator = emptyIntIterator()
    override fun listIterator(): IntListIterator = emptyIntIterator()
    override fun listIterator(index: Int): IntListIterator = if (index == 0) listIterator() else throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): IntList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyIntList
    }
}

private class SingletonIntList(private val value: Int) : AbstractIntList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Int): Boolean = value == element

    override fun getAt(index: Int): Int = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Int): Int = if (element == value) 0 else -1
    override fun lastIndexOf(element: Int): Int = if (element == value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): IntList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyIntList
    }
}

private class IntArrayListWrapper(private val array: IntArray): AbstractIntList(), RandomAccess {
    override val size: Int get() = array.size
    override fun getAt(index: Int): Int = array[index]
}
