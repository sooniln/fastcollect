/**
 * Methods for dealing with Traversables.
 */
@file:JvmName("Traversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * Implementing this interface indicates that this object can be traversed through to produce a series of values.
 * Clients are generally encouraged not to use methods from this interface directly, but to use the inline extension
 * methods provided by the concrete [Traversable] subtype (e.g. [IntTraversable.foreach], etc.).
 *
 * This interface is intended as a replacement to [Iterable], to solve some of the problems with that interface:
 *   * Since [Iterator] has two separate functions for the concept of "Is there a next value?" and "What is the next
 *     value?" it is difficult to properly implement an [Iterator] over a container that has no well-defined "invalid"
 *     value (an iterator over every integer value for example). Traversable does not solve this completely, but does
 *     make it easier to implement such things.
 *   * Since the [Iterator.hasNext] is distinct from [Iterator.next], it is necessary for [Iterator.next] to contract
 *     that implementations must throw [NoSuchElementException] if invoked incorrectly. This means implementations must
 *     contain duplicate validity checking logic, which can slow down iteration. While modern JVMs are quite good at
 *     eliminating these duplicate checks via range and value constraint analysis, this is by no means guaranteed, and
 *     there is a real risk that the JVM is unable to properly analyze and eliminate any moderately complex checks. In
 *     contrast, [Traverser.advance] achieves correctness through API design - since a Cursor is returned only if there
 *     is a valid state, it is impossible to access an invalid state.
 *   * Since [Iterator.next] is the only method of obtaining values, and since a function can only have a single return
 *     value, [Iterator] forces implementors to wrap all possible useful fields into a single return. This means not
 *     only an extra allocation for a wrapper object if necessary (like [Map.Entry]), it also means paying the price
 *     for all possible fields regardless of whether the client is going to use them or not. While escape analysis can
 *     often prevent expensive allocation/GC paths for these short-lived wrapper objects, this is not guaranteed, and
 *     escape analysis can often fail on complex code. By contrast, [Cursor] does not force a wrapper object and can
 *     load fields on demand.
 *   * Since [Iterable.iterator] is used to return both mutable and read-only [Iterator] types, mutable containers must
 *     always return a mutable [Iterator] - which means all read-only usages pay the memory/CPU overhead required for
 *     the mutable logic (this overhead is usually extremely minimal, but that is not guaranteed or always the case).
 *
 *  In summary, a properly implemented [Traversable] should always be as simple to implement and as fast to iterate as
 *  [Iterable], and in some cases may be much simpler to implement and iterate faster (~5-15% speed gains observed in
 *  benchmarking).
 */
public interface Traversable {

    /**
     * Constructs and returns a new [Traverser] which points to before the first value. Similar in use to
     * [Iterable.iterator].
     */
    public fun traverse(): Traverser
}

public interface ValueTraversable<V> : Traversable {
    override fun traverse(): ValueTraverser<V>
}

public interface KeyValueTraversable<K, V> : Traversable {
    override fun traverse(): KeyValueTraverser<K, V>
}

/**
 * Provides a way of traversing through positions in a container. Usually specialized further to provide methods of
 * accessing values for the given position (see [IntTraverser] or [Int2IntTraverser] for example). Similar in use to
 * [Iterator].
 *
 * NOTE: This interface extends Iterator because many JVMs special case inlining behavior for Iterator and related
 * classes in order to improve performance (see https://bugs.openjdk.org/browse/JDK-8223504). We want the same inlining
 * behavior since we're handling the exact same use cases as Iterator - this hack helps us achieve it. The actual
 * Iterator methods are never intended to be used.
 */
public interface Traverser : Iterator<Nothing> {

    /**
     * Moves this [Traverser] to the next position, and returns a [Cursor] pointing to the data at that position.
     * Returns null if there is no next position and the traverse is complete. The returned cursor becomes invalid and
     * may no longer be accessed as soon as [advance] is called again (thus implementations are allowed to reuse Cursor
     * instances under the hood if desired). Once this returns null once, it may never return any non-null value ever
     * again. Implementations are encouraged, but not required, to throw [ConcurrentModificationException] where
     * reasonable.
     *
     * Similar in use to [Iterator.hasNext] and [Iterator.next] combined.
     */
    public fun advance(): Boolean

    // see class-level note on why iterator interface exists here at all
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "Not a real method, do not use.")
    @JvmSynthetic
    override fun hasNext(): Boolean = throw UnsupportedOperationException()

    // see class-level note on why iterator interface exists here at all
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "Not a real method, do not use.")
    @JvmSynthetic
    override fun next(): Nothing = throw UnsupportedOperationException()
}

public interface ValueTraverser<V> : Traverser {
    public val value: V
}

public interface KeyValueTraverser<K, V> : Traverser {
    public val key: K
    public val value: V
}

/**
 * Represents a pointer to some data at the current [Traverser] position. Implementations should attempt to load
 * data on-demand where there is a reasonable gain to be made by doing so.
 */
public interface Cursor
