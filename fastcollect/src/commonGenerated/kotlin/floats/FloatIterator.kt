package io.github.sooniln.fastcollect.floats

public fun emptyFloatIterator(): FloatListIterator = EmptyFloatIterator
public fun emptyMutableFloatIterator(): MutableFloatIterator = EmptyMutableFloatIterator

public fun floatIteratorOf(value: Float): FloatListIterator = SingletonFloatIterator(value)

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

private class SingletonFloatIterator(private val value: Float) : FloatListIterator() {
    private var pos = 0

    override fun previousFloat(): Float {
        if (pos == 0) throw NoSuchElementException()
        --pos
        return value
    }
    override fun nextFloat(): Float {
        if (pos == 1) throw NoSuchElementException()
        ++pos
        return value
    }

    override fun hasNext(): Boolean = pos == 0
    override fun hasPrevious(): Boolean = pos == 1

    override fun nextIndex(): Int = pos
    override fun previousIndex(): Int = pos - 1
}
