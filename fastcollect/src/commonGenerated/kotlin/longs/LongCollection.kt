package io.github.sooniln.fastcollect.longs

/**
 * A collection of Longs which inherits from [Collection].
 */
public interface LongCollection : Collection<Long> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): LongIterator

    override fun contains(element: Long): Boolean {
        val it = iterator()
        while (it.hasNext()) {
            if (it.nextLong() == element) {
                return true
            }
        }
        return false
    }

    public fun containsAll(elements: LongCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Long>): Boolean {
        if (elements is LongCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toLongArray(): LongArray {
        val result = LongArray(size)
        var index = 0
        for (element in this) {
            result[index++] = element
        }
        return result
    }
}

/**
 * A mutable collection of Longs which inherits from [MutableCollection].
 */
public interface MutableLongCollection : LongCollection, MutableCollection<Long> {
    override fun iterator(): MutableLongIterator

    override fun add(element: Long): Boolean
    override fun remove(element: Long): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextLong()
            it.remove()
        }
    }

    public fun addAll(elements: LongCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Long>): Boolean {
        if (elements is LongCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun removeAll(elements: Collection<Long>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (elements.contains(it.nextLong())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<Long>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (!elements.contains(it.nextLong())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}

public fun MutableLongCollection.removeAll(predicate: (Long) -> Boolean): Boolean = filterInPlace(predicate)
public fun MutableLongCollection.retainAll(predicate: (Long) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }

private inline fun MutableLongCollection.filterInPlace(removePredicate: (Long) -> Boolean): Boolean {
    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextLong())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractLongCollection : LongCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}
