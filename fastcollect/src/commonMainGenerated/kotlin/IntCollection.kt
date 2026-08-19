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
 * A collection of Ints.
 */
public interface IntCollection : IntTraversable {

    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): IntIterator

    public fun contains(element: Int): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
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

    public fun toIntArray(): IntArray {
        val array = IntArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

/**
 * A mutable collection of Ints.
 */
public interface MutableIntCollection : IntCollection {
    override fun iterator(): MutableIntIterator

    public fun add(element: Int): Boolean
    public fun remove(element: Int): Boolean

    public fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextInt()
            it.remove()
        }
    }

    public fun addAll(elements: IntCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Int>): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
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

@OptIn(ExperimentalContracts::class)
public inline fun MutableIntCollection.removeAll(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextInt())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableIntCollection.retainAll(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableIntCollection.filterInPlace(removePredicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextInt())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractIntCollection : IntCollection {
    override fun toString(): String {
        return Iterable { iterator() }.joinToString(", ", "[", "]")
    }
}

public fun interface IntConsumer {
    public fun accept(value: Int)
}
