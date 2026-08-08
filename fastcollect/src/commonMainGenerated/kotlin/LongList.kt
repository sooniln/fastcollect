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

public fun emptyLongList(): LongList = EmptyLongList

public fun longListOf(): LongList = EmptyLongList
public fun longListOf(element: Long): LongList = SingletonLongList(element)
public fun longListOf(vararg elements: Long): LongList = LongArrayDeque(elements)

public fun mutableLongListOf(): MutableLongList = LongArrayDeque()
public fun mutableLongListOf(element: Long): MutableLongList = LongArrayDeque(1).apply { add(element) }
public fun mutableLongListOf(vararg elements: Long): MutableLongList = LongArrayDeque(elements)

public fun LongArray.asLongList(): LongList = LongArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildLongList(expectedSize: Int = 0, builderAction: MutableLongList.() -> Unit): LongList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = LongArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun LongList(size: Int, init: (index: Int) -> Long): LongList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableLongList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableLongList(size: Int, init: (index: Int) -> Long): MutableLongList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = LongArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Longs.
 */
public interface LongList : LongCollection {
    public fun listIterator(): LongListIterator
    public fun listIterator(index: Int): LongListIterator

    override fun contains(element: Long): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Long

    override fun containsAll(elements: Collection<Long>): Boolean = super.containsAll(elements)

    public fun indexOf(element: Long): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextLong() equalsBoxed element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Long): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previousLong() equalsBoxed element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): LongList
}


public val LongList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun LongList.first(): Long = if (isEmpty()) throw NoSuchElementException() else this[0]

@JvmSynthetic
public fun LongList.last(): Long = if (isEmpty()) throw NoSuchElementException() else this[lastIndex]

public fun LongList.rangeCheck(index: Int, size: Int = this.size): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun LongList.rangeCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun LongList.rangeCheck(fromIndex: Int, toIndex: Int, size: Int = this.size) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> LongList.foldRight(initial: R, operation: (Long, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previousLong(), accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun LongList.reduceRight(operation: (Long, accumulated: Long) -> Long) : Long {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousLong()
    while (it.hasPrevious()) {
        accumulated = operation(it.previousLong(), accumulated)
    }
    return accumulated
}

public fun LongList.asList(): List<Long> = LongListWrapper(this)

/**
 * A mutable list of Longs.
 */
public interface MutableLongList : LongList, MutableLongCollection {
    override fun listIterator(): MutableLongListIterator
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
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Long>): Boolean {
        if (elements is LongCollection) return addAll(elements)
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: LongCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Long>) {
        if (elements is LongCollection) {
            addAll(index, elements)
            return
        }
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    override fun removeAll(elements: Collection<Long>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Long>): Boolean = super.retainAll(elements)

    public fun sort() {
        val sorted = toLongArray().also { it.sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toLongArray().also { it.sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Long) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableLongList
}

public fun MutableLongList.asMutableList(): MutableList<Long> = MutableLongListWrapper(this)

public abstract class AbstractLongList : AbstractLongCollection(), LongList {

    override fun iterator(): LongIterator = IteratorImpl()
    override fun listIterator(): LongListIterator = listIterator(0)
    override fun listIterator(index: Int): LongListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): LongList {
        return if (this is RandomAccess) {
            RandomAccessLongSubList(this, fromIndex, toIndex)
        } else {
            LongSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is LongList) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        while (it.hasNext() && otherIt.hasNext()) {
            if (!(it.nextLong() equalsBoxed otherIt.nextLong())) {
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

    private inner class IteratorImpl: LongIterator() {

        private var index = 0

        override fun nextLong(): Long {
            if (index == size) throw NoSuchElementException()
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean = index != size
    }

    private inner class ListIteratorImpl(private var index: Int = 0): LongListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        override fun previousLong(): Long {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            return value
        }

        override fun nextLong(): Long {
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

    private open class LongSubList(private val list: LongList, fromIndex: Int, toIndex: Int) : AbstractLongList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Long {
            rangeCheck(index)
            return list.get(index + offset)
        }
    }

    private class RandomAccessLongSubList(list: LongList, fromIndex: Int, toIndex: Int) : LongSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableLongList : AbstractLongList(), MutableLongList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextLong()
            it.remove()
        }
    }

    override fun iterator(): MutableLongIterator = IteratorImpl()
    override fun listIterator(): MutableLongListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableLongListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableLongList {
        return if (this is RandomAccess) {
            RandomAccessLongSubList(this, fromIndex, toIndex)
        } else {
            LongSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableLongIterator() {
        private var index = 0
        private var lastIndex = -1

        override fun nextLong(): Long {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableLongListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        private var lastIndex = -1

        override fun previousLong(): Long {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextLong(): Long {
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

        override fun set(element: Long) {
            check(lastIndex >= 0)
            set(lastIndex, element)
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
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Long) {
            return list.set(rangeCheck(index) + offset, element)
        }

        override fun get(index: Int): Long {
            return list.get(rangeCheck(index) + offset)
        }

        override fun add(index: Int, element: Long) {
            list.add(rangeCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Long {
            val result = list.removeAt(rangeCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: LongCollection) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Long>) {
            if (elements is LongCollection) {
                addAll(index, elements)
                return
            }
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
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

    override fun get(index: Int): Long = throw IndexOutOfBoundsException()
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
    override fun contains(element: Long): Boolean = value equalsBoxed element

    override fun get(index: Int): Long = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Long): Int = if (element equalsBoxed value) 0 else -1
    override fun lastIndexOf(element: Long): Int = if (element equalsBoxed value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): LongList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyLongList
    }
}

private class LongArrayListWrapper(private val array: LongArray): AbstractLongList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Long = array[index]
}

private class LongListWrapper(private val list: LongList) : AbstractList<Long>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Long) = list.contains(element)

    override fun indexOf(element: Long) = list.indexOf(element)
    override fun lastIndexOf(element: Long) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
}

private class MutableLongListWrapper(private val list: MutableLongList) : AbstractMutableList<Long>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Long) = list.contains(element)

    override fun indexOf(element: Long) = list.indexOf(element)
    override fun lastIndexOf(element: Long) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)

    override fun set(index: Int, element: Long) = list.replace(index, element)

    override fun add(element: Long) = list.add(element)
    override fun add(index: Int, element: Long) = list.add(index, element)

    override fun remove(element: Long): Boolean = list.remove(element)
    override fun removeAt(index: Int): Long = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Long> = list.subList(fromIndex, toIndex).asMutableList()
}
