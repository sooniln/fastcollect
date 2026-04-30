package io.github.sooniln.fastcollect.shorts

/**
 * A collection of Shorts which inherits from [Collection].
 */
public interface ShortCollection : Collection<Short> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): ShortIterator

    override fun contains(element: Short): Boolean {
        val it = iterator()
        while (it.hasNext()) {
            if (it.nextShort() == element) {
                return true
            }
        }
        return false
    }

    public fun containsAll(elements: ShortCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Short>): Boolean {
        if (elements is ShortCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toShortArray(): ShortArray {
        return (this as Collection<Short>).toShortArray()
    }
}

/**
 * A mutable collection of Shorts which inherits from [MutableCollection].
 */
public interface MutableShortCollection : ShortCollection, MutableCollection<Short> {
    override fun iterator(): MutableShortIterator

    override fun add(element: Short): Boolean
    override fun remove(element: Short): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextShort()
            it.remove()
        }
    }

    public fun addAll(elements: ShortCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Short>): Boolean {
        if (elements is ShortCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun removeAll(elements: Collection<Short>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (elements.contains(it.nextShort())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<Short>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (!elements.contains(it.nextShort())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}

public fun MutableShortCollection.removeAll(predicate: (Short) -> Boolean): Boolean = filterInPlace(predicate)
public fun MutableShortCollection.retainAll(predicate: (Short) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }

private inline fun MutableShortCollection.filterInPlace(removePredicate: (Short) -> Boolean): Boolean {
    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextShort())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractShortCollection : ShortCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}
