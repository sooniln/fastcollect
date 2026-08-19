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
 * A collection of Longs.
 */
public interface LongCollection : LongTraversable {

    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): LongIterator

    public fun contains(element: Long): Boolean {
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

    public fun containsAll(elements: Collection<Long>): Boolean {
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

/**
 * A mutable collection of Longs.
 */
public interface MutableLongCollection : LongCollection {
    override fun iterator(): MutableLongIterator

    public fun add(element: Long): Boolean
    public fun remove(element: Long): Boolean

    public fun clear() {
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

    public fun addAll(elements: Collection<Long>): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
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
        return Iterable { iterator() }.joinToString(", ", "[", "]")
    }
}

public fun interface LongConsumer {
    public fun accept(value: Long)
}
