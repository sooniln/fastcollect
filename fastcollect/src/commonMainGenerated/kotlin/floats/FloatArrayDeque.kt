package io.github.sooniln.fastcollect.floats

import io.github.sooniln.fastcollect.ints.IntArrayDeque
import io.github.sooniln.fastcollect.ints.IntList
import io.github.sooniln.fastcollect.ints.MutableIntList
import io.github.sooniln.fastcollect.ints.MutableIntListIterator
import io.github.sooniln.fastcollect.ints.filterInPlace
import kotlin.jvm.JvmInline

public typealias FloatArrayDeque = InlineFloatArrayDeque
public typealias FloatArrayList = FloatArrayDeque

@Suppress("OVERRIDE_BY_INLINE")
@JvmInline
public value class InlineFloatArrayDeque private constructor(@PublishedApi internal val list: IntArrayDeque) : MutableFloatList {

    public constructor(capacity: Int = 0) : this(IntArrayDeque(capacity))
    public constructor(elements: FloatCollection) : this() { addAll(elements) }
    public constructor(elements: Collection<Float>) : this() { addAll(elements) }
    public constructor(elements: FloatArray, fromIndex:Int = 0, toIndex: Int = elements.size) : this(toIndex - fromIndex) { for (i in fromIndex..<toIndex) add(elements[i]) }

    override val size: Int
        inline get() = list.size

    public fun ensureCapacity(capacity: Int) { list.ensureCapacity(capacity) }
    public fun trimToSize() { list.trimToSize() }

    override fun get(index: Int): Float = Float.fromBits(list[index])
    override fun set(index: Int, element: Float) { list[index] = element.toBits() }
    override fun addFirst(element: Float) { list.addFirst(element.toBits()) }
    override fun addLast(element: Float) { list.addLast(element.toBits()) }
    override fun add(index: Int, element: Float) { list.add(index, element.toBits()) }
    override fun removeFirst(): Float = Float.fromBits(list.removeFirst())
    override fun removeLast(): Float = Float.fromBits(list.removeLast())
    override fun removeAt(index: Int): Float = Float.fromBits(list.removeAt(index))
    override fun removeRange(fromIndex: Int, toIndex: Int) { list.removeRange(fromIndex, toIndex) }
    override fun clear() { list.clear() }
    override fun indexOf(element: Float): Int = list.indexOf(element.toBits())
    override fun lastIndexOf(element: Float): Int = list.lastIndexOf(element.toBits())

    override fun addAll(elements: FloatCollection): Boolean {
        if (elements is InlineFloatArrayDeque) return list.addAll(elements.list)

        ensureCapacity(size + elements.size)
        for (element in elements) list.addLast(element.toBits())
        return !elements.isEmpty()
    }
    override fun addAll(elements: Collection<Float>): Boolean {
        if (elements is FloatCollection) return addAll(elements)

        ensureCapacity(size + elements.size)
        for (element in elements) list.addLast(element.toBits())
        return !elements.isEmpty()
    }
    override fun addAll(index: Int, elements: FloatCollection) {
        if (elements is InlineFloatArrayDeque) {
            list.addAll(index, elements.list)
        } else {
            ensureCapacity(size + elements.size)
            var i = index
            for (element in elements) list.add(i++, element.toBits())
        }
    }
    override fun addAll(index: Int, elements: Collection<Float>) {
        if (elements is FloatCollection) {
            addAll(index, elements)
        } else {
            ensureCapacity(size + elements.size)
            var i = index
            for (element in elements) list.add(i++, element.toBits())
        }
    }

    public override fun removeAll(elements: Collection<Float>): Boolean = list.filterInPlace { elements.contains(Float.fromBits(it)) }
    public override fun retainAll(elements: Collection<Float>): Boolean = list.filterInPlace { !elements.contains(Float.fromBits(it)) }

    override fun sort() { list.sortFloat() }
    override fun sortDescending() { list.sortDescendingFloat() }
    override fun fill(element: Float) { list.fill(element.toBits()) }
    override fun reverse() { list.reverse() }

    override fun iterator(): InlineMutableFloatIterator = InlineMutableFloatIterator(list.iterator())
    override fun listIterator(): InlineMutableFloatListIterator = InlineMutableFloatListIterator(list.listIterator())
    override fun listIterator(index: Int): InlineMutableFloatListIterator = InlineMutableFloatListIterator(list.listIterator(index))
    override fun subList(fromIndex: Int, toIndex: Int): InlineFloatList = InlineFloatList(list.subList(fromIndex, toIndex))

    override fun foreach(action: FloatConsumer) {
        list.foreach { element -> action.accept(Float.fromBits(element))}
    }

    override fun toString(): String = list.toFloatListString()
}

public fun InlineFloatArrayDeque.removeAll(predicate: FloatPredicate): Boolean = list.removeAll { predicate.test(Float.fromBits(it)) }
public fun InlineFloatArrayDeque.retainAll(predicate: FloatPredicate): Boolean = list.retainAll { predicate.test(Float.fromBits(it)) }

@Suppress("OVERRIDE_BY_INLINE")
@JvmInline
public value class InlineFloatList internal constructor(@PublishedApi internal val list: MutableIntList) : MutableFloatList {

    override val size: Int
        inline get() = list.size

    override fun get(index: Int): Float = Float.fromBits(list[index])
    override fun set(index: Int, element: Float) { list[index] = element.toBits() }
    override fun addFirst(element: Float) { list.addFirst(element.toBits()) }
    override fun addLast(element: Float) { list.addLast(element.toBits()) }
    override fun add(index: Int, element: Float) { list.add(index, element.toBits()) }
    override fun removeFirst(): Float = Float.fromBits(list.removeFirst())
    override fun removeLast(): Float = Float.fromBits(list.removeLast())
    override fun removeAt(index: Int): Float = Float.fromBits(list.removeAt(index))
    override fun removeRange(fromIndex: Int, toIndex: Int) { list.removeRange(fromIndex, toIndex) }
    override fun clear() { list.clear() }
    override fun indexOf(element: Float): Int = list.indexOf(element.toBits())
    override fun lastIndexOf(element: Float): Int = list.lastIndexOf(element.toBits())

    public override fun removeAll(elements: Collection<Float>): Boolean = list.filterInPlace { elements.contains(Float.fromBits(it)) }
    public override fun retainAll(elements: Collection<Float>): Boolean = list.filterInPlace { !elements.contains(Float.fromBits(it)) }

    override fun sort() { list.sortFloat() }
    override fun sortDescending() { list.sortDescendingFloat() }
    override fun fill(element: Float) { list.fill(element.toBits()) }
    override fun reverse() { list.reverse() }

    override fun iterator(): InlineMutableFloatIterator = InlineMutableFloatIterator(list.iterator())
    override fun listIterator(): InlineMutableFloatListIterator = InlineMutableFloatListIterator(list.listIterator())
    override fun listIterator(index: Int): InlineMutableFloatListIterator = InlineMutableFloatListIterator(list.listIterator(index))
    override fun subList(fromIndex: Int, toIndex: Int): InlineFloatList = InlineFloatList(list.subList(fromIndex, toIndex))

    override fun toString(): String = list.toFloatListString()
}

public class InlineMutableFloatListIterator internal constructor(private val it: MutableIntListIterator) : MutableFloatListIterator() {
    override fun hasNext(): Boolean = it.hasNext()
    override fun hasPrevious(): Boolean = it.hasPrevious()
    override fun previousFloat(): Float = Float.fromBits(it.previousInt())
    override fun nextFloat(): Float = Float.fromBits(it.nextInt())
    override fun nextIndex(): Int = it.nextIndex()
    override fun previousIndex(): Int = it.previousIndex()
    override fun remove() { it.remove() }
    override fun set(element: Float) { it.set(element.toBits()) }
    override fun add(element: Float) { it.add(element.toBits()) }
}

private fun MutableIntList.sortFloat() {
    // after sorting by integer representation, the negative values will be in reverse order - reverse them
    sort()

    // check the sign of the raw bits rather than the Float value - the value comparison misses -0.0
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

private fun MutableIntList.sortDescendingFloat() {
    // after sorting by integer representation, the negative values will be in reverse order - reverse them
    sortDescending()

    // check the sign of the raw bits rather than the Float value - the value comparison misses -0.0
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

private fun IntList.toFloatListString(): String = joinToString(", ", "[", "]") { Float.fromBits(it).toString() }

