/**
 * Methods for dealing with FloatCollections.
 */
@file:JvmName("FloatCollections")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName

/**
 * A collection of Floats.
 */
public interface FloatCollection : FloatTraversable {

    public val size: Int

    public fun isEmpty(): Boolean {
        return size == 0
    }

    public operator fun iterator(): FloatIterator

    public fun contains(element: Float): Boolean {
        foreach { e ->
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: FloatCollection): Boolean {
        elements.foreach { element ->
            if (!contains(element)) {
                return false
            }
        }
        return true
    }

    public fun containsAll(elements: Collection<Float>): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    /**
     * Copies all of the elements of this collection into [destination], starting at [destinationOffset], and returns
     * [destination].
     */
    public fun copyInto(destination: FloatArray, destinationOffset: Int = 0): FloatArray {
        destination.rangeCheck(destinationOffset, destinationOffset + size)
        var index = destinationOffset
        foreach { element -> destination[index++] = element }
        return destination
    }
}

public fun FloatCollection.toArray(): FloatArray = copyInto(FloatArray(size))

public fun FloatCollection.isNotEmpty(): Boolean = size != 0

/**
 * A mutable collection of Floats.
 */
public interface MutableFloatCollection : FloatCollection, MutableFloatTraversable {
    override fun iterator(): MutableFloatIterator

    public fun add(element: Float): Boolean
    public fun remove(element: Float): Boolean

    public fun clear() {
        val traverser = traverser()
        while (traverser.forward()) {
            traverser.remove()
        }
    }

    public fun addAll(elements: FloatCollection): Boolean {
        var modified = false
        elements.foreach { element ->
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Float>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
        }
        return modified
    }

    public fun removeAll(elements: FloatCollection): Boolean = filterInPlace { elements.contains(it) }
    public fun removeAll(elements: Collection<Float>): Boolean = filterInPlace { elements.contains(it) }

    public fun retainAll(elements: FloatCollection): Boolean = filterInPlace { !elements.contains(it) }
    public fun retainAll(elements: Collection<Float>): Boolean = filterInPlace { !elements.contains(it) }

    public operator fun plusAssign(element: Float) {
        add(element)
    }
    public operator fun plusAssign(elements: FloatCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Float>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Float) {
        remove(element)
    }
    public operator fun minusAssign(elements: FloatCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Float>) {
        removeAll(elements)
    }
}

@OptIn(ExperimentalContracts::class)
private inline fun MutableFloatCollection.filterInPlace(removePredicate: (Float) -> Boolean): Boolean {
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

public abstract class AbstractFloatCollection : FloatCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}

public fun interface FloatConsumer {
    public fun accept(value: Float)
}
