package io.github.sooniln.fastcollect

/**
 * An [Iterable] which can be iterated normally, but also supports fast iteration. Fast iteration implies that the
 * object returned from [FastIterator.next] may not escape (i.e. the object returned from one `next()` call is
 * considered no longer valid and may no longer be used after the following `next()` call). This restriction allows
 * faster iteration because it is no longer necessary to allocate a new object with every `next()` call.
 *
 * Note that the JVM already performs some escape analysis, and for simple iterator usage (see below for example) there
 * may not be an immediately realized performance benefit to using [fastIterator] instead of [iterator] since the JVM is
 * already optimizing it away. However, on platforms that do not perform escape analysis, or if platform escape analysis
 * is unable to conclude that the returned value does not escape, [fastIterator] may perform substantially faster.
 *
 * Consider the following iterator usage:
 * ```
 * val iterator = ...
 * while (it.hasNext()) {
 *   val value = it.next()
 *   println(value)
 * }
 * ```
 *
 * Note that the `value` reference does not escape the loop - it is not stored anywhere where it might persist longer
 * than the next loop iteration and `next()` invocation. This means that [fastIterator] could be used here to ensure
 * better performance.
 *
 * Consider instead the following iterator usage:
 * ```
 * var previousValue: Any? = null
 * var value: Any? = null
 * val iterator = ...
 * while (it.hasNext()) {
 *   previousValue = value
 *   value = it.next()
 *   println("($previousValue, $value)")
 * }
 * ```
 *
 * It is clear that `value` escapes the loop - it is stored and will persist inside `previousValue` through the next
 * loop iteration. This means that [fastIterator] is not safe for use here.
 */
public interface FastIterable<out T> : Iterable<T> {
    /**
     * May be used instead of [iterator] for faster iteration if the value returned by [FastIterator.next] does not
     * escape the iterator loop.
     */
    public fun fastIterator(): FastIterator<T>
}

/**
 * The mutable version of [FastIterable].
 */
public interface MutableFastIterable<out T> : FastIterable<T>, MutableIterable<T> {
    /**
     * The mutable version of [FastIterable.fastIterator].
     */
    override fun fastIterator(): MutableFastIterator<T>
}

/** Returns an empty [FastIterator]. */
public fun <T> emptyFastIterator(): FastIterator<T> = object : FastIterator<T> {
    override fun hasNext() = false
    override fun next(): T = throw NoSuchElementException()
}

/**
 * A fast iterator which can be used to improve iteration performance. See [FastIterable] for details.
 */
public interface FastIterator<out T> : Iterator<T>

/**
 * A mutable fast iterator which can be used to improve iteration performance. See [FastIterable] for details.
 */
public interface MutableFastIterator<out T> : FastIterator<T>, MutableIterator<T>
