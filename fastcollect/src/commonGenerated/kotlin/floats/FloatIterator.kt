package io.github.sooniln.fastcollect.floats

public fun emptyFloatIterator(): FloatListIterator = EmptyFloatIterator
public fun emptyMutableFloatIterator(): MutableFloatIterator = EmptyMutableFloatIterator

public abstract class MutableFloatIterator : FloatIterator(), MutableIterator<Float>

public abstract class FloatListIterator: FloatIterator(), ListIterator<Float> {
    @Deprecated(
        message = "Use previousFloat() instead.",
        replaceWith = ReplaceWith("previousFloat()"),
        level = DeprecationLevel.WARNING)
    final override fun previous(): Float = previousFloat()
    public abstract fun previousFloat(): Float
}

public abstract class MutableFloatListIterator: FloatListIterator(), MutableListIterator<Float>

private object EmptyFloatIterator : MutableFloatListIterator() {
    override fun previousFloat(): Float = throw NoSuchElementException()
    override fun nextFloat(): Float = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    override fun remove() = throw IllegalStateException()
    override fun set(element: Float) = throw IllegalStateException()
    override fun add(element: Float) = throw UnsupportedOperationException()
}

private object EmptyMutableFloatIterator : MutableFloatIterator() {
    override fun nextFloat(): Float = throw NoSuchElementException()

    override fun hasNext(): Boolean = false

    override fun remove() = throw IllegalStateException()
}
