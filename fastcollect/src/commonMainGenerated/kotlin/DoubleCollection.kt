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
import kotlin.jvm.JvmSynthetic

/**
 * A collection of Doubles.
 */
public interface DoubleCollection : DoubleTraversable {

    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): DoubleIterator

    public fun contains(element: Double): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }

    public fun containsAll(elements: DoubleCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun containsAll(elements: Collection<Double>): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toDoubleArray(): DoubleArray {
        val array = DoubleArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

/**
 * A mutable collection of Doubles.
 */
public interface MutableDoubleCollection : DoubleCollection {
    override fun iterator(): MutableDoubleIterator

    public fun add(element: Double): Boolean
    public fun remove(element: Double): Boolean

    public fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextDouble()
            it.remove()
        }
    }

    public fun addAll(elements: DoubleCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Double>): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun removeAll(elements: DoubleCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Double>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: DoubleCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Double>): Boolean = filterInPlace { !elements.contains(it) }

    public operator fun plusAssign(element: Double) {
        add(element)
    }
    public operator fun plusAssign(elements: DoubleCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Double>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Double) {
        remove(element)
    }
    public operator fun minusAssign(elements: DoubleCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Double>) {
        removeAll(elements)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableDoubleCollection.removeAll(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextDouble())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableDoubleCollection.retainAll(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableDoubleCollection.filterInPlace(removePredicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextDouble())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractDoubleCollection : DoubleCollection {
    override fun toString(): String {
        return Iterable { iterator() }.joinToString(", ", "[", "]")
    }
}

public fun interface DoubleConsumer {
    public fun accept(value: Double)
}
