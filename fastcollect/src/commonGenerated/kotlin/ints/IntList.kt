package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.random.Random

public fun emptyIntList(): IntList = EmptyIntList

public fun intListOf(): IntList = EmptyIntList
public fun intListOf(element: Int): IntList = SingletonIntList(element)
public fun intListOf(vararg elements: Int): IntList = IntArrayDeque(elements)

public fun mutableIntListOf(): MutableIntList = IntArrayDeque()
public fun mutableIntListOf(element: Int): MutableIntList = IntArrayDeque(1).apply { add(element) }
public fun mutableIntListOf(vararg elements: Int): MutableIntList = IntArrayDeque(elements)

public fun IntArray.asIntList(): IntList = IntArrayListWrapper(this)

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildIntList(expectedSize: Int = 0, builderAction: MutableIntList.() -> Unit): IntList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = IntArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun IntList(size: Int, init: (index: Int) -> Int): IntList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableIntList(size, init)
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableIntList(size: Int, init: (index: Int) -> Int): MutableIntList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = IntArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Ints.
 */
public interface IntList : IntCollection {
    public fun listIterator(): IntListIterator
    public fun listIterator(index: Int): IntListIterator

    override fun contains(element: Int): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Int

    override fun containsAll(elements: Collection<Int>): Boolean = super.containsAll(elements)

    public fun indexOf(element: Int): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextInt() equalsBoxed element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Int): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previousInt() equalsBoxed element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): IntList
}

public val IntList.lastIndex: Int inline get() = size - 1

public fun IntList.rangeCheck(index: Int, size: Int = this.size): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun IntList.rangeCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun IntList.rangeCheck(fromIndex: Int, toIndex: Int, size: Int = this.size) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> IntList.foldRight(initial: R, operation: (Int, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previousInt(), accumulated)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun IntList.reduceRight(operation: (accumulated: Int, Int) -> Int) : Int {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousInt()
    while (it.hasPrevious()) {
        accumulated = operation(accumulated, it.previousInt())
    }
    return accumulated
}

public fun IntList.asList(): List<Int> = IntListWrapper(this)

/**
 * A mutable list of Ints.
 */
public interface MutableIntList : IntList, MutableIntCollection {
    override fun listIterator(): MutableIntListIterator
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
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Int>): Boolean {
        if (elements is IntCollection) return addAll(elements)
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: IntCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Int>) {
        if (elements is IntCollection) {
            addAll(index, elements)
            return
        }
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    override fun removeAll(elements: Collection<Int>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Int>): Boolean = super.retainAll(elements)

    public fun sort() {
        val sorted = toIntArray().also { it.sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toIntArray().also { it.sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Int) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableIntList
}

public fun MutableIntList.asMutableList(): MutableList<Int> = MutableIntListWrapper(this)

public abstract class AbstractIntList : AbstractIntCollection(), IntList {

    override fun iterator(): IntIterator = IteratorImpl()
    override fun listIterator(): IntListIterator = listIterator(0)
    override fun listIterator(index: Int): IntListIterator = ListIteratorImpl(index)

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
                if (!(it.nextInt() equalsBoxed otherIt.nextInt())) {
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

    private inner class IteratorImpl: IntIterator() {

        private var index = 0

        override fun nextInt(): Int {
            if (index == size) throw NoSuchElementException()
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean = index != size
    }

    private inner class ListIteratorImpl(private var index: Int = 0): IntListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        override fun previousInt(): Int {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            return value
        }

        override fun nextInt(): Int {
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

    private open class IntSubList(private val list: IntList, fromIndex: Int, toIndex: Int) : AbstractIntList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Int {
            rangeCheck(index)
            return list.get(index + offset)
        }
    }

    private class RandomAccessIntSubList(list: IntList, fromIndex: Int, toIndex: Int) : IntSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableIntList : AbstractIntList(), MutableIntList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextInt()
            it.remove()
        }
    }

    override fun iterator(): MutableIntIterator = IteratorImpl()
    override fun listIterator(): MutableIntListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableIntListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableIntList {
        return if (this is RandomAccess) {
            RandomAccessIntSubList(this, fromIndex, toIndex)
        } else {
            IntSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableIntIterator() {
        private var index = 0
        private var lastIndex = -1

        override fun nextInt(): Int {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableIntListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        private var lastIndex = -1

        override fun previousInt(): Int {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextInt(): Int {
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

        override fun set(element: Int) {
            check(lastIndex >= 0)
            set(lastIndex, element)
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
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Int) {
            return list.set(rangeCheck(index) + offset, element)
        }

        override fun get(index: Int): Int {
            return list.get(rangeCheck(index) + offset)
        }

        override fun add(index: Int, element: Int) {
            list.add(rangeCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Int {
            val result = list.removeAt(rangeCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: IntCollection) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Int>) {
            if (elements is IntCollection) {
                addAll(index, elements)
                return
            }
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
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

    override fun get(index: Int): Int = throw IndexOutOfBoundsException()
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
    override fun contains(element: Int): Boolean = value equalsBoxed element

    override fun get(index: Int): Int = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Int): Int = if (element equalsBoxed value) 0 else -1
    override fun lastIndexOf(element: Int): Int = if (element equalsBoxed value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): IntList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyIntList
    }
}

private class IntArrayListWrapper(private val array: IntArray): AbstractIntList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Int = array[index]
}

private class IntListWrapper(private val list: IntList) : AbstractList<Int>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Int) = list.contains(element)

    override fun indexOf(element: Int) = list.indexOf(element)
    override fun lastIndexOf(element: Int) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
}

private class MutableIntListWrapper(private val list: MutableIntList) : AbstractMutableList<Int>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Int) = list.contains(element)

    override fun indexOf(element: Int) = list.indexOf(element)
    override fun lastIndexOf(element: Int) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)

    override fun set(index: Int, element: Int) = list.replace(index, element)

    override fun add(element: Int) = list.add(element)
    override fun add(index: Int, element: Int) = list.add(index, element)

    override fun remove(element: Int): Boolean = list.remove(element)
    override fun removeAt(index: Int): Int = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Int> = list.subList(fromIndex, toIndex).asMutableList()
}
