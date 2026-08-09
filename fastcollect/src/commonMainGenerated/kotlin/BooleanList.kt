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

public fun emptyBooleanList(): BooleanList = EmptyBooleanList

public fun booleanListOf(): BooleanList = EmptyBooleanList
public fun booleanListOf(element: Boolean): BooleanList = SingletonBooleanList(element)
public fun booleanListOf(vararg elements: Boolean): BooleanList = BooleanArrayDeque(elements)

public fun mutableBooleanListOf(): MutableBooleanList = BooleanArrayDeque()
public fun mutableBooleanListOf(element: Boolean): MutableBooleanList = BooleanArrayDeque(1).apply { add(element) }
public fun mutableBooleanListOf(vararg elements: Boolean): MutableBooleanList = BooleanArrayDeque(elements)

public fun BooleanArray.asBooleanList(): BooleanList = BooleanArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildBooleanList(expectedSize: Int = 0, builderAction: MutableBooleanList.() -> Unit): BooleanList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = BooleanArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun BooleanList(size: Int, init: (index: Int) -> Boolean): BooleanList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableBooleanList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableBooleanList(size: Int, init: (index: Int) -> Boolean): MutableBooleanList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = BooleanArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Booleans.
 */
public interface BooleanList : BooleanCollection {
    public fun listIterator(): BooleanListIterator
    public fun listIterator(index: Int): BooleanListIterator

    override fun contains(element: Boolean): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Boolean

    override fun containsAll(elements: Collection<Boolean>): Boolean = super.containsAll(elements)

    public fun indexOf(element: Boolean): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextBoolean() equalsBoxed element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Boolean): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previousBoolean() equalsBoxed element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): BooleanList
}


public val BooleanList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun BooleanList.first(): Boolean = if (isEmpty()) throw NoSuchElementException() else this[0]

@JvmSynthetic
public fun BooleanList.last(): Boolean = if (isEmpty()) throw NoSuchElementException() else this[lastIndex]

public fun BooleanList.rangeCheck(index: Int, size: Int = this.size): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun BooleanList.rangeCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun BooleanList.rangeCheck(fromIndex: Int, toIndex: Int, size: Int = this.size) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> BooleanList.foldRight(initial: R, operation: (Boolean, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previousBoolean(), accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun BooleanList.reduceRight(operation: (Boolean, accumulated: Boolean) -> Boolean) : Boolean {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousBoolean()
    while (it.hasPrevious()) {
        accumulated = operation(it.previousBoolean(), accumulated)
    }
    return accumulated
}

public fun BooleanList.asList(): List<Boolean> = BooleanListWrapper(this)

/**
 * A mutable list of Booleans.
 */
public interface MutableBooleanList : BooleanList, MutableBooleanCollection {
    override fun listIterator(): MutableBooleanListIterator
    override fun listIterator(index: Int): MutableBooleanListIterator

    public operator fun set(index: Int, element: Boolean)

    public fun replace(index: Int, element: Boolean): Boolean {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Boolean): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Boolean)

    public fun addFirst(element: Boolean): Unit = add(0, element)
    public fun addLast(element: Boolean): Unit = add(size, element)
    public fun removeFirst(): Boolean = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Boolean = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Boolean): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        } else {
            removeAt(index)
            return true
        }
    }

    public fun removeAt(index: Int): Boolean

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: BooleanCollection): Boolean {
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Boolean>): Boolean {
        if (elements is BooleanCollection) return addAll(elements)
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: BooleanCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Boolean>) {
        if (elements is BooleanCollection) {
            addAll(index, elements)
            return
        }
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    override fun removeAll(elements: Collection<Boolean>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Boolean>): Boolean = super.retainAll(elements)

    public fun sort() {
        val sorted = toBooleanArray().also { it.sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toBooleanArray().also { it.sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Boolean) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableBooleanList
}

public fun MutableBooleanList.asMutableList(): MutableList<Boolean> = MutableBooleanListWrapper(this)

public abstract class AbstractBooleanList : AbstractBooleanCollection(), BooleanList {

    override fun iterator(): BooleanIterator = IteratorImpl()
    override fun listIterator(): BooleanListIterator = listIterator(0)
    override fun listIterator(index: Int): BooleanListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): BooleanList {
        return if (this is RandomAccess) {
            RandomAccessBooleanSubList(this, fromIndex, toIndex)
        } else {
            BooleanSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is BooleanList) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        while (it.hasNext() && otherIt.hasNext()) {
            if (!(it.nextBoolean() equalsBoxed otherIt.nextBoolean())) {
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

    private inner class IteratorImpl: BooleanIterator() {

        private var index = 0

        override fun nextBoolean(): Boolean {
            if (index == size) throw NoSuchElementException()
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean = index != size
    }

    private inner class ListIteratorImpl(private var index: Int = 0): BooleanListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        override fun previousBoolean(): Boolean {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            return value
        }

        override fun nextBoolean(): Boolean {
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

    private open class BooleanSubList(private val list: BooleanList, fromIndex: Int, toIndex: Int) : AbstractBooleanList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Boolean {
            rangeCheck(index)
            return list.get(index + offset)
        }
    }

    private class RandomAccessBooleanSubList(list: BooleanList, fromIndex: Int, toIndex: Int) : BooleanSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableBooleanList : AbstractBooleanList(), MutableBooleanList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextBoolean()
            it.remove()
        }
    }

    override fun iterator(): MutableBooleanIterator = IteratorImpl()
    override fun listIterator(): MutableBooleanListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableBooleanListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableBooleanList {
        return if (this is RandomAccess) {
            RandomAccessBooleanSubList(this, fromIndex, toIndex)
        } else {
            BooleanSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableBooleanIterator() {
        private var index = 0
        private var lastIndex = -1

        override fun nextBoolean(): Boolean {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableBooleanListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        private var lastIndex = -1

        override fun previousBoolean(): Boolean {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextBoolean(): Boolean {
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

        override fun set(element: Boolean) {
            check(lastIndex >= 0)
            set(lastIndex, element)
        }

        override fun add(element: Boolean) {
            val i = index
            add(i, element)
            lastIndex = -1
            index = i + 1
        }
    }

    private open class BooleanSubList(private val list: MutableBooleanList, fromIndex: Int, toIndex: Int) : AbstractMutableBooleanList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Boolean) {
            return list.set(rangeCheck(index) + offset, element)
        }

        override fun get(index: Int): Boolean {
            return list.get(rangeCheck(index) + offset)
        }

        override fun add(index: Int, element: Boolean) {
            list.add(rangeCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Boolean {
            val result = list.removeAt(rangeCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: BooleanCollection) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Boolean>) {
            if (elements is BooleanCollection) {
                addAll(index, elements)
                return
            }
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }
    }

    private class RandomAccessBooleanSubList(list: MutableBooleanList, fromIndex: Int, toIndex: Int) : BooleanSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyBooleanList : BooleanList, RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Boolean): Boolean = false
    override fun containsAll(elements: Collection<Boolean>): Boolean = elements.isEmpty()
    override fun containsAll(elements: BooleanCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Boolean = throw IndexOutOfBoundsException()
    override fun indexOf(element: Boolean): Int = -1
    override fun lastIndexOf(element: Boolean): Int = -1

    override fun iterator(): BooleanIterator = emptyBooleanIterator()
    override fun listIterator(): BooleanListIterator = emptyBooleanIterator()
    override fun listIterator(index: Int): BooleanListIterator = if (index == 0) listIterator() else throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): BooleanList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyBooleanList
    }
}

private class SingletonBooleanList(private val value: Boolean) : AbstractBooleanList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Boolean): Boolean = value equalsBoxed element

    override fun get(index: Int): Boolean = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Boolean): Int = if (element equalsBoxed value) 0 else -1
    override fun lastIndexOf(element: Boolean): Int = if (element equalsBoxed value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): BooleanList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyBooleanList
    }
}

private class BooleanArrayListWrapper(private val array: BooleanArray): AbstractBooleanList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Boolean = array[index]
}

private class BooleanListWrapper(private val list: BooleanList) : AbstractList<Boolean>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Boolean) = list.contains(element)

    override fun indexOf(element: Boolean) = list.indexOf(element)
    override fun lastIndexOf(element: Boolean) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
}

private class MutableBooleanListWrapper(private val list: MutableBooleanList) : AbstractMutableList<Boolean>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Boolean) = list.contains(element)

    override fun indexOf(element: Boolean) = list.indexOf(element)
    override fun lastIndexOf(element: Boolean) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)

    override fun set(index: Int, element: Boolean) = list.replace(index, element)

    override fun add(element: Boolean) = list.add(element)
    override fun add(index: Int, element: Boolean) = list.add(index, element)

    override fun remove(element: Boolean): Boolean = list.remove(element)
    override fun removeAt(index: Int): Boolean = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Boolean> = list.subList(fromIndex, toIndex).asMutableList()
}
