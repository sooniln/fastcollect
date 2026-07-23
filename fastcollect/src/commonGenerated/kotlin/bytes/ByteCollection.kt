@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.bytes


import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * A collection of Bytes which inherits from [Collection].
 */
public interface ByteCollection : Collection<Byte> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): ByteIterator

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
    public fun foreach(action: ByteConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            action.accept(it.next())
        }
    }

    override fun contains(element: Byte): Boolean {
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

    override fun containsAll(elements: Collection<Byte>): Boolean {
        if (elements is ByteCollection) {
            return containsAll(elements)
        }

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

@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.any(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.all(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.none(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> ByteCollection.fold(initial: R, operation: (accumulated: R, Byte) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    for (element in this) {
        accumulated = operation(accumulated, element)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun ByteCollection.reduce(operation: (accumulated: Byte, Byte) -> Byte): Byte {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = iterator()
    var accumulated = it.nextByte()
    while (it.hasNext()) {
        accumulated = operation(accumulated, it.nextByte())
    }
    return accumulated
}

/**
 * A mutable collection of Bytes which inherits from [MutableCollection].
 */
public interface MutableByteCollection : ByteCollection, MutableCollection<Byte> {
    override fun iterator(): MutableByteIterator

    override fun add(element: Byte): Boolean
    override fun remove(element: Byte): Boolean

    override fun clear() {
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

    override fun addAll(elements: Collection<Byte>): Boolean {
        if (elements is ByteCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun removeAll(elements: ByteCollection): Boolean = filterInPlace { elements.contains(it) }
    override fun removeAll(elements: Collection<Byte>): Boolean {
        return if (elements is ByteCollection) removeAll(elements) else filterInPlace { elements.contains(it) }
    }

    public fun retainAll(elements: ByteCollection): Boolean = filterInPlace { !elements.contains(it) }
    override fun retainAll(elements: Collection<Byte>): Boolean {
        return if (elements is ByteCollection) retainAll(elements) else filterInPlace { !elements.contains(it) }
    }

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
        return joinToString(", ", "[", "]")
    }
}



public fun interface ByteConsumer {
    public fun accept(value: Byte)
}
