package io.github.sooniln.fastcollect.ints

public fun emptyIntIterator(): IntListIterator = EmptyIntIterator

public abstract class MutableIntIterator : IntIterator(), MutableIterator<Int>

public abstract class IntListIterator: IntIterator(), ListIterator<Int> {
    @Deprecated(
        message = "Use previousInt() instead.",
        replaceWith = ReplaceWith("previousInt()"),
        level = DeprecationLevel.WARNING)
    final override fun previous(): Int = previousInt()
    public abstract fun previousInt(): Int
}

public abstract class MutableIntListIterator: IntListIterator(), MutableListIterator<Int>

private object EmptyIntIterator : IntListIterator() {
    override fun previousInt(): Int = throw NoSuchElementException()
    override fun nextInt(): Int = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}
