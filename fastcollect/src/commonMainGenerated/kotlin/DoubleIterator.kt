/**
 * Methods for dealing with primitive Iterators.
 */
@file:JvmName("Iterators")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun emptyDoubleIterator(): MutableDoubleIterator = EmptyDoubleIterator
public fun doubleIteratorOf(value: Double): DoubleIterator = SingletonDoubleIterator(value)

public abstract class MutableDoubleIterator : DoubleIterator(), MutableIterator<Double>

private object EmptyDoubleIterator : MutableDoubleIterator() {
    override fun hasNext(): Boolean = false
    override fun nextDouble(): Double = throw NoSuchElementException()
    override fun remove() = throw IllegalStateException()
}

private class SingletonDoubleIterator(private val value: Double) : DoubleIterator() {
    private var done = false

    override fun hasNext(): Boolean = !done
    override fun nextDouble(): Double {
        if (done) throw NoSuchElementException()
        done = true
        return value
    }
}
