/**
 * Methods for dealing with Long2FloatTraversables.
 */
@file:JvmName("Long2FloatTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST", "UNNECESSARY_CAST")
public fun  emptyLong2FloatTraverser(): MutableLong2FloatTraverser = EmptyLong2FloatTraverser as MutableLong2FloatTraverser

/**
 * A primitively typed [Traversable] of Long to Float tuples.
 */
public interface Long2FloatTraversable: Traversable<Long2FloatMap.Entry> {
    override fun traverser(): Long2FloatTraverser
}

/**
 * A primitively typed [MutableTraversable] of Long to Float tuples.
 */
public interface MutableLong2FloatTraversable: MutableTraversable<Long2FloatMap.Entry>, Long2FloatTraversable {
    override fun traverser(): MutableLong2FloatTraverser
}

/**
 * A primitively typed [Traverser] of Long to Float tuples.
 *
 * How to iterate with a Long2FloatTraverser:
 *
 * ```kotlin
 * val traverser = long2FloatMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Long2FloatTraverser: Traverser<Long2FloatMap.Entry> {
    public val key: Long
    public val value: Float

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2FloatMap.Entry get() = object: Long2FloatMap.AbstractEntry() {
        override val key: Long get() = this@Long2FloatTraverser.key
        override val value: Float get() = this@Long2FloatTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Long to Float tuples.
 */
public interface MutableLong2FloatTraverser : Long2FloatTraverser, MutableTraverser<Long2FloatMap.Entry> {
    override var value: Float

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2FloatMap.Entry get() = object: Long2FloatMap.AbstractEntry() {
        override val key: Long = this@MutableLong2FloatTraverser.key
        override val value: Float = this@MutableLong2FloatTraverser.value
    }
}

public fun  Long2FloatTraverser.asKeyTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Long get() = key
    }
}


public fun  Long2FloatTraverser.asValueTraverser(): FloatTraverser {
    return object: FloatTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Float get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2FloatTraversable.foreach(
    action: (Long, Float) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2FloatTraversable.foreachKey(
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
public inline fun  Long2FloatTraversable.any(predicate: (Long, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2FloatTraversable.all(predicate: (Long, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2FloatTraversable.none(predicate: (Long, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Long2FloatTraversable.fold(initial: R, operation: (accumulated: R, Long, Float) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2FloatTraversable.sumOf(selector: (Long, Float) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2FloatTraversable.sumOf(selector: (Long, Float) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2FloatTraversable.sumOf(selector: (Long, Float) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2FloatTraverser : MutableLong2FloatTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()

    override var value: Float

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
