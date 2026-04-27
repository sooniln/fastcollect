package io.github.sooniln.fastcollect

public interface FastIterable<out T> : Iterable<T> {
    public fun fastIterator(): FastIterator<T>
}

public interface MutableFastIterable<out T> : FastIterable<T>, MutableIterable<T> {
    override fun fastIterator(): MutableFastIterator<T>
}

public interface FastIterator<out T> : Iterator<T>

public interface MutableFastIterator<out T> : FastIterator<T>, MutableIterator<T>
