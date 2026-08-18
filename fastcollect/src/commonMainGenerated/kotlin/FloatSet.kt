/**
 * Methods for dealing with FloatSets.
 */
@file:JvmName("FloatSets")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

public fun emptyFloatSet(): FloatSet = EmptyFloatSet

public fun floatSetOf(): FloatSet = EmptyFloatSet
public fun floatSetOf(element: Float): FloatSet = SingletonFloatSet(element)
public fun floatSetOf(vararg elements: Float): FloatSet = FloatHashSet(elements.asFloatList())

public fun mutableFloatSetOf(): MutableFloatSet = FloatHashSet()
public fun mutableFloatSetOf(element: Float): MutableFloatSet = FloatHashSet(1).apply { add(element) }
public fun mutableFloatSetOf(vararg elements: Float): MutableFloatSet = FloatHashSet(elements.asFloatList())

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildFloatSet(expectedSize: Int = 0, builderAction: MutableFloatSet.() -> Unit): FloatSet {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val set = FloatHashSet(expectedSize)
    set.builderAction()
    return set
}

/**
 * A set of Floats.
 */
public interface FloatSet : FloatCollection {
    override fun contains(element: Float): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }
}

public fun FloatSet.asSet(): Set<Float> = FloatSetWrapper(this)

public infix fun FloatSet.union(other: FloatSet): FloatSet = FloatHashSet(this).apply { addAll(other) }

public infix fun FloatSet.intersect(other: FloatSet): FloatSet {
    if (other.size < size) return other.intersect(this)

    val set = FloatHashSet(size)
    for (element in other) {
        if (contains(element)) set.add(element)
    }
    return set
}

public infix fun FloatSet.subtract(other: FloatSet): FloatSet {
    val set = FloatHashSet(size)
    for (element in this) {
        if (!other.contains(element)) set.add(element)
    }
    return set
}

/**
 * A mutable set of Floats.
 */
public interface MutableFloatSet : FloatSet, MutableFloatCollection

public abstract class AbstractFloatSet : AbstractFloatCollection(), FloatSet {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is FloatSet) {
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

public abstract class AbstractMutableFloatSet : AbstractFloatSet(), MutableFloatSet {
    override fun removeAll(elements: Collection<Float>): Boolean = super<MutableFloatSet>.removeAll(elements)
    override fun retainAll(elements: Collection<Float>): Boolean = super<MutableFloatSet>.retainAll(elements)
}

public fun MutableFloatSet.asMutableSet(): MutableSet<Float> = MutableFloatSetWrapper(this)

private object EmptyFloatSet : FloatSet {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Float): Boolean = false
    override fun containsAll(elements: Collection<Float>): Boolean = elements.isEmpty()
    override fun containsAll(elements: FloatCollection): Boolean = elements.isEmpty()

    override fun iterator(): FloatIterator = emptyFloatIterator()

    override fun traverse(): FloatTraverser = emptyFloatTraverser()
}

private class SingletonFloatSet(private val value: Float) : FloatSet {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Float): Boolean = value equalsBoxed element

    override fun iterator(): FloatIterator = object : FloatIterator() {
        private var complete = false

        override fun nextFloat(): Float {
            if (complete) throw NoSuchElementException()
            complete = true
            return value
        }

        override fun hasNext(): Boolean = !complete
    }

    override fun traverse(): FloatTraverser = object : FloatTraverser, FloatCursor {
        private var consumed = false
        override val value: Float get() = this@SingletonFloatSet.value
        override fun advance(): FloatCursor? {
            if (consumed) return null
            consumed = true
            return this
        }
    }
}

private class FloatSetWrapper(private val set: FloatSet) : AbstractSet<Float>() {
    override val size: Int get() = set.size
    override fun contains(element: Float) = set.contains(element)
    override fun iterator() = set.iterator()
}

private class MutableFloatSetWrapper(private val set: MutableFloatSet) : AbstractMutableSet<Float>() {
    override val size: Int get() = set.size
    override fun contains(element: Float) = set.contains(element)
    override fun iterator() = set.iterator()

    override fun add(element: Float) = set.add(element)
    override fun remove(element: Float): Boolean = set.remove(element)
    override fun clear() = set.clear()

    override fun addAll(elements: Collection<Float>): Boolean = set.addAll(elements)
}
