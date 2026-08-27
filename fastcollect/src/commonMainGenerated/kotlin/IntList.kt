/**
 * Methods for dealing with primitive Lists.
 */
@file:JvmName("Lists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.random.Random

public fun emptyIntList(): IntList = EmptyIntList

public fun intListOf(): IntList = EmptyIntList
public fun intListOf(element: Int): IntList = SingletonIntList(element)
public fun intListOf(vararg elements: Int): IntList = IntArrayDeque(elements)

public fun mutableIntListOf(): MutableIntList = IntArrayDeque()
public fun mutableIntListOf(element: Int): MutableIntList = IntArrayDeque(1).apply { add(element) }
public fun mutableIntListOf(vararg elements: Int): MutableIntList = IntArrayDeque(elements)

public fun IntArray.asIntList(): IntList = IntArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildIntList(expectedSize: Int = 0, builderAction: MutableIntList.() -> Unit): IntList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = IntArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntList(size: Int, init: (index: Int) -> Int): IntList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableIntList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableIntList(size: Int, init: (index: Int) -> Int): MutableIntList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = IntArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

public interface IntListTraversable: IntTraversable {
    public fun traverser(position: Int): IntListTraverser
}

public interface MutableIntListTraversable: MutableIntTraversable {
    public fun traverser(position: Int): MutableIntListTraverser
}

public interface IntListTraverser : IntTraverser {
    public val position: Int
    public fun backward(): Boolean
}

public interface MutableIntListTraverser : IntListTraverser, MutableIntTraverser {
    public fun set(value: Int)
    public fun insert(value: Int)
}

@OptIn(ExperimentalContracts::class)
public inline fun IntList.foreachReverse(action: (Int) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    while (traverser.backward()) {
        action(traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun IntListTraversable.foreachIndexed(action: (Int, Int) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(0)
    var index = 0
    while (traverser.forward()) {
        action(index++, traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun IntList.foreachReverseIndexed(action: (Int, Int) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    var index = size
    while (traverser.backward()) {
        action(--index, traverser.value)
    }
}

/**
 * A list of Ints.
 */
public interface IntList : IntCollection, IntListTraversable {

    override fun contains(element: Int): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Int

    public fun indexOf(element: Int): Int {
        foreachIndexed { index, value ->
            if (value equalsBoxed element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Int): Int {
        foreachReverseIndexed { index, value ->
            if (value equalsBoxed element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): IntList
}

public val IntList.indices: IntRange @JvmSynthetic inline get() = 0..<size

public val IntList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun IntList.first(): Int = if (isEmpty()) throw NoSuchElementException() else this[0]

@JvmSynthetic
public fun IntList.last(): Int = if (isEmpty()) throw NoSuchElementException() else this[lastIndex]

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

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> IntList.foldRight(initial: R, operation: (Int, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreachReverse { value ->
        accumulated = operation(value, accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntList.reduceRight(operation: (Int, accumulated: Int) -> Int) : Int {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    if (!traverser.backward()) throw NoSuchElementException()
    var accumulated = traverser.value
    while (traverser.backward()) {
        accumulated = operation(traverser.value, accumulated)
    }
    return accumulated
}

public fun IntList.asList(): List<Int> = IntListWrapper(this)

/**
 * A mutable list of Ints.
 */
public interface MutableIntList : IntList, MutableIntCollection, MutableIntTraversable {

    override fun traverser(position: Int): MutableIntListTraverser

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
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: IntCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Int>) {
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    public fun sort() {
        val sorted = toIntArray().also { it.sort() }
        val traverser = traverser(0)
        for (element in sorted) {
            check(traverser.forward())
            traverser.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toIntArray().also { it.sortDescending() }
        val traverser = traverser(0)
        for (element in sorted) {
            check(traverser.forward())
            traverser.set(element)
        }
    }

    public fun fill(element: Int) {
        if (this is RandomAccess) {
            for (index in 0..<size) {
                set(index, element)
            }
        } else {
            val traverser = traverser(0)
            while (traverser.forward()) {
                traverser.set(element)
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
    override fun traverser(): IntTraverser = TraverserImpl()
    override fun traverser(position: Int): IntListTraverser = ListTraverserImpl(position)

    override fun subList(fromIndex: Int, toIndex: Int): IntList {
        return if (this is RandomAccess) {
            RandomAccessIntSubList(this, fromIndex, toIndex)
        } else {
            IntSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is IntList) return false
        if (size != other.size) return false

        val traverser = traverser()
        val otherTraverser = other.traverser()

        var hasNext = traverser.forward()
        var otherHasNext = traverser.forward()
        while (hasNext && otherHasNext) {
            if (!(traverser.value equalsBoxed otherTraverser.value)) {
                return false
            }
            hasNext = traverser.forward()
            otherHasNext = traverser.forward()
        }
        return !hasNext && !otherHasNext
    }

    override fun hashCode(): Int {
        var hashCode = 1
        foreach { element ->
            hashCode = 31 * hashCode + element.hashCode()
        }
        return hashCode
    }

    private inner class IteratorImpl: IntIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index != size
        override fun nextInt(): Int {
            if (index == size) throw NoSuchElementException()
            return get(index++)
        }
    }

    private inner class TraverserImpl : IntTraverser {
        private val last = size - 1
        private var index = -1
        override val value: Int get() = get(index)
        override fun forward(): Boolean {
            if (index == last) return false
            ++index
            return true
        }
    }

    private inner class ListTraverserImpl(position: Int) : IntListTraverser {
        init {
            require(position in 0..size)
        }

        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Int get() = get(index)

        override fun forward(): Boolean {
            if (position == size) return false
            index = position++
            return true
        }

        override fun backward(): Boolean {
            if (position == 0) return false
            index = --position
            return true
        }
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
            return list[index + offset]
        }
    }

    private class RandomAccessIntSubList(list: IntList, fromIndex: Int, toIndex: Int) : IntSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableIntList : AbstractIntList(), MutableIntList {

    override fun iterator(): MutableIntIterator = IteratorImpl()
    override fun traverser(): MutableIntTraverser = ListTraverserImpl(0)
    override fun traverser(position: Int): MutableIntListTraverser = ListTraverserImpl(position)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val traverser = traverser(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            check(traverser.backward())
            traverser.remove()
        }
    }

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
        override fun hasNext(): Boolean = index != size
        override fun nextInt(): Int {
            if (index == size) throw NoSuchElementException()
            lastIndex = index++
            return get(lastIndex)
        }
        override fun remove() {
            check(lastIndex != -1)
            removeAt(lastIndex)
            index--
            lastIndex = -1
        }
    }

    private inner class ListTraverserImpl(position: Int) : MutableIntListTraverser {
        init {
            require(position in 0..size)
        }

        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Int get() {
            check(index != -1)
            return get(index)
        }

        override fun forward(): Boolean {
            if (position == size) return false
            index = position++
            return true
        }

        override fun backward(): Boolean {
            if (position == 0) return false
            index = --position
            return true
        }

        override fun remove() {
            check(index != -1)
            removeAt(index)
            if (position > index) --position
            index = -1
        }

        override fun set(value: Int) {
            check(index != -1)
            set(index, value)
        }

        override fun insert(value: Int) {
            add(position, value)
            ++position
            ++index
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
            return list[rangeCheck(index) + offset]
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
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }
    }

    private class RandomAccessIntSubList(list: MutableIntList, fromIndex: Int, toIndex: Int) : IntSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyIntListTraverser : IntListTraverser {
    override val position: Int get() = 0
    override val value: Int get() = throw IllegalStateException()
    override fun forward(): Boolean = false
    override fun backward(): Boolean = false
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
    override fun traverser(): IntTraverser = EmptyIntListTraverser
    override fun traverser(position: Int): IntListTraverser {
        require(position == 0)
        return EmptyIntListTraverser
    }

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

    override fun iterator(): Iterator<Int> = listIterator()
    override fun listIterator(): ListIterator<Int> = ListIteratorImpl(0)
    override fun listIterator(index: Int): ListIterator<Int> = ListIteratorImpl(index)

    private inner class ListIteratorImpl(position: Int): ListIterator<Int> {
        private val size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Int {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Int {
            if (!hasPrevious()) throw NoSuchElementException()
            traverser.backward()
            return traverser.value
        }
        override fun nextIndex(): Int = traverser.position
        override fun previousIndex(): Int = traverser.position - 1
    }
}

private class MutableIntListWrapper(private val list: MutableIntList) : AbstractMutableList<Int>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Int) = list.contains(element)

    override fun indexOf(element: Int) = list.indexOf(element)
    override fun lastIndexOf(element: Int) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Int> = listIterator()
    override fun listIterator(): MutableListIterator<Int> = ListIteratorImpl(0)
    override fun listIterator(index: Int): MutableListIterator<Int> = ListIteratorImpl(index)

    override fun set(index: Int, element: Int) = list.replace(index, element)

    override fun add(element: Int) = list.add(element)
    override fun add(index: Int, element: Int) = list.add(index, element)

    override fun remove(element: Int): Boolean = list.remove(element)
    override fun removeAt(index: Int): Int = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Int> = list.subList(fromIndex, toIndex).asMutableList()

    private inner class ListIteratorImpl(position: Int): MutableListIterator<Int> {
        private val size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Int {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Int {
            if (!hasPrevious()) throw NoSuchElementException()
            traverser.backward()
            return traverser.value
        }
        override fun nextIndex(): Int = traverser.position
        override fun previousIndex(): Int = traverser.position - 1
        override fun remove() = traverser.remove()
        override fun set(element: Int) = traverser.set(element)
        override fun add(element: Int) = traverser.insert(element)
    }
}
