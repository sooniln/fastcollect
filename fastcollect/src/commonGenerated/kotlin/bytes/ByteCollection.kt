package io.github.sooniln.fastcollect.bytes

/**
 * A collection of Bytes which inherits from [Collection].
 */
public interface ByteCollection : Collection<Byte> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): ByteIterator

    override fun contains(element: Byte): Boolean {
        val it = iterator()
        while (it.hasNext()) {
            if (it.nextByte() == element) {
                return true
            }
        }
        return false
    }

    public fun containsAll(elements: ByteCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Byte>): Boolean {
        if (elements is ByteCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toByteArray(): ByteArray {
        val result = ByteArray(size)
        var index = 0
        for (element in this) {
            result[index++] = element
        }
        return result
    }
}

/**
 * A mutable collection of Bytes which inherits from [MutableCollection].
 */
public interface MutableByteCollection : ByteCollection, MutableCollection<Byte> {
    override fun iterator(): MutableByteIterator

    override fun add(element: Byte): Boolean
    override fun remove(element: Byte): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextByte()
            it.remove()
        }
    }

    public fun addAll(elements: ByteCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Byte>): Boolean {
        if (elements is ByteCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun removeAll(elements: Collection<Byte>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (elements.contains(it.nextByte())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }

    override fun retainAll(elements: Collection<Byte>): Boolean {
        var modified = false
        val it = iterator()
        while (it.hasNext()) {
            if (!elements.contains(it.nextByte())) {
                it.remove()
                modified = true
            }
        }
        return modified
    }
}

public fun MutableByteCollection.removeAll(predicate: (Byte) -> Boolean): Boolean = filterInPlace(predicate)
public fun MutableByteCollection.retainAll(predicate: (Byte) -> Boolean): Boolean = filterInPlace { e -> !predicate(e) }

private inline fun MutableByteCollection.filterInPlace(removePredicate: (Byte) -> Boolean): Boolean {
    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextByte())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractByteCollection : ByteCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}
