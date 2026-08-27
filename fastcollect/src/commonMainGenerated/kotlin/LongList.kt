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

public fun emptyLongList(): LongList = EmptyLongList

public fun longListOf(): LongList = EmptyLongList
public fun longListOf(element: Long): LongList = SingletonLongList(element)
public fun longListOf(vararg elements: Long): LongList = LongArrayDeque(elements)

public fun mutableLongListOf(): MutableLongList = LongArrayDeque()
public fun mutableLongListOf(element: Long): MutableLongList = LongArrayDeque(1).apply { add(element) }
public fun mutableLongListOf(vararg elements: Long): MutableLongList = LongArrayDeque(elements)

public fun LongArray.asLongList(): LongList = LongArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildLongList(expectedSize: Int = 0, builderAction: MutableLongList.() -> Unit): LongList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = LongArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongList(size: Int, init: (index: Int) -> Long): LongList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableLongList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableLongList(size: Int, init: (index: Int) -> Long): MutableLongList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = LongArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

public interface LongListTraversable: LongTraversable {
    public fun traverser(position: Int): LongListTraverser
}

public interface MutableLongListTraversable: MutableLongTraversable {
    public fun traverser(position: Int): MutableLongListTraverser
}

public interface LongListTraverser : LongTraverser {
    public val position: Int
    public fun backward(): Boolean
}

public interface MutableLongListTraverser : LongListTraverser, MutableLongTraverser {
    public fun set(value: Long)
    public fun insert(value: Long)
}

@OptIn(ExperimentalContracts::class)
public inline fun LongList.foreachReverse(action: (Long) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    while (traverser.backward()) {
        action(traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun LongListTraversable.foreachIndexed(action: (Int, Long) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(0)
    var index = 0
    while (traverser.forward()) {
        action(index++, traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun LongList.foreachReverseIndexed(action: (Int, Long) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    var index = size
    while (traverser.backward()) {
        action(--index, traverser.value)
    }
}

/**
 * A list of Longs.
 */
public interface LongList : LongCollection, LongListTraversable {

    override fun contains(element: Long): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Long

    public fun indexOf(element: Long): Int {
        foreachIndexed { index, value ->
            if (value equalsBoxed element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Long): Int {
        foreachReverseIndexed { index, value ->
            if (value equalsBoxed element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): LongList
}

public val LongList.indices: IntRange @JvmSynthetic inline get() = 0..<size

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
@OptIn(ExperimentalContracts::class)
public inline fun <R> LongList.foldRight(initial: R, operation: (Long, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreachReverse { value ->
        accumulated = operation(value, accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongList.reduceRight(operation: (Long, accumulated: Long) -> Long) : Long {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    if (!traverser.backward()) throw NoSuchElementException()
    var accumulated = traverser.value
    while (traverser.backward()) {
        accumulated = operation(traverser.value, accumulated)
    }
    return accumulated
}

public fun LongList.asList(): List<Long> = LongListWrapper(this)

/**
 * A mutable list of Longs.
 */
public interface MutableLongList : LongList, MutableLongCollection, MutableLongTraversable {

    override fun traverser(position: Int): MutableLongListTraverser

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
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: LongCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Long>) {
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    public fun sort() {
        val sorted = toLongArray().also { it.sort() }
        val traverser = traverser(0)
        for (element in sorted) {
            check(traverser.forward())
            traverser.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toLongArray().also { it.sortDescending() }
        val traverser = traverser(0)
        for (element in sorted) {
            check(traverser.forward())
            traverser.set(element)
        }
    }

    public fun fill(element: Long) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableLongList
}

public fun MutableLongList.asMutableList(): MutableList<Long> = MutableLongListWrapper(this)

public abstract class AbstractLongList : AbstractLongCollection(), LongList {

    override fun iterator(): LongIterator = IteratorImpl()
    override fun traverser(): LongTraverser = TraverserImpl()
    override fun traverser(position: Int): LongListTraverser = ListTraverserImpl(position)

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

    private inner class IteratorImpl: LongIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index != size
        override fun nextLong(): Long {
            if (index == size) throw NoSuchElementException()
            return get(index++)
        }
    }

    private inner class TraverserImpl : LongTraverser {
        private val last = size - 1
        private var index = -1
        override val value: Long get() = get(index)
        override fun forward(): Boolean {
            if (index == last) return false
            ++index
            return true
        }
    }

    private inner class ListTraverserImpl(position: Int) : LongListTraverser {
        init {
            require(position in 0..size)
        }

        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Long get() = get(index)

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

    private open class LongSubList(private val list: LongList, fromIndex: Int, toIndex: Int) : AbstractLongList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Long {
            rangeCheck(index)
            return list[index + offset]
        }
    }

    private class RandomAccessLongSubList(list: LongList, fromIndex: Int, toIndex: Int) : LongSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableLongList : AbstractLongList(), MutableLongList {

    override fun iterator(): MutableLongIterator = IteratorImpl()
    override fun traverser(): MutableLongTraverser = ListTraverserImpl(0)
    override fun traverser(position: Int): MutableLongListTraverser = ListTraverserImpl(position)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val traverser = traverser(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            check(traverser.backward())
            traverser.remove()
        }
    }

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
        override fun hasNext(): Boolean = index != size
        override fun nextLong(): Long {
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

    private inner class ListTraverserImpl(position: Int) : MutableLongListTraverser {
        init {
            require(position in 0..size)
        }

        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Long get() {
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

        override fun set(value: Long) {
            check(index != -1)
            set(index, value)
        }

        override fun insert(value: Long) {
            add(position, value)
            ++position
            ++index
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
            return list[rangeCheck(index) + offset]
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
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }
    }

    private class RandomAccessLongSubList(list: MutableLongList, fromIndex: Int, toIndex: Int) : LongSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyLongListTraverser : LongListTraverser {
    override val position: Int get() = 0
    override val value: Long get() = throw IllegalStateException()
    override fun forward(): Boolean = false
    override fun backward(): Boolean = false
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
    override fun traverser(): LongTraverser = EmptyLongListTraverser
    override fun traverser(position: Int): LongListTraverser {
        require(position == 0)
        return EmptyLongListTraverser
    }

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

    override fun iterator(): Iterator<Long> = listIterator()
    override fun listIterator(): ListIterator<Long> = ListIteratorImpl(0)
    override fun listIterator(index: Int): ListIterator<Long> = ListIteratorImpl(index)

    private inner class ListIteratorImpl(position: Int): ListIterator<Long> {
        private val size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Long {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Long {
            if (!hasPrevious()) throw NoSuchElementException()
            traverser.backward()
            return traverser.value
        }
        override fun nextIndex(): Int = traverser.position
        override fun previousIndex(): Int = traverser.position - 1
    }
}

private class MutableLongListWrapper(private val list: MutableLongList) : AbstractMutableList<Long>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Long) = list.contains(element)

    override fun indexOf(element: Long) = list.indexOf(element)
    override fun lastIndexOf(element: Long) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Long> = listIterator()
    override fun listIterator(): MutableListIterator<Long> = ListIteratorImpl(0)
    override fun listIterator(index: Int): MutableListIterator<Long> = ListIteratorImpl(index)

    override fun set(index: Int, element: Long) = list.replace(index, element)

    override fun add(element: Long) = list.add(element)
    override fun add(index: Int, element: Long) = list.add(index, element)

    override fun remove(element: Long): Boolean = list.remove(element)
    override fun removeAt(index: Int): Long = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Long> = list.subList(fromIndex, toIndex).asMutableList()

    private inner class ListIteratorImpl(position: Int): MutableListIterator<Long> {
        private val size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Long {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Long {
            if (!hasPrevious()) throw NoSuchElementException()
            traverser.backward()
            return traverser.value
        }
        override fun nextIndex(): Int = traverser.position
        override fun previousIndex(): Int = traverser.position - 1
        override fun remove() = traverser.remove()
        override fun set(element: Long) = traverser.set(element)
        override fun add(element: Long) = traverser.insert(element)
    }
}
