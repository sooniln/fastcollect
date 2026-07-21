package io.github.sooniln.fastcollect.floats


import io.github.sooniln.fastcollect.ints.MutableIntCollection
import io.github.sooniln.fastcollect.ints.MutableIntIterator

import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * A collection of Floats which inherits from [Collection].
 */
public interface FloatCollection : Collection<Float> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): FloatIterator

    override fun contains(element: Float): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }

    public fun containsAll(elements: FloatCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Float>): Boolean {
        if (elements is FloatCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toFloatArray(): FloatArray {
        val array = FloatArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.any(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.all(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.none(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> FloatCollection.fold(initial: R, operation: (accumulated: R, Float) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    for (element in this) {
        accumulated = operation(accumulated, element)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.reduce(operation: (accumulated: Float, Float) -> Float): Float {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = iterator()
    var accumulated = it.nextFloat()
    while (it.hasNext()) {
        accumulated = operation(accumulated, it.nextFloat())
    }
    return accumulated
}

/**
 * A mutable collection of Floats which inherits from [MutableCollection].
 */
public interface MutableFloatCollection : FloatCollection, MutableCollection<Float> {
    override fun iterator(): MutableFloatIterator

    override fun add(element: Float): Boolean
    override fun remove(element: Float): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextFloat()
            it.remove()
        }
    }

    public fun addAll(elements: FloatCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Float>): Boolean {
        if (elements is FloatCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun removeAll(elements: FloatCollection): Boolean = filterInPlace { elements.contains(it) }
    override fun removeAll(elements: Collection<Float>): Boolean {
        return if (elements is FloatCollection) removeAll(elements) else filterInPlace { elements.contains(it) }
    }

    public fun retainAll(elements: FloatCollection): Boolean = filterInPlace { !elements.contains(it) }
    override fun retainAll(elements: Collection<Float>): Boolean {
        return if (elements is FloatCollection) retainAll(elements) else filterInPlace { !elements.contains(it) }
    }

    public operator fun plusAssign(element: Float) {
        add(element)
    }
    public operator fun plusAssign(elements: FloatCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Float>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Float) {
        remove(element)
    }
    public operator fun minusAssign(elements: FloatCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Float>) {
        removeAll(elements)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableFloatCollection.removeAll(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextFloat())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableFloatCollection.retainAll(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableFloatCollection.filterInPlace(removePredicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextFloat())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractFloatCollection : FloatCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}



@Suppress("OVERRIDE_BY_INLINE")
@JvmInline
public value class InlineFloatCollection public constructor(@PublishedApi internal val collection: MutableIntCollection) : MutableFloatCollection {
    override val size: Int
        inline get() = collection.size
    override fun contains(element: Float): Boolean = collection.contains(element.toBits())
    override fun add(element: Float): Boolean = collection.add(element.toBits())
    override fun remove(element: Float): Boolean = collection.remove(element.toBits())
    override fun iterator(): InlineMutableFloatIterator = InlineMutableFloatIterator(collection.iterator())
}

public class InlineMutableFloatIterator public constructor(@PublishedApi internal val it: MutableIntIterator) : MutableFloatIterator() {
    override fun hasNext(): Boolean = it.hasNext()
    override fun nextFloat(): Float = Float.fromBits(it.nextInt())
    override fun remove() { it.remove() }
}


