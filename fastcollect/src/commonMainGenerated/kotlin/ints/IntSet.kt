package io.github.sooniln.fastcollect.ints

import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

public fun emptyIntSet(): IntSet = EmptyIntSet

public fun intSetOf(): IntSet = EmptyIntSet
public fun intSetOf(element: Int): IntSet = SingletonIntSet(element)
public fun intSetOf(vararg elements: Int): IntSet = IntHashSet(elements.asIntList())

public fun mutableIntSetOf(): MutableIntSet = IntHashSet()
public fun mutableIntSetOf(element: Int): MutableIntSet = IntHashSet(1).apply { add(element) }
public fun mutableIntSetOf(vararg elements: Int): MutableIntSet = IntHashSet(elements.asIntList())

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildIntSet(expectedSize: Int = 0, builderAction: MutableIntSet.() -> Unit): IntSet {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val set = IntHashSet(expectedSize)
    set.builderAction()
    return set
}

/**
 * A set of Ints.
 */
public interface IntSet : IntCollection {
    override fun contains(element: Int): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }
}

public fun IntSet.asSet(): Set<Int> = IntSetWrapper(this)

public infix fun IntSet.union(other: IntSet): IntSet = IntHashSet(this).apply { addAll(other) }

public infix fun IntSet.intersect(other: IntSet): IntSet {
    if (other.size < size) return other.intersect(this)

    val set = IntHashSet(size)
    for (element in other) {
        if (contains(element)) set.add(element)
    }
    return set
}

public infix fun IntSet.subtract(other: IntSet): IntSet {
    val set = IntHashSet(size)
    for (element in this) {
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
        for (element in this) {
            hashCode += element.hashCode()
        }
        return hashCode
    }
}

public abstract class AbstractMutableIntSet : AbstractIntSet(), MutableIntSet

public fun MutableIntSet.asMutableSet(): MutableSet<Int> = MutableIntSetWrapper(this)

private object EmptyIntSet : IntSet {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Int): Boolean = false
    override fun containsAll(elements: Collection<Int>): Boolean = elements.isEmpty()
    override fun containsAll(elements: IntCollection): Boolean = elements.isEmpty()

    override fun iterator(): IntIterator = emptyIntIterator()
}

private class SingletonIntSet(private val value: Int) : IntSet {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Int): Boolean = value equalsBoxed element

    override fun iterator(): IntIterator = object : IntIterator() {
        private var complete = false

        override fun nextInt(): Int {
            if (complete) throw NoSuchElementException()
            complete = true
            return value
        }

        override fun hasNext(): Boolean = !complete
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
    override fun iterator() = set.iterator()

    override fun add(element: Int) = set.add(element)
    override fun remove(element: Int): Boolean = set.remove(element)
    override fun clear() = set.clear()

    override fun addAll(elements: Collection<Int>): Boolean = set.addAll(elements)
}
