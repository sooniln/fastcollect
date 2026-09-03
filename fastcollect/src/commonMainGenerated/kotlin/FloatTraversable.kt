/**
 * Methods for dealing with FloatTraversables.
 */
@file:JvmName("FloatTraversables")
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
 *
 * How to iterate with a FloatTraverser:
 *
 * ```kotlin
 * val traverser = floatCollection.traverser()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.value)
 * }
 * ```
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

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.traverse(action: (Float) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.any(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    traverse { if (predicate(it)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.all(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun FloatTraversable.none(predicate: (Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> FloatTraversable.fold(initial: R, operation: (accumulated: R, Float) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    traverse { accumulated = operation(accumulated, it) }
    return accumulated
}

@JvmSynthetic
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



@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun FloatTraversable.sumOf(selector: (Float) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    traverse { sum += selector(it) }
    return sum
}



@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun FloatTraversable.sumOf(selector: (Float) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    traverse { sum += selector(it) }
    return sum
}


@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
public fun FloatTraversable.sum(): Double = sumOf { it.toDouble() }


@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun FloatTraversable.sumOf(selector: (Float) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    traverse { sum += selector(it) }
    return sum
}

private object EmptyFloatTraverser : MutableFloatTraverser {
    override fun forward(): Boolean = false
    override val value: Float get() = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
