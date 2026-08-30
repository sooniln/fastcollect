/**
 * Methods for dealing with DoubleLists.
 */
@file:JvmName("DoubleLists")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic
import kotlin.random.Random

public fun emptyDoubleList(): DoubleList = EmptyDoubleList

public fun doubleListOf(): DoubleList = EmptyDoubleList
public fun doubleListOf(element: Double): DoubleList = SingletonDoubleList(element)
public fun doubleListOf(vararg elements: Double): DoubleList = DoubleArrayDeque(elements)

public fun mutableDoubleListOf(): MutableDoubleList = DoubleArrayDeque()
public fun mutableDoubleListOf(element: Double): MutableDoubleList = DoubleArrayDeque(1).apply { add(element) }
public fun mutableDoubleListOf(vararg elements: Double): MutableDoubleList = DoubleArrayDeque(elements)

public fun DoubleArray.asDoubleList(): DoubleList = DoubleArrayListWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildDoubleList(expectedSize: Int = 0, builderAction: MutableDoubleList.() -> Unit): DoubleList {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val list = DoubleArrayDeque(expectedSize)
    list.builderAction()
    return list
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleList(size: Int, init: (index: Int) -> Double): DoubleList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }
    return MutableDoubleList(size, init)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun MutableDoubleList(size: Int, init: (index: Int) -> Double): MutableDoubleList {
    contract { callsInPlace(init, InvocationKind.UNKNOWN) }

    val list = DoubleArrayDeque(size)
    repeat(size) { index -> list.add(init(index)) }
    return list
}

public interface DoubleListTraversable: DoubleTraversable {
    public fun traverser(position: Int): DoubleListTraverser
}

public interface MutableDoubleListTraversable: MutableDoubleTraversable, DoubleListTraversable {
    override fun traverser(position: Int): MutableDoubleListTraverser
}

public interface DoubleListTraverser : DoubleTraverser {
    public val position: Int
    public fun backward(): Boolean
}

public interface MutableDoubleListTraverser : DoubleListTraverser, MutableDoubleTraverser {
    public fun set(value: Double)
    public fun insert(value: Double)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleList.foreachReverse(action: (Double) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    while (traverser.backward()) {
        action(traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleListTraversable.foreachIndexed(action: (Int, Double) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(0)
    var index = 0
    while (traverser.forward()) {
        action(index++, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleList.foreachReverseIndexed(action: (Int, Double) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    var index = size
    while (traverser.backward()) {
        action(--index, traverser.value)
    }
}

/**
 * A list of Doubles.
 */
public interface DoubleList : DoubleCollection, DoubleListTraversable {

    override fun contains(element: Double): Boolean {
        return indexOf(element) != -1
    }

    public operator fun get(index: Int): Double

    public fun first(): Double = if (isEmpty()) throw NoSuchElementException() else get(0)

    public fun last(): Double = if (isEmpty()) throw NoSuchElementException() else get(lastIndex)

    public fun indexOf(element: Double): Int {
        foreachIndexed { index, value ->
            if (value equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun lastIndexOf(element: Double): Int {
        foreachReverseIndexed { index, value ->
            if (value equalsRaw element) {
                return index
            }
        }
        return -1
    }

    public fun subList(fromIndex: Int, toIndex: Int): DoubleList

    override fun copyInto(destination: DoubleArray, destinationOffset: Int): DoubleArray {
        return copyInto(destination, destinationOffset, 0, size)
    }

    /**
     * Copies the elements of this list in the range [[fromIndex], [toIndex]) into [destination], starting at
     * [destinationOffset], and returns [destination].
     */
    public fun copyInto(destination: DoubleArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): DoubleArray {
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

public val DoubleList.indices: IntRange @JvmSynthetic inline get() = 0..<size

public val DoubleList.lastIndex: Int @JvmSynthetic inline get() = size - 1

@JvmSynthetic
public fun DoubleList.copyInto(destination: DoubleArray, destinationOffset: Int = 0, fromIndex: Int = 0, toIndex: Int = destination.size): DoubleArray {
    return copyInto(destination, destinationOffset, fromIndex, toIndex)
}

public fun DoubleList.indexCheck(index: Int): Int {
    if (index !in 0..<size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun DoubleList.indexCheckInclusive(index: Int): Int {
    if (index !in 0..size) throw IndexOutOfBoundsException("index=$index, size=$size")
    return index
}

public fun DoubleList.rangeCheck(fromIndex: Int, toIndex: Int) {
    require(fromIndex <= toIndex)
    if (fromIndex < 0) throw IndexOutOfBoundsException("fromIndex=$fromIndex")
    if (toIndex > size) throw IndexOutOfBoundsException("toIndex=$toIndex, size=$size")
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> DoubleList.foldRight(initial: R, operation: (Double, accumulated: R) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreachReverse { value ->
        accumulated = operation(value, accumulated)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleList.reduceRight(operation: (Double, accumulated: Double) -> Double) : Double {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val traverser = traverser(size)
    if (!traverser.backward()) throw NoSuchElementException()
    var accumulated = traverser.value
    while (traverser.backward()) {
        accumulated = operation(traverser.value, accumulated)
    }
    return accumulated
}

public fun DoubleList.asList(): List<Double> = DoubleListWrapper(this)

/**
 * A mutable list of Doubles.
 */
public interface MutableDoubleList : DoubleList, MutableDoubleCollection, MutableDoubleListTraversable {

    override fun traverser(position: Int): MutableDoubleListTraverser

    public operator fun set(index: Int, element: Double)

    public fun replace(index: Int, element: Double): Double {
        val t = get(index)
        set(index, element)
        return t
    }

    override fun add(element: Double): Boolean {
        addLast(element)
        return true
    }

    public fun add(index: Int, element: Double)

    public fun addFirst(element: Double): Unit = add(0, element)
    public fun addLast(element: Double): Unit = add(size, element)
    public fun removeFirst(): Double = if (isEmpty()) throw NoSuchElementException() else removeAt(0)
    public fun removeLast(): Double = if (isEmpty()) throw NoSuchElementException() else removeAt(lastIndex)

    override fun remove(element: Double): Boolean {
        val index = indexOf(element)
        if (index == -1) {
            return false
        } else {
            removeAt(index)
            return true
        }
    }

    public fun removeAt(index: Int): Double

    public fun removeRange(fromIndex: Int, toIndex: Int)

    override fun clear(): Unit = removeRange(0, size)

    override fun addAll(elements: DoubleCollection): Boolean {
        elements.foreach { element ->
            addLast(element)
        }
        return !elements.isEmpty()
    }

    override fun addAll(elements: Collection<Double>): Boolean {
        for (element in elements) {
            addLast(element)
        }
        return !elements.isEmpty()
    }

    public fun addAll(index: Int, elements: DoubleCollection) {
        var i = indexCheckInclusive(index)
        elements.foreach { element ->
            add(i++, element)
        }
    }

    public fun addAll(index: Int, elements: Collection<Double>) {
        var i = indexCheckInclusive(index)
        for (element in elements) {
            add(i++, element)
        }
    }

    public fun sort() {
        val sorted = copyInto(DoubleArray(size)).also { it.sort() }
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
        val sorted = copyInto(DoubleArray(size)).also { it.sortDescending() }
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

    public fun fill(element: Double) {
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

    override fun subList(fromIndex: Int, toIndex: Int): MutableDoubleList
}

public fun MutableDoubleList.shuffle() { shuffle(Random) }

public fun MutableDoubleList.asList(): MutableList<Double> = MutableDoubleListWrapper(this)

public abstract class AbstractDoubleList : AbstractDoubleCollection(), DoubleList {

    override fun iterator(): DoubleIterator = IteratorImpl()
    override fun traverser(): DoubleTraverser = TraverserImpl()
    override fun traverser(position: Int): DoubleListTraverser = ListTraverserImpl(position)

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        return if (this is RandomAccess) {
            RandomAccessDoubleSubList(this, fromIndex, toIndex)
        } else {
            DoubleSubList(this, fromIndex, toIndex)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is DoubleList) return false
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

    private inner class IteratorImpl: DoubleIterator() {
        private val size = this@AbstractDoubleList.size
        private var index = 0

        override fun hasNext(): Boolean = index < size
        override fun nextDouble(): Double {
            if (index >= size) throw NoSuchElementException()
            if (size != this@AbstractDoubleList.size) throw ConcurrentModificationException()
            return get(index++)
        }
    }

    private inner class TraverserImpl : DoubleTraverser {
        private val last = size - 1
        private var index = -1

        override val value: Double get() {
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

    private inner class ListTraverserImpl(position: Int) : DoubleListTraverser {
        init {
            indexCheckInclusive(position)
        }

        private val size = this@AbstractDoubleList.size
        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Double get() {
            check(index >= 0)
            return get(index)
        }

        override fun forward(): Boolean {
            if (position >= size) return false
            if (size != this@AbstractDoubleList.size) throw ConcurrentModificationException()
            index = position++
            return true
        }

        override fun backward(): Boolean {
            if (position <= 0) return false
            if (size != this@AbstractDoubleList.size) throw ConcurrentModificationException()
            index = --position
            return true
        }
    }

    private open class DoubleSubList(private val list: DoubleList, fromIndex: Int, toIndex: Int) : AbstractDoubleList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            protected set

        override fun get(index: Int): Double {
            indexCheck(index)
            return list[index + offset]
        }

        override fun copyInto(destination: DoubleArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): DoubleArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }

    private class RandomAccessDoubleSubList(list: DoubleList, fromIndex: Int, toIndex: Int) : DoubleSubList(list, fromIndex, toIndex), RandomAccess
}

public abstract class AbstractMutableDoubleList : AbstractDoubleList(), MutableDoubleList {

    override fun iterator(): MutableDoubleIterator = IteratorImpl()
    override fun traverser(): MutableDoubleTraverser = ListTraverserImpl(0)
    override fun traverser(position: Int): MutableDoubleListTraverser = ListTraverserImpl(position)

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        rangeCheck(fromIndex, toIndex)

        val traverser = traverser(toIndex)
        repeat(toIndex-fromIndex) { _ ->
            check(traverser.backward())
            traverser.remove()
        }
    }

    override fun subList(fromIndex: Int, toIndex: Int): MutableDoubleList {
        return if (this is RandomAccess) {
            RandomAccessDoubleSubList(this, fromIndex, toIndex)
        } else {
            DoubleSubList(this, fromIndex, toIndex)
        }
    }

    private inner class IteratorImpl: MutableDoubleIterator() {
        private var size = this@AbstractMutableDoubleList.size
        private var index = 0
        private var lastIndex = -1
        override fun hasNext(): Boolean = index != size
        override fun nextDouble(): Double {
            if (index == size) throw NoSuchElementException()
            if (size != this@AbstractMutableDoubleList.size) throw ConcurrentModificationException()
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

    private inner class ListTraverserImpl(position: Int) : MutableDoubleListTraverser {
        init {
            indexCheckInclusive(position)
        }

        private var size = this@AbstractMutableDoubleList.size
        private var index = position - 1

        override var position: Int = position
            private set
        override val value: Double get() {
            check(index != -1)
            return get(index)
        }

        override fun forward(): Boolean {
            if (position == size) return false
            if (size != this@AbstractMutableDoubleList.size) throw ConcurrentModificationException()
            index = position++
            return true
        }

        override fun backward(): Boolean {
            if (position == 0) return false
            if (size != this@AbstractMutableDoubleList.size) throw ConcurrentModificationException()
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

        override fun set(value: Double) {
            check(index != -1)
            set(index, value)
        }

        override fun insert(value: Double) {
            add(position, value)
            ++position
            index = -1
            ++size
        }
    }

    private open class DoubleSubList(private val list: MutableDoubleList, fromIndex: Int, toIndex: Int) : AbstractMutableDoubleList() {

        init {
            list.rangeCheck(fromIndex, toIndex)
        }

        private val offset = fromIndex
        final override var size = toIndex - fromIndex
            private set

        override fun set(index: Int, element: Double) {
            return list.set(indexCheck(index) + offset, element)
        }

        override fun get(index: Int): Double {
            return list[indexCheck(index) + offset]
        }

        override fun add(index: Int, element: Double) {
            list.add(indexCheckInclusive(index) + offset, element)
            size++
        }

        override fun removeAt(index: Int): Double {
            val result = list.removeAt(indexCheck(index) + offset)
            size--
            return result
        }

        override fun removeRange(fromIndex: Int, toIndex: Int) {
            rangeCheck(fromIndex, toIndex)
            list.removeRange(fromIndex + offset, toIndex + offset)
            size -= toIndex - fromIndex
        }

        override fun addAll(index: Int, elements: DoubleCollection) {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
        }

        override fun addAll(index: Int, elements: Collection<Double>) {
            list.addAll(offset + indexCheckInclusive(index), elements)
            size += elements.size
        }

        override fun copyInto(destination: DoubleArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): DoubleArray {
            rangeCheck(fromIndex, toIndex)
            return list.copyInto(destination, destinationOffset, fromIndex + offset, toIndex + offset)
        }
    }

    private class RandomAccessDoubleSubList(list: MutableDoubleList, fromIndex: Int, toIndex: Int) : DoubleSubList(list, fromIndex, toIndex), RandomAccess
}

private object EmptyDoubleListTraverser : DoubleListTraverser {
    override val position: Int get() = 0
    override val value: Double get() = throw IllegalStateException()
    override fun forward(): Boolean = false
    override fun backward(): Boolean = false
}

private object EmptyDoubleList : AbstractDoubleList(), RandomAccess {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Double): Boolean = false
    override fun containsAll(elements: Collection<Double>): Boolean = elements.isEmpty()
    override fun containsAll(elements: DoubleCollection): Boolean = elements.isEmpty()

    override fun get(index: Int): Double = throw IndexOutOfBoundsException()
    override fun indexOf(element: Double): Int = -1
    override fun lastIndexOf(element: Double): Int = -1

    override fun iterator(): DoubleIterator = emptyDoubleIterator()
    override fun traverser(): DoubleTraverser = EmptyDoubleListTraverser
    override fun traverser(position: Int): DoubleListTraverser {
        indexCheckInclusive(position)
        return EmptyDoubleListTraverser
    }

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        rangeCheck(fromIndex, toIndex)
        return EmptyDoubleList
    }
}

private class SingletonDoubleList(private val value: Double) : AbstractDoubleList(), RandomAccess {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Double): Boolean = value equalsRaw element

    override fun get(index: Int): Double = if (index == 0) value else throw IndexOutOfBoundsException()
    override fun indexOf(element: Double): Int = if (element equalsRaw value) 0 else -1
    override fun lastIndexOf(element: Double): Int = if (element equalsRaw value) 0 else -1

    override fun subList(fromIndex: Int, toIndex: Int): DoubleList {
        rangeCheck(fromIndex, toIndex)
        return if (fromIndex == 0 && toIndex == 1) this else EmptyDoubleList
    }
}

private class DoubleArrayListWrapper(private val array: DoubleArray): AbstractDoubleList(), RandomAccess {
    override val size: Int get() = array.size
    override fun get(index: Int): Double = array[index]

    override fun iterator(): DoubleIterator = object : DoubleIterator() {
        private var index = 0
        override fun hasNext(): Boolean = index < array.size
        override fun nextDouble(): Double {
            if (index >= array.size) throw NoSuchElementException()
            return array[index++]
        }
    }

    override fun traverser(): DoubleTraverser = object : DoubleTraverser {
        private var index = -1
        override val value: Double get() {
            check(index >= 0)
            return array[index]
        }
        override fun forward(): Boolean {
            if (index >= array.lastIndex) return false
            ++index
            return true
        }
    }

    override fun copyInto(destination: DoubleArray, destinationOffset: Int, fromIndex: Int, toIndex: Int): DoubleArray {
        rangeCheck(fromIndex, toIndex)
        destination.rangeCheck(destinationOffset, destinationOffset + toIndex - fromIndex)
        return array.copyInto(destination, destinationOffset, fromIndex, toIndex)
    }
}

private class DoubleListWrapper(private val list: DoubleList) : AbstractList<Double>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Double) = list.contains(element)

    override fun indexOf(element: Double) = list.indexOf(element)
    override fun lastIndexOf(element: Double) = list.lastIndexOf(element)

    override fun iterator(): Iterator<Double> = listIterator()
    override fun listIterator(): ListIterator<Double> = ListIteratorImpl(0)
    override fun listIterator(index: Int): ListIterator<Double> = ListIteratorImpl(index)

    private inner class ListIteratorImpl(position: Int): ListIterator<Double> {
        private val size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Double {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Double {
            if (!hasPrevious()) throw NoSuchElementException()
            traverser.backward()
            return traverser.value
        }
        override fun nextIndex(): Int = traverser.position
        override fun previousIndex(): Int = traverser.position - 1
    }
}

private class MutableDoubleListWrapper(private val list: MutableDoubleList) : AbstractMutableList<Double>() {
    override val size: Int get() = list.size

    override fun get(index: Int) = list[index]
    override fun contains(element: Double) = list.contains(element)

    override fun indexOf(element: Double) = list.indexOf(element)
    override fun lastIndexOf(element: Double) = list.lastIndexOf(element)

    override fun iterator(): MutableIterator<Double> = listIterator()
    override fun listIterator(): MutableListIterator<Double> = ListIteratorImpl(0)
    override fun listIterator(index: Int): MutableListIterator<Double> = ListIteratorImpl(index)

    override fun set(index: Int, element: Double) = list.replace(index, element)

    override fun add(element: Double) = list.add(element)
    override fun add(index: Int, element: Double) = list.add(index, element)

    override fun remove(element: Double): Boolean = list.remove(element)
    override fun removeAt(index: Int): Double = list.removeAt(index)
    override fun removeRange(fromIndex: Int, toIndex: Int) = list.removeRange(fromIndex, toIndex)

    override fun clear() = list.clear()

    override fun subList(fromIndex: Int, toIndex: Int): MutableList<Double> = list.subList(fromIndex, toIndex).asList()

    private inner class ListIteratorImpl(position: Int): MutableListIterator<Double> {
        private var size = list.size
        private val traverser = list.traverser(position)

        override fun hasNext(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != size
        }
        override fun next(): Double {
            if (!hasNext()) throw NoSuchElementException()
            traverser.forward()
            return traverser.value
        }
        override fun hasPrevious(): Boolean {
            if (list.size != size) throw ConcurrentModificationException()
            return traverser.position != 0
        }
        override fun previous(): Double {
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
        override fun set(element: Double) = traverser.set(element)
        override fun add(element: Double) {
            traverser.insert(element)
            ++size
        }
    }
}
