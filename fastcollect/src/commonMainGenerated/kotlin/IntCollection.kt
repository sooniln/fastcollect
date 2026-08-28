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

    public fun toIntArray(): IntArray {
        val array = IntArray(size)
        var index = 0
        foreach { element ->
            array[index++] = element
        }
        return array
    }
}

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
internal inline fun MutableIntCollection.filterInPlace(removePredicate: (Int) -> Boolean): Boolean {
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
        return Iterable { iterator() }.joinToString(", ", "[", "]")
    }
}

public fun interface IntConsumer {
    public fun accept(value: Int)
}
