/**
 * Methods for dealing with primitive Iterators.
 */
@file:JvmName("Iterators")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun emptyIntIterator(): MutableIntIterator = EmptyIntIterator
public fun intIteratorOf(value: Int): IntIterator = SingletonIntIterator(value)

public abstract class MutableIntIterator : IntIterator(), MutableIterator<Int>

public abstract class IntListIterator : IntIterator(), ListIterator<Int> {
    public abstract fun previousInt(): Int
    final override fun previous(): Int = previousInt()
}

public abstract class MutableIntListIterator : IntListIterator(), MutableListIterator<Int> {
    abstract override fun set(element: Int)
    abstract override fun add(element: Int)
}

private object EmptyIntIterator : MutableIntIterator() {
    override fun hasNext(): Boolean = false
    override fun nextInt(): Int = throw NoSuchElementException()
    override fun remove() = throw IllegalStateException()
}

private class SingletonIntIterator(private val value: Int) : IntIterator() {
    private var done = false

    override fun hasNext(): Boolean = !done
    override fun nextInt(): Int {
        if (done) throw NoSuchElementException()
        done = true
        return value
    }
}
