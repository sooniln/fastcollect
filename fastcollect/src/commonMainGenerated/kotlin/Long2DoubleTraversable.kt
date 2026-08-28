/**
 * Methods for dealing with Long2DoubleTraversables.
 */
@file:JvmName("Long2DoubleTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST", "UNNECESSARY_CAST")
public fun  emptyLong2DoubleTraverser(): MutableLong2DoubleTraverser = EmptyLong2DoubleTraverser as MutableLong2DoubleTraverser

/**
 * A primitively typed [Traversable] of Long to Double tuples.
 */
public interface Long2DoubleTraversable: Traversable<Long2DoubleMap.Entry> {
    override fun traverser(): Long2DoubleTraverser
}

/**
 * A primitively typed [MutableTraversable] of Long to Double tuples.
 */
public interface MutableLong2DoubleTraversable: MutableTraversable<Long2DoubleMap.Entry>, Long2DoubleTraversable {
    override fun traverser(): MutableLong2DoubleTraverser
}

/**
 * A primitively typed [Traverser] of Long to Double tuples.
 *
 * How to iterate with a Long2DoubleTraverser:
 *
 * ```kotlin
 * val traverser = long2DoubleMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Long2DoubleTraverser: Traverser<Long2DoubleMap.Entry> {
    public val key: Long
    public val value: Double

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2DoubleMap.Entry get() = object: Long2DoubleMap.AbstractEntry() {
        override val key: Long get() = this@Long2DoubleTraverser.key
        override val value: Double get() = this@Long2DoubleTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Long to Double tuples.
 */
public interface MutableLong2DoubleTraverser : Long2DoubleTraverser, MutableTraverser<Long2DoubleMap.Entry> {
    override var value: Double

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2DoubleMap.Entry get() = object: Long2DoubleMap.AbstractEntry() {
        override val key: Long = this@MutableLong2DoubleTraverser.key
        override val value: Double = this@MutableLong2DoubleTraverser.value
    }
}

public fun  Long2DoubleTraverser.asKeyTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Long get() = key
    }
}


public fun  Long2DoubleTraverser.asValueTraverser(): DoubleTraverser {
    return object: DoubleTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Double get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2DoubleTraversable.foreach(
    action: (Long, Double) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2DoubleTraversable.foreachKey(
    action: (Long) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2DoubleTraversable.any(predicate: (Long, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2DoubleTraversable.all(predicate: (Long, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2DoubleTraversable.none(predicate: (Long, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Long2DoubleTraversable.fold(initial: R, operation: (accumulated: R, Long, Double) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2DoubleTraversable.sumOf(selector: (Long, Double) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2DoubleTraversable.sumOf(selector: (Long, Double) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2DoubleTraversable.sumOf(selector: (Long, Double) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2DoubleTraverser : MutableLong2DoubleTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()

    override var value: Double

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
