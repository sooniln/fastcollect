/**
 * Methods for dealing with IntCollections.
 */
@file:JvmName("IntCollections")
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
 * A collection of Ints.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface IntCollection {

    @get:JvmName("size")
    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): IntIterator

    public fun contains(element: Int): Boolean {
        for (e in this) {
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: IntCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun containsAll(elements: Collection<Int>): Boolean {
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
    public fun copyInto(destination: IntArray, destinationOffset: Int = 0): IntArray {
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
    public fun toArray(): IntArray = copyInto(IntArray(size))
}

public fun IntCollection.isNotEmpty(): Boolean = size != 0

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.any(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.all(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.none(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.find(defaultValue: Int, predicate: (Int) -> Boolean): Int {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return element
    }
    return defaultValue
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> IntCollection.fold(initial: R, operation: (accumulator: R, Int) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    var accumulator = initial
    for (element in this) {
        accumulator = operation(accumulator, element)
    }
    return accumulator
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.reduce(operation: (accumulator: Int, Int) -> Int): Int {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    val iterator = this.iterator()
    var accumulator = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

@JvmSynthetic
public fun <A : Appendable> IntCollection.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", transform: ((Int) -> CharSequence)? = null): A {
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
public fun IntCollection.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = ""): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, null).toString()
}

/**
 * A mutable collection of Ints.
 */
public interface MutableIntCollection : IntCollection {
    override fun iterator(): MutableIntIterator

    public fun add(element: Int): Boolean
    public fun remove(element: Int): Boolean

    public fun clear() {
        val iterator = iterator()
        while (iterator.hasNext()) {
            iterator.nextInt()
            iterator.remove()
        }
    }

    public fun addAll(elements: IntCollection): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Int>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun removeAll(elements: IntCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Int>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: IntCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Int>): Boolean = filterInPlace { !elements.contains(it) }

    public operator fun plusAssign(element: Int) {
        add(element)
    }
    public operator fun plusAssign(elements: IntCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Int>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Int) {
        remove(element)
    }
    public operator fun minusAssign(elements: IntCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Int>) {
        removeAll(elements)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
internal inline fun MutableIntCollection.filterInPlace(removePredicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (removePredicate(iterator.nextInt())) {
            iterator.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractIntCollection : IntCollection {
    override fun toString(): String = joinToString(", ", "[", "]")
}
