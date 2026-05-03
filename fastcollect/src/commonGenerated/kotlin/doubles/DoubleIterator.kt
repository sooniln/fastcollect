package io.github.sooniln.fastcollect.doubles

import io.github.sooniln.fastcollect.assertBoxing

public fun emptyDoubleIterator(): DoubleListIterator = EmptyDoubleIterator

public abstract class MutableDoubleIterator : DoubleIterator(), MutableIterator<Double>

public abstract class DoubleListIterator: DoubleIterator(), ListIterator<Double> {
    final override fun previous(): Double {
        assertBoxing()
        return previousDouble()
    }
    public abstract fun previousDouble(): Double
}

public abstract class MutableDoubleListIterator: DoubleListIterator(), MutableListIterator<Double>

private object EmptyDoubleIterator : DoubleListIterator() {
    override fun previousDouble(): Double = throw NoSuchElementException()
    override fun nextDouble(): Double = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}
