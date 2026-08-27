/**
 * Methods for dealing with DoubleTraversables.
 */
@file:JvmName("DoubleTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
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

@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.foreach(action: (Double) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.any(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { if (predicate(it)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.all(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleTraversable.none(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> DoubleTraversable.fold(initial: R, operation: (accumulated: R, Double) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { accumulated = operation(accumulated, it) }
    return accumulated
}

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



@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun DoubleTraversable.sumOf(selector: (Double) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { sum += selector(it) }
    return sum
}



@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun DoubleTraversable.sumOf(selector: (Double) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { sum += selector(it) }
    return sum
}


@Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")
public fun DoubleTraversable.sum(): Double = sumOf { it.toDouble() }


@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun DoubleTraversable.sumOf(selector: (Double) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { sum += selector(it) }
    return sum
}

private object EmptyDoubleTraverser : MutableDoubleTraverser {
    override fun forward(): Boolean = false
    override val value: Double get() = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
