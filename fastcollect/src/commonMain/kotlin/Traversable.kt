/**
 * Methods for dealing with Traversables.
 */
@file:JvmName("Traversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * Implementing this interface indicates that this object represents a sequence which can be traversed through to
 * produce a series of elements. Clients are generally encouraged to use the inline extension methods such as [traverse],
 * etc.
 *
 * This interface is intended as a replacement to [Iterable], to solve some of the problems with that interface:
 *   * Since [Iterator] has two separate functions for the concept of "Is there a next value?" and "What is the next
 *     value?" it is difficult to properly implement an [Iterator] over a sequence that has no well-defined "invalid"
 *     value (an iterator over every integer value for example). [Traverser] API behavior makes it much easier to
 *     implement these kind of sequences.
 *   * Since the [Iterator.hasNext] is distinct from [Iterator.next], it is necessary for [Iterator.next] to contract
 *     that implementations must throw [NoSuchElementException] if invoked incorrectly. This means implementations must
 *     contain duplicate validity checking logic, which can slow down iteration. While modern JVMs are quite good at
 *     eliminating these duplicate checks via range and value constraint analysis, this is by no means guaranteed, and
 *     there is a real risk that the JVM is unable to properly analyze and eliminate any moderately complex checks.
 *     [Traverser.forward] generally reduces (but does not eliminate) the number of checks and guards required.
 *
 *  A properly implemented [Traversable] should always be as simple to implement and as fast to iterate as [Iterable],
 *  and for complex structures is usually both simpler to implement and provides faster performance (up to 20% speed
 *  gains observed in benchmarking).
 */
public interface Traversable<out T> {

    /**
     * Constructs and returns a new [Traverser] which points to before the first value. Similar in use to
     * [Iterable.iterator].
     */
    public fun traverser(): Traverser<T>
}

public interface MutableTraversable<out T> : Traversable<T> {
    override fun traverser(): MutableTraverser<T>
}

/**
 * Provides a way of traversing through elements in a sequence. Like [Iterator], a Traverser points to a space
 * in-between two elements (or before the first element / after the last element), rather than pointing to elements
 * directly. This cursor is referred to as the position, and for a sequence with a finite size the position is in
 * [0, size] (unlike an index which is in [0, size)). A position of 0 is before all elements, and (for a sequence with
 * a finite size) the position of *size* is after all elements.
 *
 * **ANY** change to the structure of the [Traversable] that does not occur through a given Traverser invalidates that
 * Traverser. Some effort is made to throw [ConcurrentModificationException] in these cases (see [forward]), but there
 * is no guarantee that all possible illegal modifications will immediately result in an exception. It is the client's
 * responsibility not to shoot themselves in the foot. If you are traversing a structure, you should be sure it will
 * never be modified at the same time - except through the Traverser you are using.
 *
 * How to iterate with a Traverser:
 *
 * ```kotlin
 * val traverser = collection.traverser()
 * while (traverser.forward()) {
 *     doSomething(traverser.element)
 * }
 * ```
 *
 * NOTE: This interface extends Iterator because HotSpot special cases inlining behavior for Iterator in order to
 * improve performance (see https://bugs.openjdk.org/browse/JDK-8223504). More aggressive inlining allows better escape
 * analysis and thus scalar replacement. Since we want the same advantages this hack helps us achieve it (one hack
 * engenders another). The actual Iterator methods are never intended to be used.
 */
public interface Traverser<out T> : Iterator<Any?> {

    /**
     * Returns the element of the sequence the position last moved over. If the position has never been moved, then
     * this returns the element behind the initial position (as if initial position were reached by invoking [forward]),
     * or throws [IllegalStateException] if there is no element behind the initial position (the initial position is at
     * the beginning).
     */
    public val element: T

    /**
     * Attempts to move the position forward over the next element. If this method returns true, the attempt was
     * successful, and [element] has been updated to point at the element that the position moved over. If this method
     * returns false, the attempt was unsuccessful (because there is no further element to move over), and neither the
     * position nor [element] has been updated. Implementations are encouraged, but not required, to throw
     * [ConcurrentModificationException] where reasonable.
     */
    public fun forward(): Boolean

    /** DO NOT USE. See class-level note on why iterator interface exists here at all. */
    @Deprecated(level = DeprecationLevel.ERROR, message = "Not a real method, do not use.")
    override fun hasNext(): Boolean = throw UnsupportedOperationException()

    /** DO NOT USE. See class-level note on why iterator interface exists here at all. */
    @Deprecated(level = DeprecationLevel.ERROR, message = "Not a real method, do not use.")
    override fun next(): Nothing = throw UnsupportedOperationException()
}

public interface MutableTraverser<out T> : Traverser<T> {
    /**
     * Removes [element] from the sequence. Throws an exception in the same circumstances where [element] would throw an
     * exception. After removal [element] will throw an [IllegalStateException] until the position is moved again.
     */
    public fun remove()
}

/**
 * A [Traverser] over a list. A list is defined as a finite ordered collection, where order is independent of element
 * values. The fact that it is both finite and independently ordered means that ListTraverser can expose an integer
 * [position] value that indicates where this [Traverser] is currently pointing within the list. Since a list is
 * independently ordered, ListTraverser can expose a [backward] method which moves the cursor position in the opposite
 * direction as [forward].
 */
public interface ListTraverser<out T> : Traverser<T> {
    /**
     * The current cursor position within the list. Recall that this is a position not an index (since a [Traverser]
     * points at gaps between elements, not elements themselves). The position is updated whenever [forward] or
     * [backward] return true.
     */
    public val position: Int

    /**
     * Moves the cursor position in the opposite direction as [forward].
     */
    public fun backward(): Boolean
}

/**
 * A mutable [ListTraverser]. Since a list is independently ordered, MutableListTraverser can expose [set], which
 * changes the value of [element]. MutableListTraverser can also expose [insert], which inserts a new value at the
 * current cursor position.
 */
public interface MutableListTraverser<T> : ListTraverser<T>, MutableTraverser<T> {
    /**
     * Changes the value of the current [element]. Throws an exception in all circumstances where [element] would throw
     * an exception.
     */
    public fun set(value: T)

    /**
     * Inserts a value at the current cursor position. After insertion, the cursor is located behind the newly inserted
     * element, such that invoking [forward] will cause [element] to return the newly inserted value.
     */
    public fun insert(value: T)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <T> Traversable<T>.traverse(action: (T) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.element)
    }
}

@JvmSynthetic
public fun <T, A : Appendable> Traversable<T>.joinTo(
    buffer: A,
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "...",
    transform: ((T) -> CharSequence)? = null
): A {
    buffer.append(prefix)
    var count = 0
    val traverser = traverser()
    while (traverser.forward()) {
        val element = traverser.element
        if (++count > 1) buffer.append(separator)
        if (limit !in 0..<count) {
            when {
                transform != null -> buffer.append(transform(element))
                element is CharSequence? -> buffer.append(element)
                element is Char -> buffer.append(element)
                else -> buffer.append(element.toString())
            }
        } else {
            buffer.append(truncated)
            break
        }
    }
    buffer.append(postfix)
    return buffer
}

public fun <T> Traversable<T>.joinToString(
    separator: CharSequence = ", ",
    prefix: CharSequence = "",
    postfix: CharSequence = "",
    limit: Int = -1,
    truncated: CharSequence = "..."
): String {
    return joinTo(StringBuilder(), separator, prefix, postfix, limit, truncated).toString()
}
