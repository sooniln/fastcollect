/**
 * Methods for dealing with IntCollections.
 */
@file:JvmName("IntCollections")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
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
        foreach { e ->
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: IntCollection): Boolean {
        elements.foreach { element ->
            if (!contains(element)) {
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

    /**
     * Copies all of the elements of this collection into [destination], starting at [destinationOffset], and returns
     * [destination].
     */
    public fun copyInto(destination: IntArray, destinationOffset: Int = 0): IntArray {
        destination.rangeCheck(destinationOffset, destinationOffset + size)
        var index = destinationOffset
        foreach { element -> destination[index++] = element }
        return destination
    }
}

public fun IntCollection.toArray(): IntArray = copyInto(IntArray(size))

public fun IntCollection.isNotEmpty(): Boolean = size != 0

/**
 * A mutable collection of Ints.
 */
public interface MutableIntCollection : IntCollection, MutableIntTraversable {
    override fun iterator(): MutableIntIterator

    public fun add(element: Int): Boolean
    public fun remove(element: Int): Boolean

    public fun clear() {
        val traverser = traverser()
        while (traverser.forward()) {
            traverser.remove()
        }
    }

    public fun addAll(elements: IntCollection): Boolean {
        var modified = false
        elements.foreach { element ->
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Int>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
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
private inline fun MutableIntCollection.filterInPlace(removePredicate: (Int) -> Boolean): Boolean {
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

public abstract class AbstractIntCollection : IntCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}

public fun interface IntConsumer {
    public fun accept(value: Int)
}
