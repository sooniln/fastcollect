package io.github.sooniln.fastcollect.bytes

import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.random.Random

public fun emptyByteList(): ByteList = EmptyByteList

public fun byteListOf(): ByteList = EmptyByteList
public fun byteListOf(element: Byte): ByteList = SingletonByteList(element)
public fun byteListOf(vararg elements: Byte): ByteList = ByteArrayDeque(elements)

public fun mutableByteListOf(): MutableByteList = ByteArrayDeque()
public fun mutableByteListOf(element: Byte): MutableByteList = ByteArrayDeque(1).apply { add(element) }
public fun mutableByteListOf(vararg elements: Byte): MutableByteList = ByteArrayDeque(elements)

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
 * A list of Bytes.
 */
public interface ByteList : ByteCollection {
    public fun listIterator(): ByteListIterator
    public fun listIterator(index: Int): ByteListIterator

    override fun contains(element: Byte): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Byte

    override fun containsAll(elements: Collection<Byte>): Boolean = super.containsAll(elements)

    public fun indexOf(element: Byte): Int {
        val it = listIterator()
        while (it.hasNext()) {
            if (it.nextByte() equalsBoxed element) {
                return it.previousIndex()
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Byte): Int {
        val it = listIterator(size)
        while (it.hasPrevious()) {
            if (it.previousByte() equalsBoxed element) {
                return it.nextIndex()
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): ByteList
}

public val ByteList.lastIndex: Int inline get() = size - 1

public fun ByteList.rangeCheck(index: Int, size: Int = this.size): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun ByteList.rangeCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun ByteList.rangeCheck(fromIndex: Int, toIndex: Int, size: Int = this.size) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <R> ByteList.foldRight(initial: R, operation: (Byte, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    val it = listIterator(size)
    while (it.hasPrevious()) {
        accumulated = operation(it.previousByte(), accumulated)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun ByteList.reduceRight(operation: (Byte, accumulated: Byte) -> Byte) : Byte {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = listIterator(size)
    var accumulated = it.previousByte()
    while (it.hasPrevious()) {
        accumulated = operation(it.previousByte(), accumulated)
    }
    return accumulated
}

public fun ByteList.asList(): List<Byte> = ByteListWrapper(this)

/**
 * A mutable list of Bytes.
 */
public interface MutableByteList : ByteList, MutableByteCollection {
    override fun listIterator(): MutableByteListIterator
    override fun listIterator(index: Int): MutableByteListIterator

    public operator fun set(index: Int, element: Byte)

    public fun replace(index: Int, element: Byte): Byte {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Byte): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Byte)

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

    public fun removeAt(index: Int): Byte

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: ByteCollection): Boolean {
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Byte>): Boolean {
        if (elements is ByteCollection) return addAll(elements)
        for (element in elements) addLast(element)
        return !elements.isEmpty()
    }
    public fun addAll(index: Int, elements: ByteCollection) {
        var i = index
        for (element in elements) add(i++, element)
    }
    public fun addAll(index: Int, elements: Collection<Byte>) {
        if (elements is ByteCollection) {
            addAll(index, elements)
            return
        }
        var i = rangeCheckInclusive(index)
        for (element in elements) add(i++, element)
    }

    override fun removeAll(elements: Collection<Byte>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Byte>): Boolean = super.retainAll(elements)

    public fun sort() {
        val sorted = toByteArray().also { it.sort() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun sortDescending() {
        val sorted = toByteArray().also { it.sortDescending() }
        val it = listIterator()
        for (element in sorted) {
            it.next()
            it.set(element)
        }
    }

    public fun fill(element: Byte) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableByteList
}

public fun MutableByteList.asMutableList(): MutableList<Byte> = MutableByteListWrapper(this)

public abstract class AbstractByteList : AbstractByteCollection(), ByteList {

    override fun iterator(): ByteIterator = IteratorImpl()
    override fun listIterator(): ByteListIterator = listIterator(0)
    override fun listIterator(index: Int): ByteListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        return if (this is RandomAccess) {
            RandomAccessByteSubList(this, fromIndex, toIndex)
        } else {
            ByteSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ByteList) return false
        if (size != other.size) return false

        val it = listIterator()
        val otherIt = other.listIterator()
        while (it.hasNext() && otherIt.hasNext()) {
            if (!(it.nextByte() equalsBoxed otherIt.nextByte())) {
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

    private inner class IteratorImpl: ByteIterator() {

        private var index = 0

        override fun nextByte(): Byte {
            if (index == size) throw NoSuchElementException()
            val value = get(index)
            index++
            return value
        }

        override fun hasNext(): Boolean = index != size
    }

    private inner class ListIteratorImpl(private var index: Int = 0): ByteListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        override fun previousByte(): Byte {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            return value
        }

        override fun nextByte(): Byte {
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

    private open class ByteSubList(private val list: ByteList, fromIndex: Int, toIndex: Int) : AbstractByteList() {

        init {
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Byte {
            rangeCheck(index)
            return list.get(index + offset)
        }
    }

    private class RandomAccessByteSubList(list: ByteList, fromIndex: Int, toIndex: Int) : ByteSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableByteList : AbstractByteList(), MutableByteList {

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && toIndex == 0) return

        rangeCheck(fromIndex, toIndex)

        val it = listIterator(fromIndex)
        repeat(toIndex-fromIndex) { _ ->
            it.nextByte()
            it.remove()
        }
    }

    override fun iterator(): MutableByteIterator = IteratorImpl()
    override fun listIterator(): MutableByteListIterator = listIterator(0)
    override fun listIterator(index: Int): MutableByteListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): MutableByteList {
        return if (this is RandomAccess) {
            RandomAccessByteSubList(this, fromIndex, toIndex)
        } else {
            ByteSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableByteIterator() {
        private var index = 0
        private var lastIndex = -1

        override fun nextByte(): Byte {
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

    private inner class ListIteratorImpl(private var index: Int = 0): MutableByteListIterator() {

        init {
            if (index != 0) rangeCheckInclusive(index)
        }

        private var lastIndex = -1

        override fun previousByte(): Byte {
            if (index == 0) throw NoSuchElementException()
            val i = index - 1
            val value = get(i)
            index = i
            lastIndex = i
            return value
        }

        override fun nextByte(): Byte {
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

        override fun set(element: Byte) {
            check(lastIndex >= 0)
            set(lastIndex, element)
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
            rangeCheck(fromIndex, toIndex, list.size)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Byte) {
            return list.set(rangeCheck(index) + offset, element)
        }

        override fun get(index: Int): Byte {
            return list.get(rangeCheck(index) + offset)
        }

        override fun add(index: Int, element: Byte) {
            list.add(rangeCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Byte {
            val result = list.removeAt(rangeCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: ByteCollection) {
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Byte>) {
            if (elements is ByteCollection) {
                addAll(index, elements)
                return
            }
            list.addAll(offset + rangeCheckInclusive(index), elements)
            size += elements.size
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

    override fun get(index: Int): Byte = throw IndexOutOfBoundsException()
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
    override fun contains(element: Byte): Boolean = value equalsBoxed element

    override fun get(index: Int): Byte = if (index == 0) return value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Byte): Int = if (element equalsBoxed value) 0 else -1
    override fun lastIndexOf(element: Byte): Int = if (element equalsBoxed value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyByteList
    }
}

private class ByteArrayListWrapper(private val array: ByteArray): AbstractByteList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Byte = array[index]
}

private class ByteListWrapper(private val list: ByteList) : AbstractList<Byte>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Byte) = list.contains(element)

    override fun indexOf(element: Byte) = list.indexOf(element)
    override fun lastIndexOf(element: Byte) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)
}

private class MutableByteListWrapper(private val list: MutableByteList) : AbstractMutableList<Byte>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Byte) = list.contains(element)

    override fun indexOf(element: Byte) = list.indexOf(element)
    override fun lastIndexOf(element: Byte) = list.lastIndexOf(element)

    override fun iterator() = list.iterator()
    override fun listIterator() = list.listIterator()
    override fun listIterator(index: Int) = list.listIterator(index)

    override fun set(index: Int, element: Byte) = list.replace(index, element)

    override fun add(element: Byte) = list.add(element)
    override fun add(index: Int, element: Byte) = list.add(index, element)

    override fun remove(element: Byte): Boolean = list.remove(element)
    override fun removeAt(index: Int): Byte = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Byte> = list.subList(fromIndex, toIndex).asMutableList()
}
