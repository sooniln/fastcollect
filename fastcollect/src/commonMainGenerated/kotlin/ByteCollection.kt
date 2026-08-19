/**
 * Methods for dealing with primitive Collections.
 */
@file:JvmName("Collections")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
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
        for (e in this) {
            if (e equalsBoxed element) return true
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

    public fun toByteArray(): ByteArray {
        val array = ByteArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

/**
 * A mutable collection of Bytes.
 */
public interface MutableByteCollection : ByteCollection {
    override fun iterator(): MutableByteIterator

    public fun add(element: Byte): Boolean
    public fun remove(element: Byte): Boolean

    public fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextByte()
            it.remove()
        }
    }

    public fun addAll(elements: ByteCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Byte>): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
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
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextByte())) {
            it.remove()
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
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextByte())) {
            it.remove()
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
