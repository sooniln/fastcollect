package io.github.sooniln.fastcollect.doubles

import io.github.sooniln.fastcollect.assertBoxing
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

public fun emptyDoubleList(): DoubleList = EmptyDoubleList

public fun doubleListOf(): DoubleList = EmptyDoubleList
public fun doubleListOf(element: Double): DoubleList = SingletonDoubleList(element)
public fun doubleListOf(vararg elements: Double): DoubleList = DoubleArrayDeque.wrap(elements)

public fun mutableDoubleListOf(): MutableDoubleList = DoubleArrayDeque()
public fun mutableDoubleListOf(element: Double): MutableDoubleList = DoubleArrayDeque(1).apply { add(element) }
public fun mutableDoubleListOf(vararg elements: Double): MutableDoubleList = DoubleArrayDeque.wrap(elements)

public fun DoubleArray.asDoubleList(): DoubleList = DoubleArrayListWrapper(this)

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildDoubleList(expectedSize: Int = 0, builderAction: MutableDoubleList.() -> Unit): DoubleList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val list = DoubleArrayDeque(expectedSize)
    list.builderAction()
    return list
}

/**
 * A list of Doubles which inherits from [List].
 */
public interface DoubleList : List<Double>, DoubleCollection {
    override fun listIterator(): DoubleListIterator
    override fun listIterator(index: Int): DoubleListIterator

    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Double): Boolean {
        return indexOf(element) != -1
    }

    override fun containsAll(elements: Collection<Double>): Boolean = super.containsAll(elements)

    override fun indexOf(element: Double): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextDouble() == element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    override fun lastIndexOf(element: Double): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previous() == element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList
}

/**
 * A mutable list of Doubles which inherits from [MutableList].
 */
public interface MutableDoubleList : DoubleList, MutableDoubleCollection, MutableList<Double> {
    override fun listIterator(): MutableDoubleListIterator
    override fun listIterator(index: Int): MutableDoubleListIterator

    @Deprecated(
        message = "Use setAt(index, element) instead.",
        replaceWith = ReplaceWith("setAt(index, element)"),
        level = DeprecationLevel.WARNING)
    override fun set(index: Int, element: Double): Double {
        assertBoxing()
        val value = get(index)
        setAt(index, element)
        return value
    }

    public fun setAt(index: Int, element: Double)

    override fun add(element: Double): Boolean {
        add(size, element)
        return true
    }

    override fun add(index: Int, element: Double)

    public fun addFirst(element: Double): Unit = add(0, element)
    public fun addLast(element: Double): Unit = add(size, element)
    public fun removeFirst(): Double = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Double = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Double): Boolean {
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

    override fun addAll(elements: DoubleCollection): Boolean {
        return addAll(size, elements)
    }

    override fun addAll(elements: Collection<Double>): Boolean {
        return addAll(size, elements)
    }

    override fun removeAll(elements: Collection<Double>): Boolean {
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<Double>): Boolean {
        return super.retainAll(elements)
    }

    public fun addAll(index: Int, elements: DoubleCollection): Boolean
    override fun addAll(index: Int, elements: Collection<Double>): Boolean

    override fun subList(fromIndex: Int, toIndex: Int): MutableDoubleList
}

public abstract class AbstractDoubleList : AbstractDoubleCollection(), DoubleList {

    override fun iterator(): DoubleIterator {
        return IteratorImpl()
    }

    override fun listIterator(): DoubleListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): DoubleListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        return if (this is RandomAccess) {
            RandomAccessDoubleSubList(this, fromIndex, toIndex)
        } else {
            DoubleSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        if (otherIt is DoubleIterator) {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextDouble() != otherIt.nextDouble()) {
                    return false
                }
            }
        } else {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextDouble() != otherIt.next()) {
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

    private inner class IteratorImpl(private var index: Int = 0): DoubleIterator() {

        override fun nextDouble(): Double {
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }
    }

    private inner class ListIteratorImpl(private var index: Int = 0): DoubleListIterator() {

        override fun previousDouble(): Double {
            val i = index - 1
            val value = get(i)
            index = i
            return value
        }

        override fun nextDouble(): Double {
            val value = get(index)
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

    private open class DoubleSubList(private val list: DoubleList, fromIndex: Int, toIndex: Int) : AbstractDoubleList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Double {
            return list[index + offset]
        }
    }

    private class RandomAccessDoubleSubList(list: DoubleList, fromIndex: Int, toIndex: Int) : DoubleSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableDoubleList : AbstractDoubleList(), MutableDoubleList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        require(fromIndex <= toIndex)
        rangeCheck(fromIndex)
        rangeCheckForAdd(toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextDouble()
            it.remove()
        }
    }

    override fun addAll(index: Int, elements: DoubleCollection): Boolean {
        var index = rangeCheckForAdd(index)
        var modified = false
        for (element in elements) {
            add(index++, element)
            modified = true
        }
        return modified
    }

    override fun addAll(index: Int, elements: Collection<Double>): Boolean {
        if (elements is DoubleCollection) {
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

    override fun iterator(): MutableDoubleIterator {
        return IteratorImpl()
    }

    override fun listIterator(): MutableDoubleListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): MutableDoubleListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableDoubleList {
        return if (this is RandomAccess) {
            RandomAccessDoubleSubList(this, fromIndex, toIndex)
        } else {
            DoubleSubList(this, fromIndex, toIndex)
        }
    }

    protected fun rangeCheckForAdd(index: Int): Int {
        if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
        return index
    }

    private inner class IteratorImpl(private var index: Int = 0): MutableDoubleIterator() {
        private var lastIndex = -1

        override fun nextDouble(): Double {
            val i = index
            val value = get(i)
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableDoubleListIterator() {

        private var lastIndex = -1

        override fun previousDouble(): Double {
            val i = index - 1
            val value = get(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextDouble(): Double {
            val i = index
            val value = get(i)
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

        override fun set(element: Double) {
            check(lastIndex >= 0)
            setAt(lastIndex, element)
        }

        override fun add(element: Double) {
            val i = index
            add(i, element)
            lastIndex = -1
            index = i + 1
        }
    }

    private open class DoubleSubList(private val list: MutableDoubleList, fromIndex: Int, toIndex: Int) : AbstractMutableDoubleList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun setAt(index: Int, element: Double) {
            return list.setAt(index + offset, element)
        }

        override fun get(index: Int): Double {
            return list[index + offset]
        }

        override fun add(index: Int, element: Double) {
            list.add(index + offset, element)
            size++
        }

        override fun removeAt(index: Int): Double {
            val result = list.removeAt(index + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            list.removeRange(fromIndex + offset, toIndex + offset)
        }

        override fun addAll(index: Int, elements: DoubleCollection): Boolean {
            if (elements.isEmpty()) return false

            list.addAll(offset + index, elements)
            size += elements.size
            return true
        }
    }

    private class RandomAccessDoubleSubList(list: MutableDoubleList, fromIndex: Int, toIndex: Int) : DoubleSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyDoubleList : DoubleList, RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Double): Boolean = false
    override fun containsAll(elements: Collection<Double>): Boolean = elements.isEmpty()
    override fun containsAll(elements: DoubleCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Double = throw IndexOutOfBoundsException()
    override fun indexOf(element: Double): Int = -1
    override fun lastIndexOf(element: Double): Int = -1

    override fun iterator(): DoubleIterator = emptyDoubleIterator()
    override fun listIterator(): DoubleListIterator = emptyDoubleIterator()
    override fun listIterator(index: Int): DoubleListIterator = if (index == 0) listIterator() else throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyDoubleList
    }
}

private class SingletonDoubleList(private val value: Double) : AbstractDoubleList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Double): Boolean = value == element

    override fun get(index: Int): Double = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Double): Int = if (element == value) 0 else -1
    override fun lastIndexOf(element: Double): Int = if (element == value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyDoubleList
    }
}

private class DoubleArrayListWrapper(private val array: DoubleArray): AbstractDoubleList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Double = array[index]
}
