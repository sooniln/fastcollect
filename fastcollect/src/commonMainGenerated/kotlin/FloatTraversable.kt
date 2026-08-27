/**
 * Methods for dealing with Traversables.
 */
@file:JvmName("Traversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun emptyFloatTraverser(): MutableFloatTraverser = EmptyFloatTraverser

/**
 * A primitively typed [Traversable] of Floats.
 */
public interface FloatTraversable: Traversable<Float> {
    override fun traverser(): FloatTraverser
}

/**
 * A primitively typed [MutableTraversable] of Floats.
 */
public interface MutableFloatTraversable: MutableTraversable<Float> {
    override fun traverser(): MutableFloatTraverser
}

/**
 * A primitively typed [Traverser] of Floats.
 */
public interface FloatTraverser : Traverser<Float> {
    public val value: Float

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.", replaceWith = ReplaceWith("value"))
    @get:JvmSynthetic
    override val element: Float get() = value
}

/**
 * A primitively typed [MutableTraverser] of Floats.
 */
public interface MutableFloatTraverser : FloatTraverser, MutableTraverser<Float>

@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.foreach(action: (Float) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.any(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { if (predicate(it)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.all(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.none(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> FloatTraversable.fold(initial: R, operation: (accumulated: R, Float) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { accumulated = operation(accumulated, it) }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.reduce(operation: (accumulated: Float, Float) -> Float): Float {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    if (!traverser.forward()) throw NoSuchElementException()
    var accumulated = traverser.value
    while (traverser.forward()) {
        accumulated = operation(accumulated, traverser.value)
    }
    return accumulated
}



@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun FloatTraversable.sumOf(selector: (Float) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { sum += selector(it) }
    return sum
}



@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun FloatTraversable.sumOf(selector: (Float) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { sum += selector(it) }
    return sum
}


@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
public fun FloatTraversable.sum(): Double = sumOf { it.toDouble() }


@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun FloatTraversable.sumOf(selector: (Float) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { sum += selector(it) }
    return sum
}

private object EmptyFloatTraverser : MutableFloatTraverser {
    override fun forward(): Boolean = false
    override val value: Float get() = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
