/**
 * Methods for dealing with IntTraversables.
 */
@file:JvmName("IntTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun emptyIntTraverser(): MutableIntTraverser = EmptyIntTraverser

/**
 * A primitively typed [Traversable] of Ints.
 */
public interface IntTraversable: Traversable<Int> {
    override fun traverser(): IntTraverser
}

/**
 * A primitively typed [MutableTraversable] of Ints.
 */
public interface MutableIntTraversable: MutableTraversable<Int> {
    override fun traverser(): MutableIntTraverser
}

/**
 * A primitively typed [Traverser] of Ints.
 *
 * How to iterate with a IntTraverser:
 *
 * ```kotlin
 * val traverser = intCollection.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.value)
 * }
 * ```
 */
public interface IntTraverser : Traverser<Int> {
    public val value: Int

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.", replaceWith = ReplaceWith("value"))
    @get:JvmSynthetic
    override val element: Int get() = value
}

/**
 * A primitively typed [MutableTraverser] of Ints.
 */
public interface MutableIntTraverser : IntTraverser, MutableTraverser<Int>

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntTraversable.foreach(action: (Int) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntTraversable.any(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { if (predicate(it)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntTraversable.all(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntTraversable.none(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> IntTraversable.fold(initial: R, operation: (accumulated: R, Int) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { accumulated = operation(accumulated, it) }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun IntTraversable.reduce(operation: (accumulated: Int, Int) -> Int): Int {
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
public fun IntTraversable.sum(): Int = sumOf { it.toInt() }


@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun IntTraversable.sumOf(selector: (Int) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { sum += selector(it) }
    return sum
}



@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun IntTraversable.sumOf(selector: (Int) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { sum += selector(it) }
    return sum
}



@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun IntTraversable.sumOf(selector: (Int) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { sum += selector(it) }
    return sum
}

private object EmptyIntTraverser : MutableIntTraverser {
    override fun forward(): Boolean = false
    override val value: Int get() = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
