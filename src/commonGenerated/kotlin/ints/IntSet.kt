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
    val list = IntHashSet(expectedSize)
    list.builderAction()
    return list
}

public interface IntSet : Set<Int>, IntCollection {
    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Int): Boolean {
        for (e in this) {
            if (e == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<Int>): Boolean = super.containsAll(elements)
}

public interface MutableIntSet : IntSet, MutableIntCollection, MutableSet<Int> {
    override fun clear(): Unit = super.clear()
    override fun addAll(elements: Collection<Int>): Boolean = super.addAll(elements)
    override fun removeAll(elements: Collection<Int>): Boolean = super.removeAll(elements)
    override fun retainAll(elements: Collection<Int>): Boolean = super.retainAll(elements)
}

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
