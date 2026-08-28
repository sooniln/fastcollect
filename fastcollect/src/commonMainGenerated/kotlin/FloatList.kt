/**
 * Methods for dealing with FloatLists.
 */
@file:JvmName("FloatLists")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
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
@OptIn(ExperimentalContracts::class)
public inline fun buildFloatList(expectedSize: Int = 0, builderAction: MutableFloatList.() -> Unit): FloatList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = FloatArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatList(size: Int, init: (index: Int) -> Float): FloatList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableFloatList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableFloatList(size: Int, init: (index: Int) -> Float): MutableFloatList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = FloatArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

public interface FloatListTraversable: FloatTraversable {
    public fun traverser(position: Int): FloatListTraverser
}

public interface MutableFloatListTraversable: MutableFloatTraversable, FloatListTraversable {
    override fun traverser(position: Int): MutableFloatListTraverser
}

public interface FloatListTraverser : FloatTraverser {
    public val position: Int
    public fun backward(): Boolean
}

public interface MutableFloatListTraverser : FloatListTraverser, MutableFloatTraverser {
    public fun set(value: Float)
    public fun insert(value: Float)
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatList.foreachReverse(action: (Float) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    while (traverser.backward()) {
        action(traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatListTraversable.foreachIndexed(action: (Int, Float) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(0)
    var index = 0
    while (traverser.forward()) {
        action(index++, traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatList.foreachReverseIndexed(action: (Int, Float) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    var index = size
    while (traverser.backward()) {
        action(--index, traverser.value)
    }
}

/**
 * A list of Floats.
 */
public interface FloatList : FloatCollection, FloatListTraversable {

    override fun contains(element: Float): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Float

    public fun indexOf(element: Float): Int {
        foreachIndexed { index, value ->
            if (value equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Float): Int {
        foreachReverseIndexed { index, value ->
            if (value equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): FloatList

    override fun copyInto(destination: FloatArray, destinationOffset: Int): FloatArray {
        return copyInto(destination, destinationOffset, 0, size)
    }

    /**
     * Copies the elements of this list in the range [[fromIndex], [toIndex]) into [destination], starting at
     * [destinationOffset], and returns [destination].
     */
    public fun copyInto(destination: FloatArray, destinationOffset: Int = 0, fromIndex: Int, toIndex: Int): FloatArray {
        rangeCheck(fromIndex, toIndex)
        val destinationToIndex = destinationOffset + toIndex - fromIndex
        destination.rangeCheck(destinationOffset, destinationToIndex)

        val traverser = traverser(fromIndex)
        for (index in destinationOffset..destinationToIndex) {
            check(traverser.forward())
            destination[index] = traverser.value
        }
        return destination
    }
}

public val FloatList.indices: IntRange @JvmSynthetic inline get() = 0..<size

public val FloatList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun FloatList.first(): Float = if (isEmpty()) throw NoSuchElementException() else this[0]

@JvmSynthetic
public fun FloatList.last(): Float = if (isEmpty()) throw NoSuchElementException() else this[lastIndex]

public fun FloatList.indexCheck(index: Int): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun FloatList.indexCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun FloatList.rangeCheck(fromIndex: Int, toIndex: Int) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> FloatList.foldRight(initial: R, operation: (Float, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreachReverse { value ->
        accumulated = operation(value, accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatList.reduceRight(operation: (Float, accumulated: Float) -> Float) : Float {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    if (!traverser.backward()) throw NoSuchElementException()
    var accumulated = traverser.value
    while (traverser.backward()) {
        accumulated = operation(traverser.value, accumulated)
    }
    return accumulated
}

public fun FloatList.asList(): List<Float> = FloatListWrapper(this)

/**
 * A mutable list of Floats.
 */
public interface MutableFloatList : FloatList, MutableFloatCollection, MutableFloatListTraversable {

    override fun traverser(position: Int): MutableFloatListTraverser

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
        elements.foreach { element ->
            addLast(element)
        }
        return !elements.isEmpty()
    }

    override fun addAll(elements: Collection<Float>): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: FloatCollection) {
        var i = indexCheckInclusive(index)
        elements.foreach { element ->
            add(i++, element)
        }
    }

    public fun addAll(index: Int, elements: Collection<Float>) {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
    }

    public fun sort() {
        val sorted = copyInto(FloatArray(size)).also { it.sort() }
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
        val sorted = copyInto(FloatArray(size)).also { it.sortDescending() }
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

    public fun fill(element: Float) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableFloatList
}

public fun MutableFloatList.shuffle() { shuffle(Random) }

public fun MutableFloatList.asList(): MutableList<Float> = MutableFloatListWrapper(this)

public abstract class AbstractFloatList : AbstractFloatCollection(), FloatList {

    override fun iterator(): FloatIterator = IteratorImpl()
    override fun traverser(): FloatTraverser = TraverserImpl()
    override fun traverser(position: Int): FloatListTraverser = ListTraverserImpl(position)

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

    private inner class IteratorImpl: FloatIterator() {
        private val size = this@AbstractFloatList.size
        private var index = 0

        override fun hasNext(): Boolean = index < size
        override fun nextFloat(): Float {
            if (index >= size) throw NoSuchElementException()
            if (size != this@AbstractFloatList.size) throw ConcurrentModificationException()
            return get(index++)
        }
    }

    private inner class TraverserImpl : FloatTraverser {
        private val last = size - 1
        private var index = -1

        override val value: Float get() {
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

    private inner class ListTraverserImpl(position: Int) : FloatListTraverser {
        init {
            indexCheckInclusive(position)
        }

        private val size = this@AbstractFloatList.size
        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Float get() {
            check(index >= 0)
            return get(index)
        }

        override fun forward(): Boolean {
            if (position >= size) return false
            if (size != this@AbstractFloatList.size) throw ConcurrentModificationException()
            index = position++
            return true
        }

        override fun backward(): Boolean {
            if (position <= 0) return false
            if (size != this@AbstractFloatList.size) throw ConcurrentModificationException()
            index = --position
            return true
        }
    }

    private open class FloatSubList(private val list: FloatList, fromIndex: Int, toIndex: Int) : AbstractFloatList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Float {
            indexCheck(index)
            return list[index + offset]
        }

        override fun copyInto(destination: FloatArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): FloatArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }

    private class RandomAccessFloatSubList(list: FloatList, fromIndex: Int, toIndex: Int) : FloatSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableFloatList : AbstractFloatList(), MutableFloatList {

    override fun iterator(): MutableFloatIterator = IteratorImpl()
    override fun traverser(): MutableFloatTraverser = ListTraverserImpl(0)
    override fun traverser(position: Int): MutableFloatListTraverser = ListTraverserImpl(position)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val traverser = traverser(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            check(traverser.backward())
            traverser.remove()
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableFloatList {
        return if (this is RandomAccess) {
            RandomAccessFloatSubList(this, fromIndex, toIndex)
        } else {
            FloatSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableFloatIterator() {
        private var size = this@AbstractMutableFloatList.size
        private var index = 0
        private var lastIndex = -1
        override fun hasNext(): Boolean = index != size
        override fun nextFloat(): Float {
            if (index == size) throw NoSuchElementException()
            if (size != this@AbstractMutableFloatList.size) throw ConcurrentModificationException()
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

    private inner class ListTraverserImpl(position: Int) : MutableFloatListTraverser {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@AbstractMutableFloatList.size
        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Float get() {
            check(index != -1)
            return get(index)
        }

        override fun forward(): Boolean {
            if (position == size) return false
            if (size != this@AbstractMutableFloatList.size) throw ConcurrentModificationException()
            index = position++
            return true
        }

        override fun backward(): Boolean {
            if (position == 0) return false
            if (size != this@AbstractMutableFloatList.size) throw ConcurrentModificationException()
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

        override fun set(value: Float) {
            check(index != -1)
            set(index, value)
        }

        override fun insert(value: Float) {
            add(position, value)
            ++position
            index = -1
            ++size
        }
    }

    private open class FloatSubList(private val list: MutableFloatList, fromIndex: Int, toIndex: Int) : AbstractMutableFloatList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Float) {
            return list.set(indexCheck(index) + offset, element)
        }

        override fun get(index: Int): Float {
            return list[indexCheck(index) + offset]
        }

        override fun add(index: Int, element: Float) {
            list.add(indexCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Float {
            val result = list.removeAt(indexCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: FloatCollection) {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Float>) {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
        }

        override fun copyInto(destination: FloatArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): FloatArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }

    private class RandomAccessFloatSubList(list: MutableFloatList, fromIndex: Int, toIndex: Int) : FloatSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyFloatListTraverser : FloatListTraverser {
    override val position: Int get() = 0
    override val value: Float get() = throw IllegalStateException()
    override fun forward(): Boolean = false
    override fun backward(): Boolean = false
}

private object EmptyFloatList : AbstractFloatList(), RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Float): Boolean = false
    override fun containsAll(elements: Collection<Float>): Boolean = elements.isEmpty()
    override fun containsAll(elements: FloatCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Float = throw IndexOutOfBoundsException()
    override fun indexOf(element: Float): Int = -1
    override fun lastIndexOf(element: Float): Int = -1

    override fun iterator(): FloatIterator = emptyFloatIterator()
    override fun traverser(): FloatTraverser = EmptyFloatListTraverser
    override fun traverser(position: Int): FloatListTraverser {
        indexCheckInclusive(position)
        return EmptyFloatListTraverser
    }

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        if (fromIndex != 0 || toIndex != 0) throw IndexOutOfBoundsException()
        return EmptyFloatList
    }
}

private class SingletonFloatList(private val value: Float) : AbstractFloatList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Float): Boolean = value equalsRaw element

    override fun get(index: Int): Float = if (index == 0) value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Float): Int = if (element equalsRaw value) 0 else -1
    override fun lastIndexOf(element: Float): Int = if (element equalsRaw value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): FloatList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyFloatList
    }
}

private class FloatArrayListWrapper(private val array: FloatArray): AbstractFloatList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Float = array[index]

    override fun copyInto(destination: FloatArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): FloatArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return array.copyInto(destination, destinationOffset, fromIndex, toIndex)
    }
}

private class FloatListWrapper(private val list: FloatList) : AbstractList<Float>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Float) = list.contains(element)

    override fun indexOf(element: Float) = list.indexOf(element)
    override fun lastIndexOf(element: Float) = list.lastIndexOf(element)

    override fun iterator(): Iterator<Float> = listIterator()
    override fun listIterator(): ListIterator<Float> = ListIteratorImpl(0)
    override fun listIterator(index: Int): ListIterator<Float> = ListIteratorImpl(index)

    private inner class ListIteratorImpl(position: Int): ListIterator<Float> {
        private val size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Float {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Float {
            if (!hasPrevious()) throw NoSuchElementException()
            traverser.backward()
            return traverser.value
        }
        override fun nextIndex(): Int = traverser.position
        override fun previousIndex(): Int = traverser.position - 1
    }
}

private class MutableFloatListWrapper(private val list: MutableFloatList) : AbstractMutableList<Float>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Float) = list.contains(element)

    override fun indexOf(element: Float) = list.indexOf(element)
    override fun lastIndexOf(element: Float) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Float> = listIterator()
    override fun listIterator(): MutableListIterator<Float> = ListIteratorImpl(0)
    override fun listIterator(index: Int): MutableListIterator<Float> = ListIteratorImpl(index)

    override fun set(index: Int, element: Float) = list.replace(index, element)

    override fun add(element: Float) = list.add(element)
    override fun add(index: Int, element: Float) = list.add(index, element)

    override fun remove(element: Float): Boolean = list.remove(element)
    override fun removeAt(index: Int): Float = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Float> = list.subList(fromIndex, toIndex).asList()

    private inner class ListIteratorImpl(position: Int): MutableListIterator<Float> {
        private var size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Float {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Float {
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
        override fun set(element: Float) = traverser.set(element)
        override fun add(element: Float) {
            traverser.insert(element)
            ++size
        }
    }
}
