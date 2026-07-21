package io.github.sooniln.fastcollect.longs

public fun emptyLongIterator(): LongListIterator = EmptyLongIterator
public fun emptyMutableLongIterator(): MutableLongIterator = EmptyMutableLongIterator

public abstract class MutableLongIterator : LongIterator(), MutableIterator<Long>

public abstract class LongListIterator: LongIterator(), ListIterator<Long> {
    @Deprecated(
        message = "Use previousLong() instead.",
        replaceWith = ReplaceWith("previousLong()"),
        level = DeprecationLevel.WARNING)
    final override fun previous(): Long = previousLong()
    public abstract fun previousLong(): Long
}

public abstract class MutableLongListIterator: LongListIterator(), MutableListIterator<Long>

private object EmptyLongIterator : MutableLongListIterator() {
    override fun previousLong(): Long = throw NoSuchElementException()
    override fun nextLong(): Long = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    override fun remove() = throw IllegalStateException()
    override fun set(element: Long) = throw IllegalStateException()
    override fun add(element: Long) = throw UnsupportedOperationException()
}

private object EmptyMutableLongIterator : MutableLongIterator() {
    override fun nextLong(): Long = throw NoSuchElementException()

    override fun hasNext(): Boolean = false

    override fun remove() = throw IllegalStateException()
}
