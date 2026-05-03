package io.github.sooniln.fastcollect.ints

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
 * A set of Ints which inherits from [Set].
 */
public interface IntSet : Set<Int>, IntCollection {
    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Int): Boolean {
        for (e in this) {
            if (e == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<Int>): Boolean = super.containsAll(elements)

    public infix fun union(other: IntSet): IntSet = IntHashSet(this).apply { addAll(other) }

    public infix fun intersect(other: IntSet): IntSet {
        if (other.size < size) return other.intersect(this)

        val set = IntHashSet(size)
        for (element in other) {
            if (contains(element)) set.add(element)
        }
        return set
    }

    public infix fun subtract(other: IntSet): IntSet {
        if (other.size < size) return other.intersect(this)

        val set = IntHashSet(size)
        for (element in other) {
            if (!contains(element)) set.add(element)
        }
        return set
    }
}

/**
 * A mutable set of Ints which inherits from [MutableSet].
 */
public interface MutableIntSet : IntSet, MutableIntCollection, MutableSet<Int> {
    override fun clear(): Unit = super.clear()
    override fun addAll(elements: Collection<Int>): Boolean = super.addAll(elements)
    override fun removeAll(elements: Collection<Int>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Int>): Boolean = super.retainAll(elements)
}

public abstract class AbstractIntSet : AbstractIntCollection(), IntSet {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Set<*>) {
            if (size != other.size) return false
            return other.containsAll(this)
        }

        return false
    }

    override fun hashCode(): Int {
        var hashCode = 1
        for (element in this) {
            hashCode = 31 * hashCode + element.hashCode()
        }
        return hashCode
    }
}

public abstract class AbstractMutableIntSet : AbstractIntSet(), MutableIntSet

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
    override fun contains(element: Int): Boolean = value == element

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
