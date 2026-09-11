/**
 * Methods for dealing with DoubleCollections.
 */
@file:JvmName("DoubleCollections")
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
 * A collection of Doubles.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface DoubleCollection {

    @get:JvmName("size")
    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): DoubleIterator

    public fun contains(element: Double): Boolean {
        for (e in this) {
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: DoubleCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun containsAll(elements: Collection<Double>): Boolean {
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
    public fun copyInto(destination: DoubleArray, destinationOffset: Int = 0): DoubleArray {
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
    public fun toArray(): DoubleArray = copyInto(DoubleArray(size))
}

public fun DoubleCollection.isNotEmpty(): Boolean = size != 0

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.any(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.all(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.none(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.find(defaultValue: Double, predicate: (Double) -> Boolean): Double {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return element
    }
    return defaultValue
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> DoubleCollection.fold(initial: R, operation: (accumulator: R, Double) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    var accumulator = initial
    for (element in this) {
        accumulator = operation(accumulator, element)
    }
    return accumulator
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.reduce(operation: (accumulator: Double, Double) -> Double): Double {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    val iterator = this.iterator()
    var accumulator = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

@JvmSynthetic
public fun <A : Appendable> DoubleCollection.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", transform: ((Double) -> CharSequence)? = null): A {
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
public fun DoubleCollection.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = ""): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, null).toString()
}

/**
 * A mutable collection of Doubles.
 */
public interface MutableDoubleCollection : DoubleCollection {
    override fun iterator(): MutableDoubleIterator

    public fun add(element: Double): Boolean
    public fun remove(element: Double): Boolean

    public fun clear() {
        val iterator = iterator()
        while (iterator.hasNext()) {
            iterator.nextDouble()
            iterator.remove()
        }
    }

    public fun addAll(elements: DoubleCollection): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Double>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun removeAll(elements: DoubleCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Double>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: DoubleCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Double>): Boolean = filterInPlace { !elements.contains(it) }

    public operator fun plusAssign(element: Double) {
        add(element)
    }
    public operator fun plusAssign(elements: DoubleCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Double>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Double) {
        remove(element)
    }
    public operator fun minusAssign(elements: DoubleCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Double>) {
        removeAll(elements)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
internal inline fun MutableDoubleCollection.filterInPlace(removePredicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (removePredicate(iterator.nextDouble())) {
            iterator.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractDoubleCollection : DoubleCollection {
    override fun toString(): String = joinToString(", ", "[", "]")
}
