package io.github.sooniln.fastcollect.doubles

public fun emptyDoubleIterator(): DoubleListIterator = EmptyDoubleIterator
public fun emptyMutableDoubleIterator(): MutableDoubleIterator = EmptyMutableDoubleIterator

public abstract class MutableDoubleIterator : DoubleIterator(), MutableIterator<Double>

public abstract class DoubleListIterator: DoubleIterator(), ListIterator<Double> {
    @Deprecated(
        message = "Use previousDouble() instead.",
        replaceWith = ReplaceWith("previousDouble()"),
        level = DeprecationLevel.WARNING)
    final override fun previous(): Double = previousDouble()
    public abstract fun previousDouble(): Double
}

public abstract class MutableDoubleListIterator: DoubleListIterator(), MutableListIterator<Double>

private object EmptyDoubleIterator : MutableDoubleListIterator() {
    override fun previousDouble(): Double = throw NoSuchElementException()
    override fun nextDouble(): Double = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    override fun remove() = throw IllegalStateException()
    override fun set(element: Double) = throw IllegalStateException()
    override fun add(element: Double) = throw UnsupportedOperationException()
}

private object EmptyMutableDoubleIterator : MutableDoubleIterator() {
    override fun nextDouble(): Double = throw NoSuchElementException()

    override fun hasNext(): Boolean = false

    override fun remove() = throw IllegalStateException()
}
