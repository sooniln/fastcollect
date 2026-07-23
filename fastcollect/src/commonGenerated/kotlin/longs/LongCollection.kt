@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.longs


import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * A collection of Longs which inherits from [Collection].
 */
public interface LongCollection : Collection<Long> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): LongIterator

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
    public fun foreach(action: LongConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            action.accept(it.next())
        }
    }

    override fun contains(element: Long): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
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

    override fun containsAll(elements: Collection<Long>): Boolean {
        if (elements is LongCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toLongArray(): LongArray {
        val array = LongArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.any(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.all(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.none(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> LongCollection.fold(initial: R, operation: (accumulated: R, Long) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    for (element in this) {
        accumulated = operation(accumulated, element)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun LongCollection.reduce(operation: (accumulated: Long, Long) -> Long): Long {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = iterator()
    var accumulated = it.nextLong()
    while (it.hasNext()) {
        accumulated = operation(accumulated, it.nextLong())
    }
    return accumulated
}

/**
 * A mutable collection of Longs which inherits from [MutableCollection].
 */
public interface MutableLongCollection : LongCollection, MutableCollection<Long> {
    override fun iterator(): MutableLongIterator

    override fun add(element: Long): Boolean
    override fun remove(element: Long): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextLong()
            it.remove()
        }
    }

    public fun addAll(elements: LongCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Long>): Boolean {
        if (elements is LongCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun removeAll(elements: LongCollection): Boolean = filterInPlace { elements.contains(it) }
    override fun removeAll(elements: Collection<Long>): Boolean {
        return if (elements is LongCollection) removeAll(elements) else filterInPlace { elements.contains(it) }
    }

    public fun retainAll(elements: LongCollection): Boolean = filterInPlace { !elements.contains(it) }
    override fun retainAll(elements: Collection<Long>): Boolean {
        return if (elements is LongCollection) retainAll(elements) else filterInPlace { !elements.contains(it) }
    }

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

@OptIn(ExperimentalContracts::class)
public inline fun MutableLongCollection.removeAll(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextLong())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableLongCollection.retainAll(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableLongCollection.filterInPlace(removePredicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextLong())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractLongCollection : LongCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}



public fun interface LongConsumer {
    public fun accept(value: Long)
}
