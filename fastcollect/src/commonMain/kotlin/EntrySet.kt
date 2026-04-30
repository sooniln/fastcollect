package io.github.sooniln.fastcollect

/**
 * Returns an immutable empty [EntrySet].
 */
@Suppress("UNCHECKED_CAST")
public fun <K, V, T : Map.Entry<K, V>> emptyEntrySet(): EntrySet<T> = EmptyEntrySet as EntrySet<T>

/**
 * Returns an immutable singleton [EntrySet].
 */
public fun <K, V, T : Map.Entry<K, V>> entrySetOf(entry: T): EntrySet<T> = SingletonEntrySet(entry)

/**
 * A [Set] of [Map.Entry] which is also a [FastIterable] and thus supports fast iteration. See [FastIterable] for more
 * details on fast iteration.
 */
public interface EntrySet<out T : Map.Entry<*, *>> : Set<T>, FastIterable<T>

/**
 * A [MutableSet] of [MutableMap.MutableEntry] which is also a [MutableFastIterable] and thus supports fast iteration.
 * See [FastIterable] for more details on fast iteration.
 */
public interface MutableEntrySet<out T : MutableMap.MutableEntry<*, *>> : EntrySet<T>, MutableFastIterable<T>

private object EmptyEntrySet : EntrySet<Map.Entry<Nothing, Nothing>> {
    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun contains(element: Map.Entry<Nothing, Nothing>): Boolean = false
    override fun containsAll(elements: Collection<Map.Entry<Nothing, Nothing>>): Boolean = elements.isEmpty()

    override fun iterator(): Iterator<Map.Entry<Nothing, Nothing>> = emptyList<Map.Entry<Nothing, Nothing>>().iterator()
    override fun fastIterator(): FastIterator<Map.Entry<Nothing, Nothing>> = EmptyFastIterator
}

private object EmptyFastIterator : FastIterator<Nothing> {
    override fun hasNext(): Boolean = false
    override fun next(): Nothing = throw NoSuchElementException()
}

private class SingletonEntrySet<T : Map.Entry<*, *>>(private val entry: T) : AbstractSet<T>(), EntrySet<T> {
    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun contains(element: T): Boolean = element == entry

    override fun iterator(): Iterator<T> = fastIterator()
    override fun fastIterator(): FastIterator<T> = object : FastIterator<T> {
        private var complete = false

        override fun hasNext(): Boolean = !complete
        override fun next(): T {
            if (complete) throw NoSuchElementException()
            complete = true
            return entry
        }
    }
}
