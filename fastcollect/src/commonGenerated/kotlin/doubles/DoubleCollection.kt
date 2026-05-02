package io.github.sooniln.fastcollect.doubles

/**
 * A collection of Doubles which inherits from [Collection].
 */
public interface DoubleCollection : Collection<Double> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): DoubleIterator

    override fun contains(element: Double): Boolean {
        val it = iterator()
        while (it.hasNext()) {
            if (it.nextDouble() == element) {
                return true
            }
        }
        return false
    }

    public fun containsAll(elements: DoubleCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Double>): Boolean {
        if (elements is DoubleCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toDoubleArray(): DoubleArray {
        val result = DoubleArray(size)
        var index = 0
        for (element in this) {
            result[index++] = element
        }
        return result
    }
}

/**
 * A mutable collection of Doubles which inherits from [MutableCollection].
 */
public interface MutableDoubleCollection : DoubleCollection, MutableCollection<Double> {
    override fun iterator(): MutableDoubleIterator

    override fun add(element: Double): Boolean
    override fun remove(element: Double): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextDouble()
            it.remove()
        }
    }

    public fun addAll(elements: DoubleCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Double>): Boolean {
        if (elements is DoubleCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun removeAll(elements: Collection<Double>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (elements.contains(it.nextDouble())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<Double>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (!elements.contains(it.nextDouble())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}

public fun MutableDoubleCollection.removeAll(predicate: (Double) -> Boolean): Boolean = filterInPlace(predicate)
public fun MutableDoubleCollection.retainAll(predicate: (Double) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }

private inline fun MutableDoubleCollection.filterInPlace(removePredicate: (Double) -> Boolean): Boolean {
    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextDouble())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractDoubleCollection : DoubleCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}
