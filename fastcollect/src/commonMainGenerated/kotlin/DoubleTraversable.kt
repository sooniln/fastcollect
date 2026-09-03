/**
 * Methods for dealing with DoubleTraversables.
 */
@file:JvmName("DoubleTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun emptyDoubleTraverser(): MutableDoubleTraverser = EmptyDoubleTraverser

/**
 * A primitively typed [Traversable] of Doubles.
 */
public interface DoubleTraversable: Traversable<Double> {
    override fun traverser(): DoubleTraverser
}

/**
 * A primitively typed [MutableTraversable] of Doubles.
 */
public interface MutableDoubleTraversable: MutableTraversable<Double> {
    override fun traverser(): MutableDoubleTraverser
}

/**
 * A primitively typed [Traverser] of Doubles.
 *
 * How to iterate with a DoubleTraverser:
 *
 * ```kotlin
 * val traverser = doubleCollection.traverser()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.value)
 * }
 * ```
 */
public interface DoubleTraverser : Traverser<Double> {
    public val value: Double

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.", replaceWith = ReplaceWith("value"))
    @get:JvmSynthetic
    override val element: Double get() = value
}

/**
 * A primitively typed [MutableTraverser] of Doubles.
 */
public interface MutableDoubleTraverser : DoubleTraverser, MutableTraverser<Double>

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.traverse(action: (Double) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.any(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    traverse { if (predicate(it)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.all(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.none(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> DoubleTraversable.fold(initial: R, operation: (accumulated: R, Double) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    traverse { accumulated = operation(accumulated, it) }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.reduce(operation: (accumulated: Double, Double) -> Double): Double {
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
public inline fun DoubleTraversable.sumOf(selector: (Double) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    traverse { sum += selector(it) }
    return sum
}



@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun DoubleTraversable.sumOf(selector: (Double) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    traverse { sum += selector(it) }
    return sum
}


@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
public fun DoubleTraversable.sum(): Double = sumOf { it.toDouble() }


@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun DoubleTraversable.sumOf(selector: (Double) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    traverse { sum += selector(it) }
    return sum
}

private object EmptyDoubleTraverser : MutableDoubleTraverser {
    override fun forward(): Boolean = false
    override val value: Double get() = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
