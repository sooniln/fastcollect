/**
 * Methods for dealing with primitive Iterators.
 */
@file:JvmName("Iterators")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun emptyLongIterator(): MutableLongIterator = EmptyLongIterator
public fun longIteratorOf(value: Long): LongIterator = SingletonLongIterator(value)

public abstract class MutableLongIterator : LongIterator(), MutableIterator<Long>

private object EmptyLongIterator : MutableLongIterator() {
    override fun hasNext(): Boolean = false
    override fun nextLong(): Long = throw NoSuchElementException()
    override fun remove() = throw IllegalStateException()
}

private class SingletonLongIterator(private val value: Long) : LongIterator() {
    private var done = false

    override fun hasNext(): Boolean = !done
    override fun nextLong(): Long {
        if (done) throw NoSuchElementException()
        done = true
        return value
    }
}
