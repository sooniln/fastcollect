/**
 * Methods for dealing with FloatCollections.
 */
@file:JvmName("FloatCollections")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmSynthetic

/**
 * A collection of Floats.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface FloatCollection {

    @get:JvmName("size")
    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): FloatIterator

    public fun contains(element: Float): Boolean {
        for (e in this) {
            if (e equalsRaw element) return true
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

    public fun containsAll(elements: Collection<Float>): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    /**
     * Copies all of the elements of this collection into [destination], starting at [destinationOffset], and returns
     * [destination]. Throws [IndexOutOfBoundsException] if the [destination] is not large enough for all elements.
     */
    public fun copyInto(destination: FloatArray, destinationOffset: Int = 0): FloatArray {
        destination.rangeCheck(destinationOffset, destinationOffset + size)
        var index = destinationOffset
        for (element in this) {
            destination[index++] = element
        }
        return destination
    }

    /**
     * Returns a new array containing all elements in this collection.
     */
    public fun toArray(): FloatArray = copyInto(FloatArray(size))
}

public fun FloatCollection.isNotEmpty(): Boolean = size != 0

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.any(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.all(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.none(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.find(defaultValue: Float, predicate: (Float) -> Boolean): Float {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return element
    }
    return defaultValue
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> FloatCollection.fold(initial: R, operation: (accumulator: R, Float) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    var accumulator = initial
    for (element in this) {
        accumulator = operation(accumulator, element)
    }
    return accumulator
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatCollection.reduce(operation: (accumulator: Float, Float) -> Float): Float {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    val iterator = this.iterator()
    var accumulator = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

@JvmSynthetic
public fun <A : Appendable> FloatCollection.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", transform: ((Float) -> CharSequence)? = null): A {
    buffer.append(prefix)
    var first = true
    for (element in this) {
        if (first) first = false else buffer.append(separator)
        buffer.append(if (transform == null) element.toString() else transform(element))
    }
    buffer.append(postfix)
    return buffer
}

@JvmOverloads
public fun FloatCollection.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = ""): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, null).toString()
}

/**
 * A mutable collection of Floats.
 */
public interface MutableFloatCollection : FloatCollection {
    override fun iterator(): MutableFloatIterator

    public fun add(element: Float): Boolean
    public fun remove(element: Float): Boolean

    public fun clear() {
        val iterator = iterator()
        while (iterator.hasNext()) {
            iterator.nextFloat()
            iterator.remove()
        }
    }

    public fun addAll(elements: FloatCollection): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Float>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun removeAll(elements: FloatCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Float>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: FloatCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Float>): Boolean = filterInPlace { !elements.contains(it) }

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

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
internal inline fun MutableFloatCollection.filterInPlace(removePredicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (removePredicate(iterator.nextFloat())) {
            iterator.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractFloatCollection : FloatCollection {
    override fun toString(): String = joinToString(", ", "[", "]")
}
