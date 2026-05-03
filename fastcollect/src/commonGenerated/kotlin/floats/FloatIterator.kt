package io.github.sooniln.fastcollect.floats

import io.github.sooniln.fastcollect.assertBoxing

public fun emptyFloatIterator(): FloatListIterator = EmptyFloatIterator

public abstract class MutableFloatIterator : FloatIterator(), MutableIterator<Float>

public abstract class FloatListIterator: FloatIterator(), ListIterator<Float> {
    final override fun previous(): Float {
        assertBoxing()
        return previousFloat()
    }
    public abstract fun previousFloat(): Float
}

public abstract class MutableFloatListIterator: FloatListIterator(), MutableListIterator<Float>

private object EmptyFloatIterator : FloatListIterator() {
    override fun previousFloat(): Float = throw NoSuchElementException()
    override fun nextFloat(): Float = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}
