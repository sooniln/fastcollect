/**
 * Methods for dealing with ByteLists.
 */
@file:JvmName("ByteLists")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.random.Random

public fun emptyByteList(): ByteList = EmptyByteList

public fun byteListOf(): ByteList = EmptyByteList
public fun byteListOf(element: Byte): ByteList = SingletonByteList(element)
public fun byteListOf(vararg elements: Byte): ByteList = ByteArrayDeque(elements)

public fun mutableByteListOf(): MutableByteList = ByteArrayDeque()
public fun mutableByteListOf(element: Byte): MutableByteList = ByteArrayDeque(1).apply { add(element) }
public fun mutableByteListOf(vararg elements: Byte): MutableByteList = ByteArrayDeque(elements)

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

public interface ByteListTraversable: ByteTraversable {
    public fun traverser(position: Int): ByteListTraverser
}

public interface MutableByteListTraversable: MutableByteTraversable, ByteListTraversable {
    override fun traverser(position: Int): MutableByteListTraverser
}

public interface ByteListTraverser : ByteTraverser {
    public val position: Int
    public fun backward(): Boolean
}

public interface MutableByteListTraverser : ByteListTraverser, MutableByteTraverser {
    public fun set(value: Byte)
    public fun insert(value: Byte)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteList.foreachReverse(action: (Byte) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    while (traverser.backward()) {
        action(traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteListTraversable.foreachIndexed(action: (Int, Byte) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(0)
    var index = 0
    while (traverser.forward()) {
        action(index++, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteList.foreachReverseIndexed(action: (Int, Byte) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    var index = size
    while (traverser.backward()) {
        action(--index, traverser.value)
    }
}

/**
 * A list of Bytes.
 */
public interface ByteList : ByteCollection, ByteListTraversable {

    override fun contains(element: Byte): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Byte

    public fun indexOf(element: Byte): Int {
        foreachIndexed { index, value ->
            if (value equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Byte): Int {
        foreachReverseIndexed { index, value ->
            if (value equalsRaw element) {
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
     * [destinationOffset], and returns [destination].
     */
    public fun copyInto(destination: ByteArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): ByteArray {
        rangeCheck(fromIndex, toIndex)
        val destinationToIndex = destinationOffset + toIndex - fromIndex
        destination.rangeCheck(destinationOffset, destinationToIndex)

        val traverser = traverser(fromIndex)
        for (index in destinationOffset..<destinationToIndex) {
            check(traverser.forward())
            destination[index] = traverser.value
        }
        return destination
    }
}

public val ByteList.indices: IntRange @JvmSynthetic inline get() = 0..<size

public val ByteList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun ByteList.first(): Byte = if (isEmpty()) throw NoSuchElementException() else this[0]

@JvmSynthetic
public fun ByteList.last(): Byte = if (isEmpty()) throw NoSuchElementException() else this[lastIndex]

@JvmSynthetic
public fun ByteList.copyInto(destination: ByteArray, destinationOffset: Int = 0, fromIndex: Int = 0, toIndex: Int = destination.size): ByteArray {
    return copyInto(destination, destinationOffset, fromIndex, toIndex)
}

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

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> ByteList.foldRight(initial: R, operation: (Byte, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreachReverse { value ->
        accumulated = operation(value, accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteList.reduceRight(operation: (Byte, accumulated: Byte) -> Byte) : Byte {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    if (!traverser.backward()) throw NoSuchElementException()
    var accumulated = traverser.value
    while (traverser.backward()) {
        accumulated = operation(traverser.value, accumulated)
    }
    return accumulated
}

public fun ByteList.asList(): List<Byte> = ByteListWrapper(this)

/**
 * A mutable list of Bytes.
 */
public interface MutableByteList : ByteList, MutableByteCollection, MutableByteListTraversable {

    override fun traverser(position: Int): MutableByteListTraverser

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
        elements.foreach { element ->
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

    public fun addAll(index: Int, elements: ByteCollection) {
        var i = indexCheckInclusive(index)
        elements.foreach { element ->
            add(i++, element)
        }
    }

    public fun addAll(index: Int, elements: Collection<Byte>) {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
    }

    public fun sort() {
        val sorted = copyInto(ByteArray(size)).also { it.sort() }
        if (this is RandomAccess) {
            for (index in 0..<sorted.size) {
                set(index, sorted[index])
            }
        } else {
            val traverser = traverser(0)
            for (element in sorted) {
                check(traverser.forward())
                traverser.set(element)
            }
        }
    }

    public fun sortDescending() {
        val sorted = copyInto(ByteArray(size)).also { it.sortDescending() }
        if (this is RandomAccess) {
            for (index in 0..<sorted.size) {
                set(index, sorted[index])
            }
        } else {
            val traverser = traverser(0)
            for (element in sorted) {
                check(traverser.forward())
                traverser.set(element)
            }
        }
    }

    public fun fill(element: Byte) {
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

    public fun shuffle(random: Random) {
        for (i in lastIndex downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = get(i)
            set(i, get(j))
            set(j, tmp)
        }
    }

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

public fun MutableByteList.shuffle() { shuffle(Random) }

public fun MutableByteList.asList(): MutableList<Byte> = MutableByteListWrapper(this)

public abstract class AbstractByteList : AbstractByteCollection(), ByteList {

    override fun iterator(): ByteIterator = IteratorImpl()
    override fun traverser(): ByteTraverser = TraverserImpl()
    override fun traverser(position: Int): ByteListTraverser = ListTraverserImpl(position)

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

        val traverser = traverser()
        val otherTraverser = other.traverser()

        var hasNext = traverser.forward()
        var otherHasNext = otherTraverser.forward()
        while (hasNext && otherHasNext) {
            if (!(traverser.value equalsRaw otherTraverser.value)) {
                return false
            }
            hasNext = traverser.forward()
            otherHasNext = otherTraverser.forward()
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

    private inner class TraverserImpl : ByteTraverser {
        private val last = size - 1
        private var index = -1

        override val value: Byte get() {
            check(index >= 0)
            return get(index)
        }

        override fun forward(): Boolean {
            if (index >= last) return false
            if (last != size - 1) throw ConcurrentModificationException()
            ++index
            return true
        }
    }

    private inner class ListTraverserImpl(position: Int) : ByteListTraverser {
        init {
            indexCheckInclusive(position)
        }

        private val size = this@AbstractByteList.size
        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Byte get() {
            check(index >= 0)
            return get(index)
        }

        override fun forward(): Boolean {
            if (position >= size) return false
            if (size != this@AbstractByteList.size) throw ConcurrentModificationException()
            index = position++
            return true
        }

        override fun backward(): Boolean {
            if (position <= 0) return false
            if (size != this@AbstractByteList.size) throw ConcurrentModificationException()
            index = --position
            return true
        }
    }

    private open class ByteSubList(private val list: ByteList, fromIndex: Int, toIndex: Int) : AbstractByteList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
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

    private class RandomAccessByteSubList(list: ByteList, fromIndex: Int, toIndex: Int) : ByteSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableByteList : AbstractByteList(), MutableByteList {

    override fun iterator(): MutableByteIterator = IteratorImpl()
    override fun traverser(): MutableByteTraverser = ListTraverserImpl(0)
    override fun traverser(position: Int): MutableByteListTraverser = ListTraverserImpl(position)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val traverser = traverser(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            check(traverser.backward())
            traverser.remove()
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableByteList {
        return if (this is RandomAccess) {
            RandomAccessByteSubList(this, fromIndex, toIndex)
        } else {
            ByteSubList(this, fromIndex, toIndex)
        }
    }

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

    private inner class ListTraverserImpl(position: Int) : MutableByteListTraverser {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@AbstractMutableByteList.size
        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Byte get() {
            check(index != -1)
            return get(index)
        }

        override fun forward(): Boolean {
            if (position == size) return false
            if (size != this@AbstractMutableByteList.size) throw ConcurrentModificationException()
            index = position++
            return true
        }

        override fun backward(): Boolean {
            if (position == 0) return false
            if (size != this@AbstractMutableByteList.size) throw ConcurrentModificationException()
            index = --position
            return true
        }

        override fun remove() {
            check(index != -1)
            removeAt(index)
            if (position > index) --position
            index = -1
            --size
        }

        override fun set(value: Byte) {
            check(index != -1)
            set(index, value)
        }

        override fun insert(value: Byte) {
            add(position, value)
            ++position
            index = -1
            ++size
        }
    }

    private open class ByteSubList(private val list: MutableByteList, fromIndex: Int, toIndex: Int) : AbstractMutableByteList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
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

        override fun addAll(index: Int, elements: ByteCollection) {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Byte>) {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
        }

        override fun copyInto(destination: ByteArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): ByteArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }

    private class RandomAccessByteSubList(list: MutableByteList, fromIndex: Int, toIndex: Int) : ByteSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyByteListTraverser : ByteListTraverser {
    override val position: Int get() = 0
    override val value: Byte get() = throw IllegalStateException()
    override fun forward(): Boolean = false
    override fun backward(): Boolean = false
}

private object EmptyByteList : AbstractByteList(), RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Byte): Boolean = false
    override fun containsAll(elements: Collection<Byte>): Boolean = elements.isEmpty()
    override fun containsAll(elements: ByteCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Byte = throw IndexOutOfBoundsException()
    override fun indexOf(element: Byte): Int = -1
    override fun lastIndexOf(element: Byte): Int = -1

    override fun iterator(): ByteIterator = emptyByteIterator()
    override fun traverser(): ByteTraverser = EmptyByteListTraverser
    override fun traverser(position: Int): ByteListTraverser {
        indexCheckInclusive(position)
        return EmptyByteListTraverser
    }

    override fun subList(fromIndex: Int, toIndex: Int): ByteList {
        rangeCheck(fromIndex, toIndex)
        return EmptyByteList
    }
}

private class SingletonByteList(private val value: Byte) : AbstractByteList(), RandomAccess {
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

private class ByteArrayListWrapper(private val array: ByteArray): AbstractByteList(), RandomAccess {
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

    override fun traverser(): ByteTraverser = object : ByteTraverser {
        private var index = -1
        override val value: Byte get() {
            check(index >= 0)
            return array[index]
        }
        override fun forward(): Boolean {
            if (index >= array.lastIndex) return false
            ++index
            return true
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

    override fun iterator(): Iterator<Byte> = listIterator()
    override fun listIterator(): ListIterator<Byte> = ListIteratorImpl(0)
    override fun listIterator(index: Int): ListIterator<Byte> = ListIteratorImpl(index)

    private inner class ListIteratorImpl(position: Int): ListIterator<Byte> {
        private val size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Byte {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Byte {
            if (!hasPrevious()) throw NoSuchElementException()
            traverser.backward()
            return traverser.value
        }
        override fun nextIndex(): Int = traverser.position
        override fun previousIndex(): Int = traverser.position - 1
    }
}

private class MutableByteListWrapper(private val list: MutableByteList) : AbstractMutableList<Byte>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Byte) = list.contains(element)

    override fun indexOf(element: Byte) = list.indexOf(element)
    override fun lastIndexOf(element: Byte) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Byte> = listIterator()
    override fun listIterator(): MutableListIterator<Byte> = ListIteratorImpl(0)
    override fun listIterator(index: Int): MutableListIterator<Byte> = ListIteratorImpl(index)

    override fun set(index: Int, element: Byte) = list.replace(index, element)

    override fun add(element: Byte) = list.add(element)
    override fun add(index: Int, element: Byte) = list.add(index, element)

    override fun remove(element: Byte): Boolean = list.remove(element)
    override fun removeAt(index: Int): Byte = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Byte> = list.subList(fromIndex, toIndex).asList()

    private inner class ListIteratorImpl(position: Int): MutableListIterator<Byte> {
        private var size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Byte {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Byte {
            if (!hasPrevious()) throw NoSuchElementException()
            traverser.backward()
            return traverser.value
        }
        override fun nextIndex(): Int = traverser.position
        override fun previousIndex(): Int = traverser.position - 1
        override fun remove() {
            traverser.remove()
            --size
        }
        override fun set(element: Byte) = traverser.set(element)
        override fun add(element: Byte) {
            traverser.insert(element)
            ++size
        }
    }
}
