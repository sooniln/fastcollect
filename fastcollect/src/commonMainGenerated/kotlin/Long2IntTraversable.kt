/**
 * Methods for dealing with Long2IntTraversables.
 */
@file:JvmName("Long2IntTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun  emptyLong2IntTraverser(): Long2IntTraverser = EmptyLong2IntTraverser

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
 */
public interface Long2IntTraverser: Traverser<Long2IntMap.Entry> {
    public val key: Long
    public val value: Int

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2IntMap.Entry get() = object: Long2IntMap.AbstractEntry() {
        override val key: Long get() = this@Long2IntTraverser.key
        override val value: Int get() = this@Long2IntTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Long to Int tuples.
 */
public interface MutableLong2IntTraverser : Long2IntTraverser, MutableTraverser<Long2IntMap.Entry> {
    override var value: Int

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableLong2IntMap.MutableEntry get() = object: MutableLong2IntMap.AbstractMutableEntry() {
        override val key: Long get() = this@MutableLong2IntTraverser.key
        override var value: Int
            get() = this@MutableLong2IntTraverser.value
            set(value) { this@MutableLong2IntTraverser.value = value }
    }
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

@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntTraversable.any(predicate: (Long, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntTraversable.all(predicate: (Long, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Long2IntTraversable.none(predicate: (Long, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R> Long2IntTraversable.fold(initial: R, operation: (accumulated: R, Long, Int) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2IntTraversable.sumOf(selector: (Long, Int) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2IntTraversable.sumOf(selector: (Long, Int) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2IntTraversable.sumOf(selector: (Long, Int) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2IntTraverser : Long2IntTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
