package io.github.sooniln.fastcollect.shorts

public fun emptyShortIterator(): ShortListIterator = EmptyShortIterator

public abstract class MutableShortIterator : ShortIterator(), MutableIterator<Short>

public abstract class ShortListIterator: ShortIterator(), ListIterator<Short> {
    final override fun previous(): Short = previousShort()
    public abstract fun previousShort(): Short
}

public abstract class MutableShortListIterator: ShortListIterator(), MutableListIterator<Short>

private object EmptyShortIterator : ShortListIterator() {
    override fun previousShort(): Short = throw NoSuchElementException()
    override fun nextShort(): Short = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}