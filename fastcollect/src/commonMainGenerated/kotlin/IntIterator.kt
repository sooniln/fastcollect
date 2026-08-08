/**
 * Methods for dealing with primitive Iterators.
 */
@file:JvmName("Iterators")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun emptyIntIterator(): IntListIterator = EmptyIntIterator
public fun emptyMutableIntIterator(): MutableIntIterator = EmptyMutableIntIterator

public fun intIteratorOf(value: Int): IntListIterator = SingletonIntIterator(value)

public abstract class MutableIntIterator : IntIterator(), MutableIterator<Int>

public abstract class IntListIterator: IntIterator(), ListIterator<Int> {
    @Deprecated(
        message = "Use previousInt() instead.",
        replaceWith = ReplaceWith("previousInt()"),
        level = DeprecationLevel.WARNING)
    final override fun previous(): Int = previousInt()
    public abstract fun previousInt(): Int
}

public abstract class MutableIntListIterator: IntListIterator(), MutableListIterator<Int>

private object EmptyIntIterator : MutableIntListIterator() {
    override fun previousInt(): Int = throw NoSuchElementException()
    override fun nextInt(): Int = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    override fun remove() = throw IllegalStateException()
    override fun set(element: Int) = throw IllegalStateException()
    override fun add(element: Int) = throw UnsupportedOperationException()
}

private object EmptyMutableIntIterator : MutableIntIterator() {
    override fun nextInt(): Int = throw NoSuchElementException()

    override fun hasNext(): Boolean = false

    override fun remove() = throw IllegalStateException()
}

private class SingletonIntIterator(private val value: Int) : IntListIterator() {
    private var pos = 0

    override fun previousInt(): Int {
        if (pos == 0) throw NoSuchElementException()
        --pos
        return value
    }
    override fun nextInt(): Int {
        if (pos == 1) throw NoSuchElementException()
        ++pos
        return value
    }

    override fun hasNext(): Boolean = pos == 0
    override fun hasPrevious(): Boolean = pos == 1

    override fun nextIndex(): Int = pos
    override fun previousIndex(): Int = pos - 1
}
