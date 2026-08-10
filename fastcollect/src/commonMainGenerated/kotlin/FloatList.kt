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

public fun emptyFloatList(): FloatList = EmptyFloatList

public fun floatListOf(): FloatList = EmptyFloatList
public fun floatListOf(element: Float): FloatList = SingletonFloatList(element)
public fun floatListOf(vararg elements: Float): FloatList = FloatArrayDeque(elements)

public fun mutableFloatListOf(): MutableFloatList = FloatArrayDeque()
public fun mutableFloatListOf(element: Float): MutableFloatList = FloatArrayDeque(1).apply { add(element) }
public fun mutableFloatListOf(vararg elements: Float): MutableFloatList = FloatArrayDeque(elements)

public fun FloatArray.asFloatList(): FloatList = FloatArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildFloatList(expectedSize: Int = 0, builderAction: MutableFloatList.() -> Unit): FloatList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = FloatArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun FloatList(size: Int, init: (index: Int) -> Float): FloatList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableFloatList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableFloatList(size: Int, init: (index: Int) -> Float): MutableFloatList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = FloatArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Floats.
 */
public interface FloatList : FloatCollection {
    public fun listIterator(): FloatListIterator
    public fun listIterator(index: Int): FloatListIterator

    override fun contains(element: Float): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Float

    override fun containsAll(elements: Collection<Float>): Boolean = super.containsAll(elements)

    public fun indexOf(element: Float): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextFloat() equalsBoxed element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Float): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previousFloat() equalsBoxed element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): FloatList
}

public val FloatList.indices: IntRange @JvmSynthetic inline get() = 0..<size

public val FloatList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun FloatList.first(): Float = if (isEmpty()) throw NoSuchElementException() else this[0]

@JvmSynthetic
public fun FloatList.last(): Float = if (isEmpty()) throw NoSuchElementException() else this[lastIndex]

public fun FloatList.rangeCheck(index: Int, size: Int = this.size): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun FloatList.rangeCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun FloatList.rangeCheck(fromIndex: Int, toIndex: Int, size: Int = this.size) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> FloatList.foldRight(initial: R, operation: (Float, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previousFloat(), accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun FloatList.reduceRight(operation: (Float, accumulated: Float) -> Float) : Float {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousFloat()
    while (it.hasPrevious()) {
        accumulated = operation(it.previousFloat(), accumulated)
    }
    return accumulated
}

public fun FloatList.asList(): List<Float> = FloatListWrapper(this)

/**
 * A mutable list of Floats.
 */
public interface MutableFloatList : FloatList, MutableFloatCollection {
    override fun listIterator(): MutableFloatListIterator
    override fun listIterator(index: Int): MutableFloatListIterator

    public operator fun set(index: Int, element: Float)

    public fun replace(index: Int, element: Float): Float {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Float): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Float)

    public fun addFirst(element: Float): Unit = add(0, element)
    public fun addLast(element: Float): Unit = add(size, element)
    public fun removeFirst(): Float = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Float = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Float): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        } else {
            removeAt(index)
            return true
        }
    }

    public fun removeAt(index: Int): Float

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: FloatCollection): Boolean {
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Float>): Boolean {
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: FloatCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Float>) {
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    override fun removeAll(elements: Collection<Float>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Float>): Boolean = super.retainAll(elements)

    public fun sort() {
        val sorted = toFloatArray().also { it.sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toFloatArray().also { it.sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Float) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableFloatList
}

public fun MutableFloatList.asMutableList(): MutableList<Float> = MutableFloatListWrapper(this)

public abstract class AbstractFloatList : AbstractFloatCollection(), FloatList {

    override fun iterator(): FloatIterator = IteratorImpl()
    override fun listIterator(): FloatListIterator = listIterator(0)
    override fun listIterator(index: Int): FloatListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        return if (this is RandomAccess) {
            RandomAccessFloatSubList(this, fromIndex, toIndex)
        } else {
            FloatSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is FloatList) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        while (it.hasNext() && otherIt.hasNext()) {
            if (!(it.nextFloat() equalsBoxed otherIt.nextFloat())) {
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

    private inner class IteratorImpl: FloatIterator() {

        private var index = 0

        override fun nextFloat(): Float {
            if (index == size) throw NoSuchElementException()
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean = index != size
    }

    private inner class ListIteratorImpl(private var index: Int = 0): FloatListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        override fun previousFloat(): Float {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            return value
        }

        override fun nextFloat(): Float {
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

    private open class FloatSubList(private val list: FloatList, fromIndex: Int, toIndex: Int) : AbstractFloatList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Float {
            rangeCheck(index)
            return list.get(index + offset)
        }
    }

    private class RandomAccessFloatSubList(list: FloatList, fromIndex: Int, toIndex: Int) : FloatSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableFloatList : AbstractFloatList(), MutableFloatList {

    override fun add(element: Float): Boolean = super<MutableFloatList>.add(element)
    override fun remove(element: Float): Boolean = super<MutableFloatList>.remove(element)
    override fun clear(): Unit = super<MutableFloatList>.clear()
    override fun addAll(elements: Collection<Float>): Boolean = super<MutableFloatList>.addAll(elements)
    override fun removeAll(elements: Collection<Float>): Boolean = super<MutableFloatList>.removeAll(elements)
    override fun retainAll(elements: Collection<Float>): Boolean = super<MutableFloatList>.retainAll(elements)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextFloat()
            it.remove()
        }
    }

    override fun iterator(): MutableFloatIterator = IteratorImpl()
    override fun listIterator(): MutableFloatListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableFloatListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableFloatList {
        return if (this is RandomAccess) {
            RandomAccessFloatSubList(this, fromIndex, toIndex)
        } else {
            FloatSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableFloatIterator() {
        private var index = 0
        private var lastIndex = -1

        override fun nextFloat(): Float {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableFloatListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        private var lastIndex = -1

        override fun previousFloat(): Float {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextFloat(): Float {
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

        override fun set(element: Float) {
            check(lastIndex >= 0)
            set(lastIndex, element)
        }

        override fun add(element: Float) {
            val i = index
            add(i, element)
            lastIndex = -1
            index = i + 1
        }
    }

    private open class FloatSubList(private val list: MutableFloatList, fromIndex: Int, toIndex: Int) : AbstractMutableFloatList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Float) {
            return list.set(rangeCheck(index) + offset, element)
        }

        override fun get(index: Int): Float {
            return list.get(rangeCheck(index) + offset)
        }

        override fun add(index: Int, element: Float) {
            list.add(rangeCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Float {
            val result = list.removeAt(rangeCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: FloatCollection) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Float>) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }
    }

    private class RandomAccessFloatSubList(list: MutableFloatList, fromIndex: Int, toIndex: Int) : FloatSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyFloatList : FloatList, RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Float): Boolean = false
    override fun containsAll(elements: Collection<Float>): Boolean = elements.isEmpty()
    override fun containsAll(elements: FloatCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Float = throw IndexOutOfBoundsException()
    override fun indexOf(element: Float): Int = -1
    override fun lastIndexOf(element: Float): Int = -1

    override fun iterator(): FloatIterator = emptyFloatIterator()
    override fun listIterator(): FloatListIterator = emptyFloatIterator()
    override fun listIterator(index: Int): FloatListIterator = if (index == 0) listIterator() else throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyFloatList
    }
}

private class SingletonFloatList(private val value: Float) : AbstractFloatList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Float): Boolean = value equalsBoxed element

    override fun get(index: Int): Float = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Float): Int = if (element equalsBoxed value) 0 else -1
    override fun lastIndexOf(element: Float): Int = if (element equalsBoxed value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyFloatList
    }
}

private class FloatArrayListWrapper(private val array: FloatArray): AbstractFloatList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Float = array[index]
}

private class FloatListWrapper(private val list: FloatList) : AbstractList<Float>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Float) = list.contains(element)

    override fun indexOf(element: Float) = list.indexOf(element)
    override fun lastIndexOf(element: Float) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
}

private class MutableFloatListWrapper(private val list: MutableFloatList) : AbstractMutableList<Float>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Float) = list.contains(element)

    override fun indexOf(element: Float) = list.indexOf(element)
    override fun lastIndexOf(element: Float) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)

    override fun set(index: Int, element: Float) = list.replace(index, element)

    override fun add(element: Float) = list.add(element)
    override fun add(index: Int, element: Float) = list.add(index, element)

    override fun remove(element: Float): Boolean = list.remove(element)
    override fun removeAt(index: Int): Float = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Float> = list.subList(fromIndex, toIndex).asMutableList()
}
