package io.github.sooniln.fastcollect.floats

import io.github.sooniln.fastcollect.assertBoxing
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.random.Random

public fun emptyFloatList(): FloatList = EmptyFloatList

public fun floatListOf(): FloatList = EmptyFloatList
public fun floatListOf(element: Float): FloatList = SingletonFloatList(element)
public fun floatListOf(vararg elements: Float): FloatList = FloatArrayDeque.wrap(elements)

public fun mutableFloatListOf(): MutableFloatList = FloatArrayDeque()
public fun mutableFloatListOf(element: Float): MutableFloatList = FloatArrayDeque(1).apply { add(element) }
public fun mutableFloatListOf(vararg elements: Float): MutableFloatList = FloatArrayDeque.wrap(elements)

public fun FloatArray.asFloatList(): FloatList = FloatArrayListWrapper(this)

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildFloatList(expectedSize: Int = 0, builderAction: MutableFloatList.() -> Unit): FloatList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = FloatArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun FloatList(size: Int, init: (index: Int) -> Float): FloatList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableFloatList(size, init)
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableFloatList(size: Int, init: (index: Int) -> Float): MutableFloatList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = FloatArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Floats which inherits from [List].
 */
public interface FloatList : List<Float>, FloatCollection {
    override fun listIterator(): FloatListIterator
    override fun listIterator(index: Int): FloatListIterator

    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Float): Boolean {
        return indexOf(element) != -1
    }

    @Deprecated(
        message = "Use getAt(index) instead.",
        replaceWith = ReplaceWith("getAt(index)"),
        level = DeprecationLevel.WARNING)
    override fun get(index: Int): Float = getAt(index)

    public fun getAt(index: Int): Float

    override fun containsAll(elements: Collection<Float>): Boolean = super.containsAll(elements)

    override fun indexOf(element: Float): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextFloat() == element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    override fun lastIndexOf(element: Float): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previous() == element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    override fun subList(fromIndex: Int, toIndex: Int): FloatList
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> FloatList.foldRight(initial: R, operation: (Float, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previous(), accumulated)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun FloatList.reduceRight(operation: (accumulated: Float, Float) -> Float) : Float {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousFloat()
    while (it.hasPrevious()) {
        accumulated = operation(accumulated, it.previousFloat())
    }
    return accumulated
}

/**
 * A mutable list of Floats which inherits from [MutableList].
 */
public interface MutableFloatList : FloatList, MutableFloatCollection, MutableList<Float> {
    override fun listIterator(): MutableFloatListIterator
    override fun listIterator(index: Int): MutableFloatListIterator

    @Deprecated(
        message = "Use replace(index, element) instead.",
        replaceWith = ReplaceWith("replace(index, element)"),
        level = DeprecationLevel.WARNING)
    override fun set(index: Int, element: Float): Float {
        assertBoxing()
        val value = getAt(index)
        setAt(index, element)
        return value
    }

    public fun replace(index: Int, element: Float): Float {
        val value = getAt(index)
        setAt(index, element)
        return value
    }

    public fun setAt(index: Int, element: Float)

    override fun add(element: Float): Boolean {
        addLast(element)
        return true
    }

    override fun add(index: Int, element: Float)

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

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear() {
        removeRange(0, size)
    }

    override fun addAll(elements: FloatCollection): Boolean {
        return addAll(size, elements)
    }

    override fun addAll(elements: Collection<Float>): Boolean {
        return addAll(size, elements)
    }

    override fun removeAll(elements: Collection<Float>): Boolean {
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<Float>): Boolean {
        return super.retainAll(elements)
    }

    public fun addAll(index: Int, elements: FloatCollection): Boolean
    override fun addAll(index: Int, elements: Collection<Float>): Boolean

    public fun sort() {
        val sorted = toFloatArray().also { sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toFloatArray().also { sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Float) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableFloatList
}

public abstract class AbstractFloatList : AbstractFloatCollection(), FloatList {

    override fun iterator(): FloatIterator {
        return IteratorImpl()
    }

    override fun listIterator(): FloatListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): FloatListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        return if (this is RandomAccess) {
            RandomAccessFloatSubList(this, fromIndex, toIndex)
        } else {
            FloatSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        if (otherIt is FloatIterator) {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextFloat() != otherIt.nextFloat()) {
                    return false
                }
            }
        } else {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextFloat() != otherIt.next()) {
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

    private inner class IteratorImpl(private var index: Int = 0): FloatIterator() {

        override fun nextFloat(): Float {
            val value = getAt(index)
            index++
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }
    }

    private inner class ListIteratorImpl(private var index: Int = 0): FloatListIterator() {

        override fun previousFloat(): Float {
            val i = index - 1
            val value = getAt(i)
            index = i
            return value
        }

        override fun nextFloat(): Float {
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

    private open class FloatSubList(private val list: FloatList, fromIndex: Int, toIndex: Int) : AbstractFloatList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun getAt(index: Int): Float {
            return list.getAt(index + offset)
        }
    }

    private class RandomAccessFloatSubList(list: FloatList, fromIndex: Int, toIndex: Int) : FloatSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableFloatList : AbstractFloatList(), MutableFloatList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        require(fromIndex <= toIndex)
        rangeCheck(fromIndex)
        rangeCheckForAdd(toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextFloat()
            it.remove()
        }
    }

    override fun addAll(index: Int, elements: FloatCollection): Boolean {
        var index = rangeCheckForAdd(index)
        var modified = false
        for (element in elements) {
            add(index++, element)
            modified = true
        }
        return modified
    }

    override fun addAll(index: Int, elements: Collection<Float>): Boolean {
        if (elements is FloatCollection) {
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

    override fun iterator(): MutableFloatIterator {
        return IteratorImpl()
    }

    override fun listIterator(): MutableFloatListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): MutableFloatListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableFloatList {
        return if (this is RandomAccess) {
            RandomAccessFloatSubList(this, fromIndex, toIndex)
        } else {
            FloatSubList(this, fromIndex, toIndex)
        }
    }

    protected fun rangeCheckForAdd(index: Int): Int {
        if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
        return index
    }

    private inner class IteratorImpl(private var index: Int = 0): MutableFloatIterator() {
        private var lastIndex = -1

        override fun nextFloat(): Float {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableFloatListIterator() {

        private var lastIndex = -1

        override fun previousFloat(): Float {
            val i = index - 1
            val value = getAt(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextFloat(): Float {
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

        override fun set(element: Float) {
            check(lastIndex >= 0)
            setAt(lastIndex, element)
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
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun setAt(index: Int, element: Float) {
            return list.setAt(index + offset, element)
        }

        override fun getAt(index: Int): Float {
            return list.getAt(index + offset)
        }

        override fun add(index: Int, element: Float) {
            list.add(index + offset, element)
            size++
        }

        override fun removeAt(index: Int): Float {
            val result = list.removeAt(index + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            list.removeRange(fromIndex + offset, toIndex + offset)
        }

        override fun addAll(index: Int, elements: FloatCollection): Boolean {
            if (elements.isEmpty()) return false

            list.addAll(offset + index, elements)
            size += elements.size
            return true
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

    override fun getAt(index: Int): Float = throw IndexOutOfBoundsException()
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
    override fun contains(element: Float): Boolean = value == element

    override fun getAt(index: Int): Float = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Float): Int = if (element == value) 0 else -1
    override fun lastIndexOf(element: Float): Int = if (element == value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyFloatList
    }
}

private class FloatArrayListWrapper(private val array: FloatArray): AbstractFloatList(), RandomAccess {
    override val size: Int get() = array.size
    override fun getAt(index: Int): Float = array[index]
}
