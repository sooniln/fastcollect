/**
 * Methods for dealing with ByteCollections.
 */
@file:JvmName("ByteCollections")
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
 * A collection of Bytes.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface ByteCollection {

    @get:JvmName("size")
    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): ByteIterator

    public fun contains(element: Byte): Boolean {
        for (e in this) {
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: ByteCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun containsAll(elements: Collection<Byte>): Boolean {
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
    public fun copyInto(destination: ByteArray, destinationOffset: Int = 0): ByteArray {
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
    public fun toArray(): ByteArray = copyInto(ByteArray(size))
}

public fun ByteCollection.isNotEmpty(): Boolean = size != 0

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.any(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.all(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.none(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.find(defaultValue: Byte, predicate: (Byte) -> Boolean): Byte {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    for (element in this) {
        if (predicate(element)) return element
    }
    return defaultValue
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> ByteCollection.fold(initial: R, operation: (accumulator: R, Byte) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    var accumulator = initial
    for (element in this) {
        accumulator = operation(accumulator, element)
    }
    return accumulator
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.reduce(operation: (accumulator: Byte, Byte) -> Byte): Byte {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }
    val iterator = this.iterator()
    var accumulator = iterator.next()
    while (iterator.hasNext()) {
        accumulator = operation(accumulator, iterator.next())
    }
    return accumulator
}

@JvmSynthetic
public fun <A : Appendable> ByteCollection.joinTo(buffer: A, separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = "", transform: ((Byte) -> CharSequence)? = null): A {
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
public fun ByteCollection.joinToString(separator: CharSequence = ", ", prefix: CharSequence = "", postfix: CharSequence = ""): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, null).toString()
}

/**
 * A mutable collection of Bytes.
 */
public interface MutableByteCollection : ByteCollection {
    override fun iterator(): MutableByteIterator

    public fun add(element: Byte): Boolean
    public fun remove(element: Byte): Boolean

    public fun clear() {
        val iterator = iterator()
        while (iterator.hasNext()) {
            iterator.nextByte()
            iterator.remove()
        }
    }

    public fun addAll(elements: ByteCollection): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Byte>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun removeAll(elements: ByteCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Byte>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: ByteCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Byte>): Boolean = filterInPlace { !elements.contains(it) }

    public operator fun plusAssign(element: Byte) {
        add(element)
    }
    public operator fun plusAssign(elements: ByteCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Byte>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Byte) {
        remove(element)
    }
    public operator fun minusAssign(elements: ByteCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Byte>) {
        removeAll(elements)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
internal inline fun MutableByteCollection.filterInPlace(removePredicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (removePredicate(iterator.nextByte())) {
            iterator.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractByteCollection : ByteCollection {
    override fun toString(): String = joinToString(", ", "[", "]")
}
