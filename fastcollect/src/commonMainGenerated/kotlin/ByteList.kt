/**
 * Methods for dealing with ByteLists.
 */
@file:JvmName("ByteLists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic
import kotlin.random.Random

public fun emptyByteList(): ByteList = EmptyByteList

public fun byteListOf(): ByteList = EmptyByteList
public fun byteListOf(element: Byte): ByteList = SingletonByteList(element)
public fun byteListOf(vararg elements: Byte): ByteList = ByteArrayDeque.wrap(elements)

public fun mutableByteListOf(): MutableByteList = ByteArrayDeque()
public fun mutableByteListOf(element: Byte): MutableByteList = ByteArrayDeque(1).apply { add(element) }
public fun mutableByteListOf(vararg elements: Byte): MutableByteList = ByteArrayDeque.wrap(elements)

public fun ByteArray.asByteList(): ByteList = ByteArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildByteList(expectedSize: Int = 0, builderAction: MutableByteList.() -> Unit): ByteList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = ByteArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteList(size: Int, init: (index: Int) -> Byte): ByteList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableByteList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableByteList(size: Int, init: (index: Int) -> Byte): MutableByteList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = ByteArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

/**
 * A random-access list of Bytes.
 */
public interface ByteList : ByteCollection, RandomAccess {

    public fun listIterator(): ByteListIterator = listIterator(0)
    public fun listIterator(index: Int): ByteListIterator

    override fun contains(element: Byte): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Byte

    public fun first(): Byte = if (isEmpty()) throw NoSuchElementException() else get(0)

    public fun last(): Byte = if (isEmpty()) throw NoSuchElementException() else get(lastIndex)

    public fun indexOf(element: Byte): Int {
        for (index in 0..<size) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Byte): Int {
        for (index in (size - 1) downTo 0) {
            val e = get(index)
            if (e equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): ByteList

    override fun copyInto(destination: ByteArray, destinationOffset: Int): ByteArray {
        return copyInto(destination, destinationOffset, 0, size)
    }

    /**
     * Copies the elements of this list in the range [[fromIndex], [toIndex]) into [destination], starting at
     * [destinationOffset], and returns [destination]. Throw [IndexOutOfBoundsException] if [destination] does not have
     * enough room for all elements in the given range.
     */
    public fun copyInto(destination: ByteArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): ByteArray {
        rangeCheck(fromIndex, toIndex)
        val destinationToIndex = destinationOffset + toIndex - fromIndex
        destination.rangeCheck(destinationOffset, destinationToIndex)

        var destinationIndex = destinationOffset
        var index = fromIndex
        while (destinationIndex < destinationToIndex) {
            destination[destinationIndex++] = get(index++)
        }
        return destination
    }
}

public val ByteList.lastIndex: Int @JvmSynthetic inline get() = size - 1

public fun ByteList.indexCheck(index: Int): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun ByteList.indexCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun ByteList.rangeCheck(fromIndex: Int, toIndex: Int) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmOverloads
public fun ByteList.binarySearch(element: Byte, fromIndex: Int = 0, toIndex: Int = size): Int {
    rangeCheck(fromIndex, toIndex)

    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = low + (high - low) / 2
        val c = element.compareTo(get(mid))

        if (c > 0) {
            low = mid + 1
        } else if (c < 0) {
            high = mid - 1
        } else {
            return mid
        }
    }
    return -(low + 1)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> ByteList.foldRight(initial: R, operation: (accumulator: R, Byte) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    var accumulator = initial
    var index = size - 1
    while (index >= 0) {
        accumulator = operation(accumulator, get(index--))
    }
    return accumulator
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteList.reduceRight(operation: (accumulator: Byte, Byte) -> Byte): Byte {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    if (isEmpty()) throw NoSuchElementException()
    var index = size - 1
    var accumulator = get(index--)
    while (index >= 0) {
        accumulator = operation(accumulator, get(index--))
    }
    return accumulator
}

public fun ByteList.asList(): List<Byte> = ByteListWrapper(this)

/**
 * A mutable list of Bytes.
 */
public interface MutableByteList : ByteList, MutableByteCollection {

    override fun listIterator(): MutableByteListIterator = listIterator(0)
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
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    override fun addAll(elements: Collection<Byte>): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: ByteCollection): Boolean {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: Collection<Byte>): Boolean {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
        return !elements.isEmpty()
    }

    public fun sort() {
        val sorted = toArray().also { it.sort() }
        for (index in 0..<sorted.size) {
            set(index, sorted[index])
        }
    }

    public fun sortDescending() {
        val sorted = toArray().also { it.sortDescending() }
        for (index in 0..<sorted.size) {
            set(index, sorted[index])
        }
    }

    public fun fill(element: Byte) {
        for (index in 0..<size) {
            set(index, element)
        }
    }

    @JvmSynthetic
    public fun shuffle(random: Random) {
        for (i in lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = get(i)
            set(i, get(j))
            set(j, tmp)
        }
    }

    public fun shuffle() { shuffle(Random) }

    public fun reverse() {
        val midPoint = (size / 2)
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

public fun MutableByteList.asList(): MutableList<Byte> = MutableByteListWrapper(this)

public abstract class AbstractByteList : AbstractByteCollection(), ByteList {

    override fun iterator(): ByteIterator = IteratorImpl()
    override fun listIterator(index: Int): ByteListIterator = ListIteratorImpl(index)

    override fun subList(fromIndex: Int, toIndex: Int): ByteList = ByteSubList(this, fromIndex, toIndex)

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is ByteList) return false
        if (size != other.size) return false

        for (i in 0..<size) {
            if (this[i] notEqualsRaw other[i]) {
                return false
            }
        }
        return true
    }

    override fun hashCode(): Int {
        var hashCode = 1
        for (element in this) {
            hashCode = 31 * hashCode + element.hashCode()
        }
        return hashCode
    }

    private inner class IteratorImpl: ByteIterator() {
        private val size = this@AbstractByteList.size
        private var index = 0

        override fun hasNext(): Boolean = index < size
        override fun nextByte(): Byte {
            if (index >= size) throw NoSuchElementException()
            if (size != this@AbstractByteList.size) throw ConcurrentModificationException()
            return get(index++)
        }
    }

    private inner class ListIteratorImpl(private var position: Int) : ByteListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private val size = this@AbstractByteList.size

        override fun hasNext(): Boolean = position < size

        override fun nextByte(): Byte {
            if (position >= size) throw NoSuchElementException()
            if (size != this@AbstractByteList.size) throw ConcurrentModificationException()
            return get(position++)
        }

        override fun hasPrevious(): Boolean = position > 0

        override fun previousByte(): Byte {
            if (position <= 0) throw NoSuchElementException()
            if (size != this@AbstractByteList.size) throw ConcurrentModificationException()
            return get(--position)
        }

        override fun nextIndex(): Int = position
        override fun previousIndex(): Int = position - 1
    }

    private class ByteSubList(private val list: ByteList, fromIndex: Int, toIndex: Int) : AbstractByteList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Byte {
            indexCheck(index)
            return list[index + offset]
        }

        override fun copyInto(destination: ByteArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): ByteArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

public abstract class AbstractMutableByteList : AbstractByteList(), MutableByteList {

    override fun iterator(): MutableByteIterator = IteratorImpl()
    override fun listIterator(index: Int): MutableByteListIterator = ListIteratorImpl(index)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val iterator = listIterator(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            iterator.previousByte()
            iterator.remove()
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableByteList = ByteSubList(this, fromIndex, toIndex)

    private inner class IteratorImpl: MutableByteIterator() {
        private var size = this@AbstractMutableByteList.size
        private var index = 0
        private var lastIndex = -1
        override fun hasNext(): Boolean = index != size
        override fun nextByte(): Byte {
            if (index == size) throw NoSuchElementException()
            if (size != this@AbstractMutableByteList.size) throw ConcurrentModificationException()
            lastIndex = index++
            return get(lastIndex)
        }
        override fun remove() {
            check(lastIndex != -1)
            removeAt(lastIndex)
            index--
            lastIndex = -1
            --size
        }
    }

    private inner class ListIteratorImpl(private var position: Int) : MutableByteListIterator() {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@AbstractMutableByteList.size
        private var index = -1

        override fun hasNext(): Boolean = position != size

        override fun nextByte(): Byte {
            if (position == size) throw NoSuchElementException()
            if (size != this@AbstractMutableByteList.size) throw ConcurrentModificationException()
            index = position++
            return get(index)
        }

        override fun hasPrevious(): Boolean = position != 0

        override fun previousByte(): Byte {
            if (position == 0) throw NoSuchElementException()
            if (size != this@AbstractMutableByteList.size) throw ConcurrentModificationException()
            index = --position
            return get(index)
        }

        override fun nextIndex(): Int = position
        override fun previousIndex(): Int = position - 1

        override fun remove() {
            check(index != -1)
            removeAt(index)
            if (position > index) --position
            index = -1
            --size
        }

        override fun set(element: Byte) {
            check(index != -1)
            set(index, element)
        }

        override fun add(element: Byte) {
            add(position, element)
            ++position
            index = -1
            ++size
        }
    }

    private class ByteSubList(private val list: MutableByteList, fromIndex: Int, toIndex: Int) : AbstractMutableByteList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Byte) {
            return list.set(indexCheck(index) + offset, element)
        }

        override fun get(index: Int): Byte {
            return list[indexCheck(index) + offset]
        }

        override fun add(index: Int, element: Byte) {
            list.add(indexCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Byte {
            val result = list.removeAt(indexCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: ByteCollection): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun addAll(index: Int, elements: Collection<Byte>): Boolean {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
            return !elements.isEmpty()
        }

        override fun copyInto(destination: ByteArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): ByteArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }
}

private object EmptyByteListIterator : ByteListIterator() {
    override fun hasNext(): Boolean = false
    override fun nextByte(): Byte = throw NoSuchElementException()
    override fun hasPrevious(): Boolean = false
    override fun previousByte(): Byte = throw NoSuchElementException()
    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}

private object EmptyByteList : AbstractByteList() {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Byte): Boolean = false
    override fun containsAll(elements: Collection<Byte>): Boolean = elements.isEmpty()
    override fun containsAll(elements: ByteCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Byte = throw IndexOutOfBoundsException()
    override fun indexOf(element: Byte): Int = -1
    override fun lastIndexOf(element: Byte): Int = -1

    override fun iterator(): ByteIterator = emptyByteIterator()
    override fun listIterator(index: Int): ByteListIterator {
        indexCheckInclusive(index)
        return EmptyByteListIterator
    }

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        rangeCheck(fromIndex, toIndex)
        return EmptyByteList
    }
}

private class SingletonByteList(private val value: Byte) : AbstractByteList() {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Byte): Boolean = value equalsRaw element

    override fun get(index: Int): Byte = if (index == 0) value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Byte): Int = if (element equalsRaw value) 0 else -1
    override fun lastIndexOf(element: Byte): Int = if (element equalsRaw value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyByteList
    }
}

private class ByteArrayListWrapper(private val array: ByteArray): AbstractByteList() {
    override val size: Int get() = array.size
    override fun get(index: Int): Byte = array[index]

    override fun iterator(): ByteIterator = object : ByteIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index < array.size
        override fun nextByte(): Byte {
            if (index >= array.size) throw NoSuchElementException()
            return array[index++]
        }
    }

    override fun copyInto(destination: ByteArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): ByteArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return array.copyInto(destination, destinationOffset, fromIndex, toIndex)
    }
}

private class ByteListWrapper(private val list: ByteList) : AbstractList<Byte>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Byte) = list.contains(element)

    override fun indexOf(element: Byte) = list.indexOf(element)
    override fun lastIndexOf(element: Byte) = list.lastIndexOf(element)

    override fun iterator(): Iterator<Byte> = IteratorWrapper(list.iterator())
    override fun listIterator(): ListIterator<Byte> = ListIteratorWrapper(list.listIterator())
    override fun listIterator(index: Int): ListIterator<Byte> = ListIteratorWrapper(list.listIterator(index))

    // wrappers exist to remove mutable operations from the underlying types
    private class IteratorWrapper(iterator: Iterator<Byte>): Iterator<Byte> by iterator
    private class ListIteratorWrapper(iterator: ListIterator<Byte>): ListIterator<Byte> by iterator
}

private class MutableByteListWrapper(private val list: MutableByteList) : AbstractMutableList<Byte>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Byte) = list.contains(element)

    override fun indexOf(element: Byte) = list.indexOf(element)
    override fun lastIndexOf(element: Byte) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Byte> = list.iterator()
    override fun listIterator(index: Int): MutableListIterator<Byte> = list.listIterator(index)

    override fun set(index: Int, element: Byte) = list.replace(index, element)

    override fun add(element: Byte) = list.add(element)
    override fun add(index: Int, element: Byte) = list.add(index, element)

    override fun remove(element: Byte): Boolean = list.remove(element)
    override fun removeAt(index: Int): Byte = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Byte> = list.subList(fromIndex, toIndex).asList()
}
