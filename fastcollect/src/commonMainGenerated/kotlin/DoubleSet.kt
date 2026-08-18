/**
 * Methods for dealing with DoubleSets.
 */
@file:JvmName("DoubleSets")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

public fun emptyDoubleSet(): DoubleSet = EmptyDoubleSet

public fun doubleSetOf(): DoubleSet = EmptyDoubleSet
public fun doubleSetOf(element: Double): DoubleSet = SingletonDoubleSet(element)
public fun doubleSetOf(vararg elements: Double): DoubleSet = DoubleHashSet(elements.asDoubleList())

public fun mutableDoubleSetOf(): MutableDoubleSet = DoubleHashSet()
public fun mutableDoubleSetOf(element: Double): MutableDoubleSet = DoubleHashSet(1).apply { add(element) }
public fun mutableDoubleSetOf(vararg elements: Double): MutableDoubleSet = DoubleHashSet(elements.asDoubleList())

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildDoubleSet(expectedSize: Int = 0, builderAction: MutableDoubleSet.() -> Unit): DoubleSet {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val set = DoubleHashSet(expectedSize)
    set.builderAction()
    return set
}

/**
 * A set of Doubles.
 */
public interface DoubleSet : DoubleCollection {
    override fun contains(element: Double): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }
}

public fun DoubleSet.asSet(): Set<Double> = DoubleSetWrapper(this)

public infix fun DoubleSet.union(other: DoubleSet): DoubleSet = DoubleHashSet(this).apply { addAll(other) }

public infix fun DoubleSet.intersect(other: DoubleSet): DoubleSet {
    if (other.size < size) return other.intersect(this)

    val set = DoubleHashSet(size)
    for (element in other) {
        if (contains(element)) set.add(element)
    }
    return set
}

public infix fun DoubleSet.subtract(other: DoubleSet): DoubleSet {
    val set = DoubleHashSet(size)
    for (element in this) {
        if (!other.contains(element)) set.add(element)
    }
    return set
}

/**
 * A mutable set of Doubles.
 */
public interface MutableDoubleSet : DoubleSet, MutableDoubleCollection

public abstract class AbstractDoubleSet : AbstractDoubleCollection(), DoubleSet {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is DoubleSet) {
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

public abstract class AbstractMutableDoubleSet : AbstractDoubleSet(), MutableDoubleSet {
    override fun removeAll(elements: Collection<Double>): Boolean = super<MutableDoubleSet>.removeAll(elements)
    override fun retainAll(elements: Collection<Double>): Boolean = super<MutableDoubleSet>.retainAll(elements)
}

public fun MutableDoubleSet.asMutableSet(): MutableSet<Double> = MutableDoubleSetWrapper(this)

private object EmptyDoubleSet : DoubleSet {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Double): Boolean = false
    override fun containsAll(elements: Collection<Double>): Boolean = elements.isEmpty()
    override fun containsAll(elements: DoubleCollection): Boolean = elements.isEmpty()

    override fun iterator(): DoubleIterator = emptyDoubleIterator()

    override fun traverse(): DoubleTraverser = emptyDoubleTraverser()
}

private class SingletonDoubleSet(private val value: Double) : DoubleSet {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Double): Boolean = value equalsBoxed element

    override fun iterator(): DoubleIterator = object : DoubleIterator() {
        private var complete = false

        override fun nextDouble(): Double {
            if (complete) throw NoSuchElementException()
            complete = true
            return value
        }

        override fun hasNext(): Boolean = !complete
    }

    override fun traverse(): DoubleTraverser = object : DoubleTraverser, DoubleCursor {
        private var consumed = false
        override val value: Double get() = this@SingletonDoubleSet.value
        override fun advance(): DoubleCursor? {
            if (consumed) return null
            consumed = true
            return this
        }
    }
}

private class DoubleSetWrapper(private val set: DoubleSet) : AbstractSet<Double>() {
    override val size: Int get() = set.size
    override fun contains(element: Double) = set.contains(element)
    override fun iterator() = set.iterator()
}

private class MutableDoubleSetWrapper(private val set: MutableDoubleSet) : AbstractMutableSet<Double>() {
    override val size: Int get() = set.size
    override fun contains(element: Double) = set.contains(element)
    override fun iterator() = set.iterator()

    override fun add(element: Double) = set.add(element)
    override fun remove(element: Double): Boolean = set.remove(element)
    override fun clear() = set.clear()

    override fun addAll(elements: Collection<Double>): Boolean = set.addAll(elements)
}
