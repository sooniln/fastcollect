/**
 * Methods for dealing with LongCollections.
 */
@file:JvmName("LongCollections")
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
 * A collection of Longs.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface LongCollection {

    @get:JvmName("size")
    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): LongIterator

    public fun contains(element: Long): Boolean {
        for (e in this) {
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: LongCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun containsAll(elements: Collection<Long>): Boolean {
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
    public fun copyInto(destination: LongArray, destinationOffset: Int = 0): LongArray {
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
    public fun toArray(): LongArray = copyInto(LongArray(size))
}

public fun LongCollection.isNotEmpty(): Boolean = size != 0

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.any(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.all(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.none(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.find(defaultValue: Long, predicate: (Long) -> Boolean): Long {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return element
    }
    return defaultValue
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> LongCollection.fold(initial: R, operation: (accumulator: R, Long) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    var accumulator = initial
    for (element in this) {
        accumulator = operation(accumulator, element)
    }
    return accumulator
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.reduce(operation: (accumulator: Long, Long) -> Long): Long {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    val iterator = this.iterator()
    var accumulator = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

@JvmSynthetic
public fun <A : Appendable> LongCollection.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", transform: ((Long) -> CharSequence)? = null): A {
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
public fun LongCollection.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = ""): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, null).toString()
}

/**
 * A mutable collection of Longs.
 */
public interface MutableLongCollection : LongCollection {
    override fun iterator(): MutableLongIterator

    public fun add(element: Long): Boolean
    public fun remove(element: Long): Boolean

    public fun clear() {
        val iterator = iterator()
        while (iterator.hasNext()) {
            iterator.nextLong()
            iterator.remove()
        }
    }

    public fun addAll(elements: LongCollection): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Long>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun removeAll(elements: LongCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Long>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: LongCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Long>): Boolean = filterInPlace { !elements.contains(it) }

    public operator fun plusAssign(element: Long) {
        add(element)
    }
    public operator fun plusAssign(elements: LongCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Long>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Long) {
        remove(element)
    }
    public operator fun minusAssign(elements: LongCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Long>) {
        removeAll(elements)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
internal inline fun MutableLongCollection.filterInPlace(removePredicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (removePredicate(iterator.nextLong())) {
            iterator.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractLongCollection : LongCollection {
    override fun toString(): String = joinToString(", ", "[", "]")
}
