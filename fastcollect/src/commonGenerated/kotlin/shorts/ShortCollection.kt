@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.shorts


import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * A collection of Shorts which inherits from [Collection].
 */
public interface ShortCollection : Collection<Short> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): ShortIterator

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
    public fun foreach(action: ShortConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            action.accept(it.next())
        }
    }

    override fun contains(element: Short): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }

    public fun containsAll(elements: ShortCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Short>): Boolean {
        if (elements is ShortCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toShortArray(): ShortArray {
        val array = ShortArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun ShortCollection.any(predicate: (Short) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun ShortCollection.all(predicate: (Short) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
public inline fun ShortCollection.none(predicate: (Short) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> ShortCollection.fold(initial: R, operation: (accumulated: R, Short) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    for (element in this) {
        accumulated = operation(accumulated, element)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun ShortCollection.reduce(operation: (accumulated: Short, Short) -> Short): Short {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = iterator()
    var accumulated = it.nextShort()
    while (it.hasNext()) {
        accumulated = operation(accumulated, it.nextShort())
    }
    return accumulated
}

/**
 * A mutable collection of Shorts which inherits from [MutableCollection].
 */
public interface MutableShortCollection : ShortCollection, MutableCollection<Short> {
    override fun iterator(): MutableShortIterator

    override fun add(element: Short): Boolean
    override fun remove(element: Short): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextShort()
            it.remove()
        }
    }

    public fun addAll(elements: ShortCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Short>): Boolean {
        if (elements is ShortCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun removeAll(elements: ShortCollection): Boolean = filterInPlace { elements.contains(it) }
    override fun removeAll(elements: Collection<Short>): Boolean {
        return if (elements is ShortCollection) removeAll(elements) else filterInPlace { elements.contains(it) }
    }

    public fun retainAll(elements: ShortCollection): Boolean = filterInPlace { !elements.contains(it) }
    override fun retainAll(elements: Collection<Short>): Boolean {
        return if (elements is ShortCollection) retainAll(elements) else filterInPlace { !elements.contains(it) }
    }

    public operator fun plusAssign(element: Short) {
        add(element)
    }
    public operator fun plusAssign(elements: ShortCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Short>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Short) {
        remove(element)
    }
    public operator fun minusAssign(elements: ShortCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Short>) {
        removeAll(elements)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableShortCollection.removeAll(predicate: (Short) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextShort())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableShortCollection.retainAll(predicate: (Short) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableShortCollection.filterInPlace(removePredicate: (Short) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextShort())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractShortCollection : ShortCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}



public fun interface ShortConsumer {
    public fun accept(value: Short)
}
