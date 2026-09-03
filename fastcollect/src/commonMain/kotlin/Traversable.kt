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
 * directly. This cursor is referred to as the position (and for a sequence with a finite size the position is in
 * 0..size, unlike the index which is in 0..<size). A position of 0 is before all elements (and for a sequence with
 * a finite size, the position of *size* is after all elements).
 *
 * **ANY** change to the structure of the [Traversable] that does not occur through a Traverser invalidates that
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
 * NOTE: This interface extends Iterator because many JVMs special case inlining behavior for Iterator and related
 * classes in order to improve performance (see https://bugs.openjdk.org/browse/JDK-8223504). We want the same inlining
 * behavior since we're handling the exact same use cases as Iterator - this hack helps us achieve it. The actual
 * Iterator methods are never intended to be used.
 */
public interface Traverser<out T> : Iterator<Any?> {

    /**
     * Returns the last element of the sequence the position moved over. If the position has never been moved, then
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
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "Not a real method, do not use.", replaceWith = ReplaceWith("value"))
    override fun hasNext(): Boolean = throw UnsupportedOperationException()

    /** DO NOT USE. See class-level note on why iterator interface exists here at all. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "Not a real method, do not use.")
    override fun next(): Nothing = throw UnsupportedOperationException()
}

public interface MutableTraverser<out T> : Traverser<T> {
    /**
     * Removes [element] from the sequence. Throws an exception in the same circumstances where [element] would throw an
     * exception. After removal [element] will throw an exception until the position is moved again.
     */
    public fun remove()
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
