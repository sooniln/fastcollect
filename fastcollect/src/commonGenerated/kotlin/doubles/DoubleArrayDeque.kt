package io.github.sooniln.fastcollect.doubles

import io.github.sooniln.fastcollect.longs.LongArrayDeque
import io.github.sooniln.fastcollect.longs.LongList
import io.github.sooniln.fastcollect.longs.MutableLongList
import io.github.sooniln.fastcollect.longs.MutableLongListIterator
import io.github.sooniln.fastcollect.longs.filterInPlace
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

public typealias DoubleArrayDeque = InlineDoubleArrayDeque
public typealias DoubleArrayList = DoubleArrayDeque

@Suppress("OVERRIDE_BY_INLINE")
@JvmInline
public value class InlineDoubleArrayDeque private constructor(@PublishedApi internal val list: LongArrayDeque) : MutableDoubleList {

    public constructor(capacity: Int = 0) : this(LongArrayDeque(capacity))
    public constructor(elements: DoubleCollection) : this() { addAll(elements) }
    public constructor(elements: Collection<Double>) : this() { addAll(elements) }
    public constructor(elements: DoubleArray, fromIndex:Int = 0, toIndex: Int = elements.size) : this(toIndex - fromIndex) { for (i in fromIndex..<toIndex) add(elements[i]) }

    override val size: Int
        inline get() = list.size

    public fun ensureCapacity(capacity: Int) { list.ensureCapacity(capacity) }
    public fun trimToSize() { list.trimToSize() }

    override fun get(index: Int): Double = Double.fromBits(list[index])
    override fun set(index: Int, element: Double) { list[index] = element.toBits() }
    override fun addFirst(element: Double) { list.addFirst(element.toBits()) }
    override fun addLast(element: Double) { list.addLast(element.toBits()) }
    override fun add(index: Int, element: Double) { list.add(index, element.toBits()) }
    override fun removeFirst(): Double = Double.fromBits(list.removeFirst())
    override fun removeLast(): Double = Double.fromBits(list.removeLast())
    override fun removeAt(index: Int): Double = Double.fromBits(list.removeAt(index))
    override fun removeRange(fromIndex: Int, toIndex: Int) { list.removeRange(fromIndex, toIndex) }
    override fun clear() { list.clear() }
    override fun indexOf(element: Double): Int = list.indexOf(element.toBits())
    override fun lastIndexOf(element: Double): Int = list.lastIndexOf(element.toBits())

    override fun addAll(elements: DoubleCollection): Boolean {
        if (elements is InlineDoubleArrayDeque) return list.addAll(elements.list)

        ensureCapacity(size + elements.size)
        for (element in elements) list.addLast(element.toBits())
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Double>): Boolean {
        if (elements is DoubleCollection) return addAll(elements)

        ensureCapacity(size + elements.size)
        for (element in elements) list.addLast(element.toBits())
        return !elements.isEmpty()
    }
    override fun addAll(index: Int, elements: DoubleCollection) {
        if (elements is InlineDoubleArrayDeque) {
            list.addAll(index, elements.list)
        } else {
            ensureCapacity(size + elements.size)
            var i = index
            for (element in elements) list.add(i++, element.toBits())
        }
    }
    override fun addAll(index: Int, elements: Collection<Double>) {
        if (elements is DoubleCollection) {
            addAll(index, elements)
        } else {
            ensureCapacity(size + elements.size)
            var i = index
            for (element in elements) list.add(i++, element.toBits())
        }
    }

    public override fun removeAll(elements: Collection<Double>): Boolean = list.filterDoubleInPlace { e -> elements.contains(e) }
    public override fun retainAll(elements: Collection<Double>): Boolean = list.filterDoubleInPlace { e -> !elements.contains(e) }

    override fun sort() { list.sortDouble() }
    override fun sortDescending() { list.sortDescendingDouble() }
    override fun fill(element: Double) { list.fill(element.toBits()) }
    override fun reverse() { list.reverse() }

    override fun iterator(): InlineMutableDoubleIterator = InlineMutableDoubleIterator(list.iterator())
    override fun listIterator(): InlineMutableDoubleListIterator = InlineMutableDoubleListIterator(list.listIterator())
    override fun listIterator(index: Int): InlineMutableDoubleListIterator = InlineMutableDoubleListIterator(list.listIterator(index))
    override fun subList(fromIndex: Int, toIndex: Int): InlineDoubleList = InlineDoubleList(list.subList(fromIndex, toIndex))

    override fun fastForEach(action: (Double) -> Unit) {
        list.fastForEach { element -> action(Double.fromBits(element))}
    }

    override fun toString(): String = list.toDoubleListString()
}

@Suppress("OVERRIDE_BY_INLINE")
@JvmInline
public value class InlineDoubleList internal constructor(@PublishedApi internal val list: MutableLongList) : MutableDoubleList {

    override val size: Int
        inline get() = list.size

    override fun get(index: Int): Double = Double.fromBits(list[index])
    override fun set(index: Int, element: Double) { list[index] = element.toBits() }
    override fun addFirst(element: Double) { list.addFirst(element.toBits()) }
    override fun addLast(element: Double) { list.addLast(element.toBits()) }
    override fun add(index: Int, element: Double) { list.add(index, element.toBits()) }
    override fun removeFirst(): Double = Double.fromBits(list.removeFirst())
    override fun removeLast(): Double = Double.fromBits(list.removeLast())
    override fun removeAt(index: Int): Double = Double.fromBits(list.removeAt(index))
    override fun removeRange(fromIndex: Int, toIndex: Int) { list.removeRange(fromIndex, toIndex) }
    override fun clear() { list.clear() }
    override fun indexOf(element: Double): Int = list.indexOf(element.toBits())
    override fun lastIndexOf(element: Double): Int = list.lastIndexOf(element.toBits())

    public override fun removeAll(elements: Collection<Double>): Boolean = list.filterDoubleInPlace { e -> elements.contains(e) }
    public override fun retainAll(elements: Collection<Double>): Boolean = list.filterDoubleInPlace { e -> !elements.contains(e) }

    override fun sort() { list.sortDouble() }
    override fun sortDescending() { list.sortDescendingDouble() }
    override fun fill(element: Double) { list.fill(element.toBits()) }
    override fun reverse() { list.reverse() }

    override fun iterator(): InlineMutableDoubleIterator = InlineMutableDoubleIterator(list.iterator())
    override fun listIterator(): InlineMutableDoubleListIterator = InlineMutableDoubleListIterator(list.listIterator())
    override fun listIterator(index: Int): InlineMutableDoubleListIterator = InlineMutableDoubleListIterator(list.listIterator(index))
    override fun subList(fromIndex: Int, toIndex: Int): InlineDoubleList = InlineDoubleList(list.subList(fromIndex, toIndex))

    override fun toString(): String = list.toDoubleListString()
}

public class InlineMutableDoubleListIterator internal constructor(private val it: MutableLongListIterator) : MutableDoubleListIterator() {
    override fun hasNext(): Boolean = it.hasNext()
    override fun hasPrevious(): Boolean = it.hasPrevious()
    override fun previousDouble(): Double = Double.fromBits(it.previousLong())
    override fun nextDouble(): Double = Double.fromBits(it.nextLong())
    override fun nextIndex(): Int = it.nextIndex()
    override fun previousIndex(): Int = it.previousIndex()
    override fun remove() { it.remove() }
    override fun set(element: Double) { it.set(element.toBits()) }
    override fun add(element: Double) { it.add(element.toBits()) }
}

@OptIn(ExperimentalContracts::class)
private inline fun MutableLongList.filterDoubleInPlace(removePredicate: (Double) -> Boolean): Boolean {
    contract {
        callsInPlace(removePredicate, InvocationKind.UNKNOWN)
    }

    return filterInPlace { removePredicate(Double.fromBits(it)) }
}

private fun MutableLongList.sortDouble() {
    // after sorting by integer representation, the negative values will be in reverse order - reverse them
    sort()

    // check the sign of the raw bits rather than the Double value - the value comparison misses -0.0
    if (!isEmpty() && this[0] < 0) {
        // binary search for edge of negative values
        var left = 0
        var right = size
        while (left < right) {
            val mid = left + ((right - left) / 2)
            if (this[mid] < 0) left = mid + 1 else right = mid
        }

        right = left - 1
        left = 0
        while (left < right) {
            val tmp = this[left]
            this[left++] = this[right]
            this[right--] = tmp
        }
    }
}

private fun MutableLongList.sortDescendingDouble() {
    // after sorting by integer representation, the negative values will be in reverse order - reverse them
    sortDescending()

    // check the sign of the raw bits rather than the Double value - the value comparison misses -0.0
    if (!isEmpty() && this[size - 1] < 0) {
        // binary search for edge of negative values
        var left = 0
        var right = size
        while (left < right) {
            val mid = left + ((right - left) / 2)
            if (this[mid] < 0) right = mid else left = mid + 1
        }

        right = size - 1
        while (left < right) {
            val tmp = this[left]
            this[left++] = this[right]
            this[right--] = tmp
        }
    }
}

private fun LongList.toDoubleListString(): String = joinToString(", ", "[", "]") { Double.fromBits(it).toString() }

