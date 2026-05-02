package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.assertBoxing
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.random.Random

public fun emptyLongList(): LongList = EmptyLongList

public fun longListOf(): LongList = EmptyLongList
public fun longListOf(element: Long): LongList = SingletonLongList(element)
public fun longListOf(vararg elements: Long): LongList = LongArrayDeque.wrap(elements)

public fun mutableLongListOf(): MutableLongList = LongArrayDeque()
public fun mutableLongListOf(element: Long): MutableLongList = LongArrayDeque(1).apply { add(element) }
public fun mutableLongListOf(vararg elements: Long): MutableLongList = LongArrayDeque.wrap(elements)

public fun LongArray.asLongList(): LongList = LongArrayListWrapper(this)

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildLongList(expectedSize: Int = 0, builderAction: MutableLongList.() -> Unit): LongList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val list = LongArrayDeque(expectedSize)
    list.builderAction()
    return list
}

public inline fun LongList(size: Int, init: (index: Int) -> Long): LongList = MutableLongList(size, init)

public inline fun MutableLongList(size: Int, init: (index: Int) -> Long): MutableLongList {
    val list = LongArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Longs which inherits from [List].
 */
public interface LongList : List<Long>, LongCollection {
    override fun listIterator(): LongListIterator
    override fun listIterator(index: Int): LongListIterator

    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Long): Boolean {
        return indexOf(element) != -1
    }

    @Deprecated(
        message = "Use getAt(index) instead.",
        replaceWith = ReplaceWith("getAt(index)"),
        level = DeprecationLevel.WARNING)
    override fun get(index: Int): Long = getAt(index)

    public fun getAt(index: Int): Long

    override fun containsAll(elements: Collection<Long>): Boolean = super.containsAll(elements)

    override fun indexOf(element: Long): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextLong() == element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    override fun lastIndexOf(element: Long): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previous() == element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    override fun subList(fromIndex: Int, toIndex: Int): LongList
}

/**
 * A mutable list of Longs which inherits from [MutableList].
 */
public interface MutableLongList : LongList, MutableLongCollection, MutableList<Long> {
    override fun listIterator(): MutableLongListIterator
    override fun listIterator(index: Int): MutableLongListIterator

    @Deprecated(
        message = "Use setAt(index, element) instead.",
        replaceWith = ReplaceWith("setAt(index, element)"),
        level = DeprecationLevel.WARNING)
    override fun set(index: Int, element: Long): Long {
        assertBoxing()
        val value = getAt(index)
        setAt(index, element)
        return value
    }

    public fun setAt(index: Int, element: Long)

    override fun add(element: Long): Boolean {
        add(size, element)
        return true
    }

    override fun add(index: Int, element: Long)

    public fun addFirst(element: Long): Unit = add(0, element)
    public fun addLast(element: Long): Unit = add(size, element)
    public fun removeFirst(): Long = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Long = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Long): Boolean {
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

    override fun addAll(elements: LongCollection): Boolean {
        return addAll(size, elements)
    }

    override fun addAll(elements: Collection<Long>): Boolean {
        return addAll(size, elements)
    }

    override fun removeAll(elements: Collection<Long>): Boolean {
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<Long>): Boolean {
        return super.retainAll(elements)
    }

    public fun addAll(index: Int, elements: LongCollection): Boolean
    override fun addAll(index: Int, elements: Collection<Long>): Boolean

    public fun sort() {
        val sorted = toLongArray().also { sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toLongArray().also { sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Long) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableLongList
}

public abstract class AbstractLongList : AbstractLongCollection(), LongList {

    override fun iterator(): LongIterator {
        return IteratorImpl()
    }

    override fun listIterator(): LongListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): LongListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): LongList {
        return if (this is RandomAccess) {
            RandomAccessLongSubList(this, fromIndex, toIndex)
        } else {
            LongSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        if (otherIt is LongIterator) {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextLong() != otherIt.nextLong()) {
                    return false
                }
            }
        } else {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextLong() != otherIt.next()) {
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

    private inner class IteratorImpl(private var index: Int = 0): LongIterator() {

        override fun nextLong(): Long {
            val value = getAt(index)
            index++
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }
    }

    private inner class ListIteratorImpl(private var index: Int = 0): LongListIterator() {

        override fun previousLong(): Long {
            val i = index - 1
            val value = getAt(i)
            index = i
            return value
        }

        override fun nextLong(): Long {
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

    private open class LongSubList(private val list: LongList, fromIndex: Int, toIndex: Int) : AbstractLongList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun getAt(index: Int): Long {
            return list.getAt(index + offset)
        }
    }

    private class RandomAccessLongSubList(list: LongList, fromIndex: Int, toIndex: Int) : LongSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableLongList : AbstractLongList(), MutableLongList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        require(fromIndex <= toIndex)
        rangeCheck(fromIndex)
        rangeCheckForAdd(toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextLong()
            it.remove()
        }
    }

    override fun addAll(index: Int, elements: LongCollection): Boolean {
        var index = rangeCheckForAdd(index)
        var modified = false
        for (element in elements) {
            add(index++, element)
            modified = true
        }
        return modified
    }

    override fun addAll(index: Int, elements: Collection<Long>): Boolean {
        if (elements is LongCollection) {
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

    override fun iterator(): MutableLongIterator {
        return IteratorImpl()
    }

    override fun listIterator(): MutableLongListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): MutableLongListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableLongList {
        return if (this is RandomAccess) {
            RandomAccessLongSubList(this, fromIndex, toIndex)
        } else {
            LongSubList(this, fromIndex, toIndex)
        }
    }

    protected fun rangeCheckForAdd(index: Int): Int {
        if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
        return index
    }

    private inner class IteratorImpl(private var index: Int = 0): MutableLongIterator() {
        private var lastIndex = -1

        override fun nextLong(): Long {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableLongListIterator() {

        private var lastIndex = -1

        override fun previousLong(): Long {
            val i = index - 1
            val value = getAt(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextLong(): Long {
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

        override fun set(element: Long) {
            check(lastIndex >= 0)
            setAt(lastIndex, element)
        }

        override fun add(element: Long) {
            val i = index
            add(i, element)
            lastIndex = -1
            index = i + 1
        }
    }

    private open class LongSubList(private val list: MutableLongList, fromIndex: Int, toIndex: Int) : AbstractMutableLongList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun setAt(index: Int, element: Long) {
            return list.setAt(index + offset, element)
        }

        override fun getAt(index: Int): Long {
            return list.getAt(index + offset)
        }

        override fun add(index: Int, element: Long) {
            list.add(index + offset, element)
            size++
        }

        override fun removeAt(index: Int): Long {
            val result = list.removeAt(index + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            list.removeRange(fromIndex + offset, toIndex + offset)
        }

        override fun addAll(index: Int, elements: LongCollection): Boolean {
            if (elements.isEmpty()) return false

            list.addAll(offset + index, elements)
            size += elements.size
            return true
        }
    }

    private class RandomAccessLongSubList(list: MutableLongList, fromIndex: Int, toIndex: Int) : LongSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyLongList : LongList, RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Long): Boolean = false
    override fun containsAll(elements: Collection<Long>): Boolean = elements.isEmpty()
    override fun containsAll(elements: LongCollection): Boolean = elements.isEmpty()

    override fun getAt(index: Int): Long = throw IndexOutOfBoundsException()
    override fun indexOf(element: Long): Int = -1
    override fun lastIndexOf(element: Long): Int = -1

    override fun iterator(): LongIterator = emptyLongIterator()
    override fun listIterator(): LongListIterator = emptyLongIterator()
    override fun listIterator(index: Int): LongListIterator = if (index == 0) listIterator() else throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): LongList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyLongList
    }
}

private class SingletonLongList(private val value: Long) : AbstractLongList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Long): Boolean = value == element

    override fun getAt(index: Int): Long = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Long): Int = if (element == value) 0 else -1
    override fun lastIndexOf(element: Long): Int = if (element == value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): LongList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyLongList
    }
}

private class LongArrayListWrapper(private val array: LongArray): AbstractLongList(), RandomAccess {
    override val size: Int get() = array.size
    override fun getAt(index: Int): Long = array[index]
}
