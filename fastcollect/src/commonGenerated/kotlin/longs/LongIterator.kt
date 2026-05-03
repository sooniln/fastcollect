package io.github.sooniln.fastcollect.longs

import io.github.sooniln.fastcollect.assertBoxing

public fun emptyLongIterator(): LongListIterator = EmptyLongIterator

public abstract class MutableLongIterator : LongIterator(), MutableIterator<Long>

public abstract class LongListIterator: LongIterator(), ListIterator<Long> {
    final override fun previous(): Long {
        assertBoxing()
        return previousLong()
    }
    public abstract fun previousLong(): Long
}

public abstract class MutableLongListIterator: LongListIterator(), MutableListIterator<Long>

private object EmptyLongIterator : LongListIterator() {
    override fun previousLong(): Long = throw NoSuchElementException()
    override fun nextLong(): Long = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}
