/**
 * Methods for dealing with LongTraversables.
 */
@file:JvmName("LongTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun emptyLongTraverser(): MutableLongTraverser = EmptyLongTraverser

/**
 * A primitively typed [Traversable] of Longs.
 */
public interface LongTraversable: Traversable<Long> {
    override fun traverser(): LongTraverser
}

/**
 * A primitively typed [MutableTraversable] of Longs.
 */
public interface MutableLongTraversable: MutableTraversable<Long> {
    override fun traverser(): MutableLongTraverser
}

/**
 * A primitively typed [Traverser] of Longs.
 *
 * How to iterate with a LongTraverser:
 *
 * ```kotlin
 * val traverser = longCollection.traverser()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.value)
 * }
 * ```
 */
public interface LongTraverser : Traverser<Long> {
    public val value: Long

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.ERROR, message = "May cause boxing.", replaceWith = ReplaceWith("value"))
    override val element: Long get() = value
}

/**
 * A primitively typed [MutableTraverser] of Longs.
 */
public interface MutableLongTraverser : LongTraverser, MutableTraverser<Long>

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongTraversable.traverse(action: (Long) -> Unit) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongTraversable.any(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    traverse { if (predicate(it)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongTraversable.all(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongTraversable.none(predicate: (Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> LongTraversable.fold(initial: R, operation: (accumulated: R, Long) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    traverse { accumulated = operation(accumulated, it) }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun LongTraversable.reduce(operation: (accumulated: Long, Long) -> Long): Long {
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
public inline fun LongTraversable.sumOf(selector: (Long) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    traverse { sum += selector(it) }
    return sum
}


public fun LongTraversable.sum(): Long = sumOf { it }


@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun LongTraversable.sumOf(selector: (Long) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    traverse { sum += selector(it) }
    return sum
}



@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun LongTraversable.sumOf(selector: (Long) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    traverse { sum += selector(it) }
    return sum
}

private object EmptyLongTraverser : MutableLongTraverser {
    override fun forward(): Boolean = false
    override val value: Long get() = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
