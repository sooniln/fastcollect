/**
 * Methods for dealing with DoubleCollections.
 */
@file:JvmName("DoubleCollections")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

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
        foreach { e ->
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: DoubleCollection): Boolean {
        elements.foreach { element ->
            if (!contains(element)) {
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
        foreach { element ->
            array[index++] = element
        }
        return array
    }
}

/**
 * A mutable collection of Doubles.
 */
public interface MutableDoubleCollection : DoubleCollection, MutableDoubleTraversable {
    override fun iterator(): MutableDoubleIterator

    public fun add(element: Double): Boolean
    public fun remove(element: Double): Boolean

    public fun clear() {
        val traverser = traverser()
        while (traverser.forward()) {
            traverser.remove()
        }
    }

    public fun addAll(elements: DoubleCollection): Boolean {
        var modified = false
        elements.foreach { element ->
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Double>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
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
internal inline fun MutableDoubleCollection.filterInPlace(removePredicate: (Double) -> Boolean): Boolean {
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

public abstract class AbstractDoubleCollection : DoubleCollection {
    override fun toString(): String {
        return Iterable { iterator() }.joinToString(", ", "[", "]")
    }
}

public fun interface DoubleConsumer {
    public fun accept(value: Double)
}
