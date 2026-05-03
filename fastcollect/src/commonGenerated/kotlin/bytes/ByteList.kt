package io.github.sooniln.fastcollect.bytes

import io.github.sooniln.fastcollect.assertBoxing
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.random.Random

public fun emptyByteList(): ByteList = EmptyByteList

public fun byteListOf(): ByteList = EmptyByteList
public fun byteListOf(element: Byte): ByteList = SingletonByteList(element)
public fun byteListOf(vararg elements: Byte): ByteList = ByteArrayDeque.wrap(elements)

public fun mutableByteListOf(): MutableByteList = ByteArrayDeque()
public fun mutableByteListOf(element: Byte): MutableByteList = ByteArrayDeque(1).apply { add(element) }
public fun mutableByteListOf(vararg elements: Byte): MutableByteList = ByteArrayDeque.wrap(elements)

public fun ByteArray.asByteList(): ByteList = ByteArrayListWrapper(this)

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildByteList(expectedSize: Int = 0, builderAction: MutableByteList.() -> Unit): ByteList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = ByteArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun ByteList(size: Int, init: (index: Int) -> Byte): ByteList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableByteList(size, init)
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun MutableByteList(size: Int, init: (index: Int) -> Byte): MutableByteList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = ByteArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A list of Bytes which inherits from [List].
 */
public interface ByteList : List<Byte>, ByteCollection {
    override fun listIterator(): ByteListIterator
    override fun listIterator(index: Int): ByteListIterator

    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Byte): Boolean {
        return indexOf(element) != -1
    }

    @Deprecated(
        message = "Use getAt(index) instead.",
        replaceWith = ReplaceWith("getAt(index)"),
        level = DeprecationLevel.WARNING)
    override fun get(index: Int): Byte = getAt(index)

    public fun getAt(index: Int): Byte

    override fun containsAll(elements: Collection<Byte>): Boolean = super.containsAll(elements)

    override fun indexOf(element: Byte): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextByte() == element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    override fun lastIndexOf(element: Byte): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previous() == element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    override fun subList(fromIndex: Int, toIndex: Int): ByteList
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> ByteList.foldRight(initial: R, operation: (Byte, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previous(), accumulated)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun ByteList.reduceRight(operation: (accumulated: Byte, Byte) -> Byte) : Byte {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousByte()
    while (it.hasPrevious()) {
        accumulated = operation(accumulated, it.previousByte())
    }
    return accumulated
}

/**
 * A mutable list of Bytes which inherits from [MutableList].
 */
public interface MutableByteList : ByteList, MutableByteCollection, MutableList<Byte> {
    override fun listIterator(): MutableByteListIterator
    override fun listIterator(index: Int): MutableByteListIterator

    @Deprecated(
        message = "Use setAt(index, element) instead.",
        replaceWith = ReplaceWith("setAt(index, element)"),
        level = DeprecationLevel.WARNING)
    override fun set(index: Int, element: Byte): Byte {
        assertBoxing()
        val value = getAt(index)
        setAt(index, element)
        return value
    }

    public fun setAt(index: Int, element: Byte)

    override fun add(element: Byte): Boolean {
        addLast(element)
        return true
    }

    override fun add(index: Int, element: Byte)

    public fun addFirst(element: Byte): Unit = add(0, element)
    public fun addLast(element: Byte): Unit = add(size, element)
    public fun removeFirst(): Byte = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Byte = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Byte): Boolean {
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

    override fun addAll(elements: ByteCollection): Boolean {
        return addAll(size, elements)
    }

    override fun addAll(elements: Collection<Byte>): Boolean {
        return addAll(size, elements)
    }

    override fun removeAll(elements: Collection<Byte>): Boolean {
        return super.removeAll(elements)
    }

    override fun retainAll(elements: Collection<Byte>): Boolean {
        return super.retainAll(elements)
    }

    public fun addAll(index: Int, elements: ByteCollection): Boolean
    override fun addAll(index: Int, elements: Collection<Byte>): Boolean

    public fun sort() {
        val sorted = toByteArray().also { sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toByteArray().also { sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Byte) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableByteList
}

public abstract class AbstractByteList : AbstractByteCollection(), ByteList {

    override fun iterator(): ByteIterator {
        return IteratorImpl()
    }

    override fun listIterator(): ByteListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): ByteListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        return if (this is RandomAccess) {
            RandomAccessByteSubList(this, fromIndex, toIndex)
        } else {
            ByteSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is List<*>) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        if (otherIt is ByteIterator) {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextByte() != otherIt.nextByte()) {
                    return false
                }
            }
        } else {
            while (it.hasNext() && otherIt.hasNext()) {
                if (it.nextByte() != otherIt.next()) {
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

    private inner class IteratorImpl(private var index: Int = 0): ByteIterator() {

        override fun nextByte(): Byte {
            val value = getAt(index)
            index++
            return value
        }

        override fun hasNext(): Boolean {
            return index != size
        }
    }

    private inner class ListIteratorImpl(private var index: Int = 0): ByteListIterator() {

        override fun previousByte(): Byte {
            val i = index - 1
            val value = getAt(i)
            index = i
            return value
        }

        override fun nextByte(): Byte {
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

    private open class ByteSubList(private val list: ByteList, fromIndex: Int, toIndex: Int) : AbstractByteList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun getAt(index: Int): Byte {
            return list.getAt(index + offset)
        }
    }

    private class RandomAccessByteSubList(list: ByteList, fromIndex: Int, toIndex: Int) : ByteSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableByteList : AbstractByteList(), MutableByteList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        require(fromIndex <= toIndex)
        rangeCheck(fromIndex)
        rangeCheckForAdd(toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextByte()
            it.remove()
        }
    }

    override fun addAll(index: Int, elements: ByteCollection): Boolean {
        var index = rangeCheckForAdd(index)
        var modified = false
        for (element in elements) {
            add(index++, element)
            modified = true
        }
        return modified
    }

    override fun addAll(index: Int, elements: Collection<Byte>): Boolean {
        if (elements is ByteCollection) {
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

    override fun iterator(): MutableByteIterator {
        return IteratorImpl()
    }

    override fun listIterator(): MutableByteListIterator {
        return listIterator(0)
    }

    override fun listIterator(index: Int): MutableByteListIterator {
        return ListIteratorImpl(index)
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableByteList {
        return if (this is RandomAccess) {
            RandomAccessByteSubList(this, fromIndex, toIndex)
        } else {
            ByteSubList(this, fromIndex, toIndex)
        }
    }

    protected fun rangeCheckForAdd(index: Int): Int {
        if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
        return index
    }

    private inner class IteratorImpl(private var index: Int = 0): MutableByteIterator() {
        private var lastIndex = -1

        override fun nextByte(): Byte {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableByteListIterator() {

        private var lastIndex = -1

        override fun previousByte(): Byte {
            val i = index - 1
            val value = getAt(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextByte(): Byte {
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

        override fun set(element: Byte) {
            check(lastIndex >= 0)
            setAt(lastIndex, element)
        }

        override fun add(element: Byte) {
            val i = index
            add(i, element)
            lastIndex = -1
            index = i + 1
        }
    }

    private open class ByteSubList(private val list: MutableByteList, fromIndex: Int, toIndex: Int) : AbstractMutableByteList() {

        init {
            rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun setAt(index: Int, element: Byte) {
            return list.setAt(index + offset, element)
        }

        override fun getAt(index: Int): Byte {
            return list.getAt(index + offset)
        }

        override fun add(index: Int, element: Byte) {
            list.add(index + offset, element)
            size++
        }

        override fun removeAt(index: Int): Byte {
            val result = list.removeAt(index + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            list.removeRange(fromIndex + offset, toIndex + offset)
        }

        override fun addAll(index: Int, elements: ByteCollection): Boolean {
            if (elements.isEmpty()) return false

            list.addAll(offset + index, elements)
            size += elements.size
            return true
        }
    }

    private class RandomAccessByteSubList(list: MutableByteList, fromIndex: Int, toIndex: Int) : ByteSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyByteList : ByteList, RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Byte): Boolean = false
    override fun containsAll(elements: Collection<Byte>): Boolean = elements.isEmpty()
    override fun containsAll(elements: ByteCollection): Boolean = elements.isEmpty()

    override fun getAt(index: Int): Byte = throw IndexOutOfBoundsException()
    override fun indexOf(element: Byte): Int = -1
    override fun lastIndexOf(element: Byte): Int = -1

    override fun iterator(): ByteIterator = emptyByteIterator()
    override fun listIterator(): ByteListIterator = emptyByteIterator()
    override fun listIterator(index: Int): ByteListIterator = if (index == 0) listIterator() else throw IndexOutOfBoundsException()

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyByteList
    }
}

private class SingletonByteList(private val value: Byte) : AbstractByteList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Byte): Boolean = value == element

    override fun getAt(index: Int): Byte = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Byte): Int = if (element == value) 0 else -1
    override fun lastIndexOf(element: Byte): Int = if (element == value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyByteList
    }
}

private class ByteArrayListWrapper(private val array: ByteArray): AbstractByteList(), RandomAccess {
    override val size: Int get() = array.size
    override fun getAt(index: Int): Byte = array[index]
}
