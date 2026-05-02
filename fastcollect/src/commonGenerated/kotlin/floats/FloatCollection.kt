package io.github.sooniln.fastcollect.floats

/**
 * A collection of Floats which inherits from [Collection].
 */
public interface FloatCollection : Collection<Float> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): FloatIterator

    override fun contains(element: Float): Boolean {
        val it = iterator()
        while (it.hasNext()) {
            if (it.nextFloat() == element) {
                return true
            }
        }
        return false
    }

    public fun containsAll(elements: FloatCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Float>): Boolean {
        if (elements is FloatCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toFloatArray(): FloatArray {
        val result = FloatArray(size)
        var index = 0
        for (element in this) {
            result[index++] = element
        }
        return result
    }
}

/**
 * A mutable collection of Floats which inherits from [MutableCollection].
 */
public interface MutableFloatCollection : FloatCollection, MutableCollection<Float> {
    override fun iterator(): MutableFloatIterator

    override fun add(element: Float): Boolean
    override fun remove(element: Float): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextFloat()
            it.remove()
        }
    }

    public fun addAll(elements: FloatCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Float>): Boolean {
        if (elements is FloatCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun removeAll(elements: Collection<Float>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (elements.contains(it.nextFloat())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<Float>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (!elements.contains(it.nextFloat())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}

public fun MutableFloatCollection.removeAll(predicate: (Float) -> Boolean): Boolean = filterInPlace(predicate)
public fun MutableFloatCollection.retainAll(predicate: (Float) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }

private inline fun MutableFloatCollection.filterInPlace(removePredicate: (Float) -> Boolean): Boolean {
    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextFloat())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractFloatCollection : FloatCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}
