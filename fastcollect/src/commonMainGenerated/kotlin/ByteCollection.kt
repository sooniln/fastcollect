/**
 * Methods for dealing with ByteCollections.
 */
@file:JvmName("ByteCollections")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

/**
 * A collection of Bytes.
 */
public interface ByteCollection : ByteTraversable {

    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): ByteIterator

    public fun contains(element: Byte): Boolean {
        foreach { e ->
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: ByteCollection): Boolean {
        elements.foreach { element ->
            if (!contains(element)) {
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

    public fun toByteArray(): ByteArray {
        val array = ByteArray(size)
        var index = 0
        foreach { element ->
            array[index++] = element
        }
        return array
    }
}

/**
 * A mutable collection of Bytes.
 */
public interface MutableByteCollection : ByteCollection, MutableByteTraversable {
    override fun iterator(): MutableByteIterator

    public fun add(element: Byte): Boolean
    public fun remove(element: Byte): Boolean

    public fun clear() {
        val traverser = traverser()
        while (traverser.forward()) {
            traverser.remove()
        }
    }

    public fun addAll(elements: ByteCollection): Boolean {
        var modified = false
        elements.foreach { element ->
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

@OptIn(ExperimentalContracts::class)
public inline fun MutableByteCollection.removeAll(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val traverser = traverser()
    while (traverser.forward()) {
        if (predicate(traverser.value)) {
            traverser.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableByteCollection.retainAll(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableByteCollection.filterInPlace(removePredicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val traverser = traverser()
    while (traverser.forward()) {
        if (removePredicate(traverser.value)) {
            traverser.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractByteCollection : ByteCollection {
    override fun toString(): String {
        return Iterable { iterator() }.joinToString(", ", "[", "]")
    }
}

public fun interface ByteConsumer {
    public fun accept(value: Byte)
}
