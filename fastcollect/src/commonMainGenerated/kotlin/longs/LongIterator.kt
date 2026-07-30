package io.github.sooniln.fastcollect.longs

public fun emptyLongIterator(): LongListIterator = EmptyLongIterator
public fun emptyMutableLongIterator(): MutableLongIterator = EmptyMutableLongIterator

public fun longIteratorOf(value: Long): LongListIterator = SingletonLongIterator(value)

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

private class SingletonLongIterator(private val value: Long) : LongListIterator() {
    private var pos = 0

    override fun previousLong(): Long {
        if (pos == 0) throw NoSuchElementException()
        --pos
        return value
    }
    override fun nextLong(): Long {
        if (pos == 1) throw NoSuchElementException()
        ++pos
        return value
    }

    override fun hasNext(): Boolean = pos == 0
    override fun hasPrevious(): Boolean = pos == 1

    override fun nextIndex(): Int = pos
    override fun previousIndex(): Int = pos - 1
}
