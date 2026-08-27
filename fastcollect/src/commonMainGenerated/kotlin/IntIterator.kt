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
