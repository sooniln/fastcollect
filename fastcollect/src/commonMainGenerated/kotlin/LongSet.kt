/**
 * Methods for dealing with LongSets.
 */
@file:JvmName("LongSets")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

public fun emptyLongSet(): LongSet = EmptyLongSet

public fun longSetOf(): LongSet = EmptyLongSet
public fun longSetOf(element: Long): LongSet = SingletonLongSet(element)
public fun longSetOf(vararg elements: Long): LongSet = LongHashSet(elements.asLongList())

public fun mutableLongSetOf(): MutableLongSet = LongHashSet()
public fun mutableLongSetOf(element: Long): MutableLongSet = LongHashSet(1).apply { add(element) }
public fun mutableLongSetOf(vararg elements: Long): MutableLongSet = LongHashSet(elements.asLongList())

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun buildLongSet(expectedSize: Int = 0, builderAction: MutableLongSet.() -> Unit): LongSet {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val set = LongHashSet(expectedSize)
    set.builderAction()
    return set
}

/**
 * A set of Longs.
 */
public interface LongSet : LongCollection {
    override fun contains(element: Long): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }
}

public fun LongSet.asSet(): Set<Long> = LongSetWrapper(this)

public infix fun LongSet.union(other: LongSet): LongSet = LongHashSet(this).apply { addAll(other) }

public infix fun LongSet.intersect(other: LongSet): LongSet {
    if (other.size < size) return other.intersect(this)

    val set = LongHashSet(size)
    for (element in other) {
        if (contains(element)) set.add(element)
    }
    return set
}

public infix fun LongSet.subtract(other: LongSet): LongSet {
    val set = LongHashSet(size)
    for (element in this) {
        if (!other.contains(element)) set.add(element)
    }
    return set
}

/**
 * A mutable set of Longs.
 */
public interface MutableLongSet : LongSet, MutableLongCollection

public abstract class AbstractLongSet : AbstractLongCollection(), LongSet {
    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is LongSet) {
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

public abstract class AbstractMutableLongSet : AbstractLongSet(), MutableLongSet {
    override fun removeAll(elements: Collection<Long>): Boolean = super<MutableLongSet>.removeAll(elements)
    override fun retainAll(elements: Collection<Long>): Boolean = super<MutableLongSet>.retainAll(elements)
}

public fun MutableLongSet.asMutableSet(): MutableSet<Long> = MutableLongSetWrapper(this)

private object EmptyLongSet : LongSet {
    override val size: Int get() = 0

    override fun isEmpty(): Boolean = true
    override fun contains(element: Long): Boolean = false
    override fun containsAll(elements: Collection<Long>): Boolean = elements.isEmpty()
    override fun containsAll(elements: LongCollection): Boolean = elements.isEmpty()

    override fun iterator(): LongIterator = emptyLongIterator()
}

private class SingletonLongSet(private val value: Long) : LongSet {
    override val size: Int get() = 1

    override fun isEmpty(): Boolean = false
    override fun contains(element: Long): Boolean = value equalsBoxed element

    override fun iterator(): LongIterator = object : LongIterator() {
        private var complete = false

        override fun nextLong(): Long {
            if (complete) throw NoSuchElementException()
            complete = true
            return value
        }

        override fun hasNext(): Boolean = !complete
    }
}

private class LongSetWrapper(private val set: LongSet) : AbstractSet<Long>() {
    override val size: Int get() = set.size
    override fun contains(element: Long) = set.contains(element)
    override fun iterator() = set.iterator()
}

private class MutableLongSetWrapper(private val set: MutableLongSet) : AbstractMutableSet<Long>() {
    override val size: Int get() = set.size
    override fun contains(element: Long) = set.contains(element)
    override fun iterator() = set.iterator()

    override fun add(element: Long) = set.add(element)
    override fun remove(element: Long): Boolean = set.remove(element)
    override fun clear() = set.clear()

    override fun addAll(elements: Collection<Long>): Boolean = set.addAll(elements)
}
