package io.github.sooniln.fastcollect.shorts

public fun emptyShortIterator(): ShortListIterator = EmptyShortIterator
public fun emptyMutableShortIterator(): MutableShortIterator = EmptyMutableShortIterator

public abstract class MutableShortIterator : ShortIterator(), MutableIterator<Short>

public abstract class ShortListIterator: ShortIterator(), ListIterator<Short> {
    @Deprecated(
        message = "Use previousShort() instead.",
        replaceWith = ReplaceWith("previousShort()"),
        level = DeprecationLevel.WARNING)
    final override fun previous(): Short = previousShort()
    public abstract fun previousShort(): Short
}

public abstract class MutableShortListIterator: ShortListIterator(), MutableListIterator<Short>

private object EmptyShortIterator : MutableShortListIterator() {
    override fun previousShort(): Short = throw NoSuchElementException()
    override fun nextShort(): Short = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    override fun remove() = throw IllegalStateException()
    override fun set(element: Short) = throw IllegalStateException()
    override fun add(element: Short) = throw UnsupportedOperationException()
}

private object EmptyMutableShortIterator : MutableShortIterator() {
    override fun nextShort(): Short = throw NoSuchElementException()

    override fun hasNext(): Boolean = false

    override fun remove() = throw IllegalStateException()
}
