/**
 * Methods for dealing with LongCollections.
 */
@file:JvmName("LongCollections")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
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
        foreach { e ->
            if (e equalsRaw element) return true
        }
        return false
    }

    public fun containsAll(elements: LongCollection): Boolean {
        elements.foreach { element ->
            if (!contains(element)) {
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

    /**
     * Copies all of the elements of this collection into [destination], starting at [destinationOffset], and returns
     * [destination].
     */
    public fun copyInto(destination: LongArray, destinationOffset: Int = 0): LongArray {
        destination.rangeCheck(destinationOffset, destinationOffset + size)
        var index = destinationOffset
        foreach { element -> destination[index++] = element }
        return destination
    }
}

public fun LongCollection.toArray(): LongArray = copyInto(LongArray(size))

public fun LongCollection.isNotEmpty(): Boolean = size != 0

/**
 * A mutable collection of Longs.
 */
public interface MutableLongCollection : LongCollection, MutableLongTraversable {
    override fun iterator(): MutableLongIterator

    public fun add(element: Long): Boolean
    public fun remove(element: Long): Boolean

    public fun clear() {
        val traverser = traverser()
        while (traverser.forward()) {
            traverser.remove()
        }
    }

    public fun addAll(elements: LongCollection): Boolean {
        var modified = false
        elements.foreach { element ->
            modified = add(element) or modified
        }
        return modified
    }

    public fun addAll(elements: Collection<Long>): Boolean {
        var modified = false
        for (element in elements) {
            modified = add(element) or modified
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
private inline fun MutableLongCollection.filterInPlace(removePredicate: (Long) -> Boolean): Boolean {
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

public abstract class AbstractLongCollection : LongCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}

public fun interface LongConsumer {
    public fun accept(value: Long)
}
