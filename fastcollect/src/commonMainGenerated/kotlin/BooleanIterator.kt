/**
 * Methods for dealing with primitive Iterators.
 */
@file:JvmName("Iterators")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun emptyBooleanIterator(): BooleanListIterator = EmptyBooleanIterator
public fun emptyMutableBooleanIterator(): MutableBooleanIterator = EmptyMutableBooleanIterator

public fun booleanIteratorOf(value: Boolean): BooleanListIterator = SingletonBooleanIterator(value)

public abstract class MutableBooleanIterator : BooleanIterator(), MutableIterator<Boolean>

public abstract class BooleanListIterator: BooleanIterator(), ListIterator<Boolean> {
    @Deprecated(
        message = "Use previousBoolean() instead.",
        replaceWith = ReplaceWith("previousBoolean()"),
        level = DeprecationLevel.WARNING)
    final override fun previous(): Boolean = previousBoolean()
    public abstract fun previousBoolean(): Boolean
}

public abstract class MutableBooleanListIterator: BooleanListIterator(), MutableListIterator<Boolean>

private object EmptyBooleanIterator : MutableBooleanListIterator() {
    override fun previousBoolean(): Boolean = throw NoSuchElementException()
    override fun nextBoolean(): Boolean = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    override fun remove() = throw IllegalStateException()
    override fun set(element: Boolean) = throw IllegalStateException()
    override fun add(element: Boolean) = throw UnsupportedOperationException()
}

private object EmptyMutableBooleanIterator : MutableBooleanIterator() {
    override fun nextBoolean(): Boolean = throw NoSuchElementException()

    override fun hasNext(): Boolean = false

    override fun remove() = throw IllegalStateException()
}

private class SingletonBooleanIterator(private val value: Boolean) : BooleanListIterator() {
    private var pos = 0

    override fun previousBoolean(): Boolean {
        if (pos == 0) throw NoSuchElementException()
        --pos
        return value
    }
    override fun nextBoolean(): Boolean {
        if (pos == 1) throw NoSuchElementException()
        ++pos
        return value
    }

    override fun hasNext(): Boolean = pos == 0
    override fun hasPrevious(): Boolean = pos == 1

    override fun nextIndex(): Int = pos
    override fun previousIndex(): Int = pos - 1
}
