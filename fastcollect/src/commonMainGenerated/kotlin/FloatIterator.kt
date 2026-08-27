/**
 * Methods for dealing with primitive Iterators.
 */
@file:JvmName("Iterators")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun emptyFloatIterator(): MutableFloatIterator = EmptyFloatIterator
public fun floatIteratorOf(value: Float): FloatIterator = SingletonFloatIterator(value)

public abstract class MutableFloatIterator : FloatIterator(), MutableIterator<Float>

private object EmptyFloatIterator : MutableFloatIterator() {
    override fun hasNext(): Boolean = false
    override fun nextFloat(): Float = throw NoSuchElementException()
    override fun remove() = throw IllegalStateException()
}

private class SingletonFloatIterator(private val value: Float) : FloatIterator() {
    private var done = false

    override fun hasNext(): Boolean = !done
    override fun nextFloat(): Float {
        if (done) throw NoSuchElementException()
        done = true
        return value
    }
}
