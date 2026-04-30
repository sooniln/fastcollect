package io.github.sooniln.fastcollect.ints

/**
 * A collection of Ints which inherits from [Collection].
 */
public interface IntCollection : Collection<Int> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): IntIterator

    override fun contains(element: Int): Boolean {
        val it = iterator()
        while (it.hasNext()) {
            if (it.nextInt() == element) {
                return true
            }
        }
        return false
    }

    public fun containsAll(elements: IntCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        if (elements is IntCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toIntArray(): IntArray {
        return (this as Collection<Int>).toIntArray()
    }
}

/**
 * A mutable collection of Ints which inherits from [MutableCollection].
 */
public interface MutableIntCollection : IntCollection, MutableCollection<Int> {
    override fun iterator(): MutableIntIterator

    override fun add(element: Int): Boolean
    override fun remove(element: Int): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextInt()
            it.remove()
        }
    }

    public fun addAll(elements: IntCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        if (elements is IntCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun removeAll(elements: Collection<Int>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (elements.contains(it.nextInt())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<Int>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (!elements.contains(it.nextInt())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}

public fun MutableIntCollection.removeAll(predicate: (Int) -> Boolean): Boolean = filterInPlace(predicate)
public fun MutableIntCollection.retainAll(predicate: (Int) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }

private inline fun MutableIntCollection.filterInPlace(removePredicate: (Int) -> Boolean): Boolean {
    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextInt())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractIntCollection : IntCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}
