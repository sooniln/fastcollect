/**
 * Methods for dealing with IntSets.
 */
@file:JvmName("IntSets")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun emptyIntSet(): IntSet = EmptyIntSet

public fun intSetOf(): IntSet = EmptyIntSet
public fun intSetOf(element: Int): IntSet = SingletonIntSet(element)
public fun intSetOf(vararg elements: Int): IntSet = IntHashSet(elements.asIntList())

public fun mutableIntSetOf(): MutableIntSet = IntHashSet()
public fun mutableIntSetOf(element: Int): MutableIntSet = IntHashSet(1).apply { add(element) }
public fun mutableIntSetOf(vararg elements: Int): MutableIntSet = IntHashSet(elements.asIntList())

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun buildIntSet(expectedSize: Int = 0, builderAction: MutableIntSet.() -> Unit): IntSet {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val set = IntHashSet(expectedSize)
    set.builderAction()
    return set
}

/**
 * A set of Ints.
 */
public interface IntSet : IntCollection

public fun IntSet.asSet(): Set<Int> = IntSetWrapper(this)

public infix fun IntSet.union(other: IntSet): IntSet = IntHashSet(this).apply { addAll(other) }

public infix fun IntSet.intersect(other: IntSet): IntSet {
    if (other.size > size) return other.intersect(this)

    val set = IntHashSet(other.size)
    other.foreach { element ->
        if (contains(element)) set.add(element)
    }
    return set
}

public infix fun IntSet.subtract(other: IntSet): IntSet {
    val set = IntHashSet(size)
    foreach { element ->
        if (!other.contains(element)) set.add(element)
    }
    return set
}

/**
 * A mutable set of Ints.
 */
public interface MutableIntSet : IntSet, MutableIntCollection

public abstract class AbstractIntSet : AbstractIntCollection(), IntSet {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is IntSet) {
            if (size != other.size) return false
            return other.containsAll(this)
        }

        return false
    }

    override fun hashCode(): Int {
        var hashCode = 0
        foreach { element ->
            hashCode += element.hashCode()
        }
        return hashCode
    }
}

public abstract class AbstractMutableIntSet : AbstractIntSet(), MutableIntSet

public fun MutableIntSet.asSet(): MutableSet<Int> = MutableIntSetWrapper(this)

private object EmptyIntSet : AbstractIntSet() {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Int): Boolean = false
    override fun containsAll(elements: Collection<Int>): Boolean = elements.isEmpty()
    override fun containsAll(elements: IntCollection): Boolean = elements.isEmpty()

    override fun iterator(): IntIterator = emptyIntIterator()
    override fun traverser(): IntTraverser = emptyIntTraverser()
}

private class SingletonIntSet(private val value: Int) : AbstractIntSet() {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Int): Boolean = value equalsRaw element

    override fun iterator(): IntIterator = object : IntIterator() {
        private var complete = false
        override fun hasNext(): Boolean = !complete
        override fun nextInt(): Int {
            if (complete) throw NoSuchElementException()
            complete = true
            return value
        }
    }

    override fun traverser(): IntTraverser = object : IntTraverser {
        private var complete = false
        override val value: Int get() {
            check(complete)
            return this@SingletonIntSet.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class IntSetWrapper(private val set: IntSet) : AbstractSet<Int>() {
    override val size: Int get() = set.size
    override fun contains(element: Int) = set.contains(element)
    override fun iterator() = set.iterator()
}

private class MutableIntSetWrapper(private val set: MutableIntSet) : AbstractMutableSet<Int>() {
    override val size: Int get() = set.size
    override fun contains(element: Int) = set.contains(element)
    override fun containsAll(elements: Collection<Int>): Boolean = set.containsAll(elements)
    override fun iterator() = set.iterator()

    override fun add(element: Int) = set.add(element)
    override fun remove(element: Int): Boolean = set.remove(element)
    override fun clear() = set.clear()

    override fun addAll(elements: Collection<Int>): Boolean = set.addAll(elements)
    override fun removeAll(elements: Collection<Int>): Boolean = set.removeAll(elements)
    override fun retainAll(elements: Collection<Int>): Boolean = set.retainAll(elements)
}
