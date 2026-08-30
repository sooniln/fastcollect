/**
 * Methods for dealing with ByteTraversables.
 */
@file:JvmName("ByteTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun emptyByteTraverser(): MutableByteTraverser = EmptyByteTraverser

/**
 * A primitively typed [Traversable] of Bytes.
 */
public interface ByteTraversable: Traversable<Byte> {
    override fun traverser(): ByteTraverser
}

/**
 * A primitively typed [MutableTraversable] of Bytes.
 */
public interface MutableByteTraversable: MutableTraversable<Byte> {
    override fun traverser(): MutableByteTraverser
}

/**
 * A primitively typed [Traverser] of Bytes.
 *
 * How to iterate with a ByteTraverser:
 *
 * ```kotlin
 * val traverser = byteCollection.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.value)
 * }
 * ```
 */
public interface ByteTraverser : Traverser<Byte> {
    public val value: Byte

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.", replaceWith = ReplaceWith("value"))
    @get:JvmSynthetic
    override val element: Byte get() = value
}

/**
 * A primitively typed [MutableTraverser] of Bytes.
 */
public interface MutableByteTraverser : ByteTraverser, MutableTraverser<Byte>

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteTraversable.foreach(action: (Byte) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteTraversable.any(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { if (predicate(it)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteTraversable.all(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteTraversable.none(predicate: (Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> ByteTraversable.fold(initial: R, operation: (accumulated: R, Byte) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { accumulated = operation(accumulated, it) }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun ByteTraversable.reduce(operation: (accumulated: Byte, Byte) -> Byte): Byte {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    if (!traverser.forward()) throw NoSuchElementException()
    var accumulated = traverser.value
    while (traverser.forward()) {
        accumulated = operation(accumulated, traverser.value)
    }
    return accumulated
}


@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
public fun ByteTraversable.sum(): Int = sumOf { it.toInt() }


@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun ByteTraversable.sumOf(selector: (Byte) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { sum += selector(it) }
    return sum
}



@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun ByteTraversable.sumOf(selector: (Byte) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { sum += selector(it) }
    return sum
}



@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun ByteTraversable.sumOf(selector: (Byte) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { sum += selector(it) }
    return sum
}

private object EmptyByteTraverser : MutableByteTraverser {
    override fun forward(): Boolean = false
    override val value: Byte get() = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
