package io.github.sooniln.fastcollect.longs

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

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
    val list = LongHashSet(expectedSize)
    list.builderAction()
    return list
}

public interface LongSet : Set<Long>, LongCollection {
    override fun isEmpty(): Boolean = super.isEmpty()

    override fun contains(element: Long): Boolean {
        for (e in this) {
            if (e == element) return true
        }
        return false
    }

    override fun containsAll(elements: Collection<Long>): Boolean = super.containsAll(elements)
}

public interface MutableLongSet : LongSet, MutableLongCollection

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
    override fun contains(element: Long): Boolean = value == element

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