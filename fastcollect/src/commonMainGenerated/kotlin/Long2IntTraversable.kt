/**
 * Methods for dealing with Long2IntTraversables.
 */
@file:JvmName("Long2IntTraversables")
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
public fun  emptyLong2IntTraverser(): MutableLong2IntTraverser = EmptyLong2IntTraverser as MutableLong2IntTraverser

/**
 * A primitively typed [Traversable] of Long to Int tuples.
 */
public interface Long2IntTraversable: Traversable<Long2IntMap.Entry> {
    override fun traverser(): Long2IntTraverser
}

/**
 * A primitively typed [MutableTraversable] of Long to Int tuples.
 */
public interface MutableLong2IntTraversable: MutableTraversable<Long2IntMap.Entry>, Long2IntTraversable {
    override fun traverser(): MutableLong2IntTraverser
}

/**
 * A primitively typed [Traverser] of Long to Int tuples.
 *
 * How to iterate with a Long2IntTraverser:
 *
 * ```kotlin
 * val traverser = long2IntMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Long2IntTraverser: Traverser<Long2IntMap.Entry> {
    public val key: Long
    public val value: Int

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2IntMap.Entry get() = AbstractLong2IntMap.SimpleEntry(key, value)
}
/**
 * A primitively typed [MutableTraverser] of Long to Int tuples.
 */
public interface MutableLong2IntTraverser : Long2IntTraverser, MutableTraverser<Long2IntMap.Entry> {
    override var value: Int
}

public fun  Long2IntTraverser.asKeyTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Long get() = key
    }
}


public fun  Long2IntTraverser.asValueTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Int get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntTraversable.foreach(
    action: (Long, Int) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntTraversable.foreachKey(
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
public inline fun  Long2IntTraversable.any(predicate: (Long, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntTraversable.all(predicate: (Long, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntTraversable.none(predicate: (Long, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Long2IntTraversable.fold(initial: R, operation: (accumulated: R, Long, Int) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2IntTraversable.sumOf(selector: (Long, Int) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2IntTraversable.sumOf(selector: (Long, Int) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2IntTraversable.sumOf(selector: (Long, Int) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2IntTraverser : MutableLong2IntTraverser {

    override fun forward(): Boolean = false
    override val key: Long get() = throw IllegalStateException()

    override var value: Int

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
