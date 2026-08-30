/**
 * Methods for dealing with Long2LongTraversables.
 */
@file:JvmName("Long2LongTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyLong2LongTraverser(): MutableLong2LongTraverser = EmptyLong2LongTraverser as MutableLong2LongTraverser

/**
 * A primitively typed [Traversable] of Long to Long tuples.
 */
public interface Long2LongTraversable: Traversable<Long2LongMap.Entry> {
    override fun traverser(): Long2LongTraverser
}

/**
 * A primitively typed [MutableTraversable] of Long to Long tuples.
 */
public interface MutableLong2LongTraversable: MutableTraversable<Long2LongMap.Entry>, Long2LongTraversable {
    override fun traverser(): MutableLong2LongTraverser
}

/**
 * A primitively typed [Traverser] of Long to Long tuples.
 *
 * How to iterate with a Long2LongTraverser:
 *
 * ```kotlin
 * val traverser = long2LongMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Long2LongTraverser: Traverser<Long2LongMap.Entry> {
    public val key: Long
    public val value: Long

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2LongMap.Entry get() = AbstractLong2LongMap.SimpleEntry(key, value)
}
/**
 * A primitively typed [MutableTraverser] of Long to Long tuples.
 */
public interface MutableLong2LongTraverser : Long2LongTraverser, MutableTraverser<Long2LongMap.Entry> {
    override var value: Long
}

public fun  Long2LongTraverser.asKeyTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Long get() = key
    }
}


public fun  Long2LongTraverser.asValueTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Long get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.foreach(
    action: (Long, Long) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.foreachKey(
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
public inline fun  Long2LongTraversable.any(predicate: (Long, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.all(predicate: (Long, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.none(predicate: (Long, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Long2LongTraversable.fold(initial: R, operation: (accumulated: R, Long, Long) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2LongTraversable.sumOf(selector: (Long, Long) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2LongTraversable.sumOf(selector: (Long, Long) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2LongTraversable.sumOf(selector: (Long, Long) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2LongTraverser : MutableLong2LongTraverser {

    override fun forward(): Boolean = false
    override val key: Long get() = throw IllegalStateException()

    override var value: Long

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
