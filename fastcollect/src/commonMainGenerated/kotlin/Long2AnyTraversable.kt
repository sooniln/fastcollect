/**
 * Methods for dealing with Long2AnyTraversables.
 */
@file:JvmName("Long2AnyTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun <V> emptyLong2AnyTraverser(): Long2AnyTraverser<V> = EmptyLong2AnyTraverser

/**
 * A primitively typed [Traversable] of Long to V tuples.
 */
public interface Long2AnyTraversable<out V>: Traversable<Long2AnyMap.Entry<V>> {
    override fun traverser(): Long2AnyTraverser<V>
}

/**
 * A primitively typed [MutableTraversable] of Long to V tuples.
 */
public interface MutableLong2AnyTraversable<V>: MutableTraversable<Long2AnyMap.Entry<V>>, Long2AnyTraversable<V> {
    override fun traverser(): MutableLong2AnyTraverser<V>
}

/**
 * A primitively typed [Traverser] of Long to V tuples.
 */
public interface Long2AnyTraverser<out V>: Traverser<Long2AnyMap.Entry<V>> {
    public val key: Long
    public val value: V

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2AnyMap.Entry<V> get() = object: Long2AnyMap.AbstractEntry<V>() {
        override val key: Long get() = this@Long2AnyTraverser.key
        override val value: V get() = this@Long2AnyTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Long to V tuples.
 */
public interface MutableLong2AnyTraverser<V> : Long2AnyTraverser<V>, MutableTraverser<Long2AnyMap.Entry<V>> {
    override var value: V

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableLong2AnyMap.MutableEntry<V> get() = object: MutableLong2AnyMap.AbstractMutableEntry<V>() {
        override val key: Long get() = this@MutableLong2AnyTraverser.key
        override var value: V
            get() = this@MutableLong2AnyTraverser.value
            set(value) { this@MutableLong2AnyTraverser.value = value }
    }
}

public fun <V> Long2AnyTraverser<V>.asKeyTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Long get() = key
    }
}



@OptIn(ExperimentalContracts::class)
public inline fun <V> Long2AnyTraversable<V>.foreach(
    action: (Long, V) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> Long2AnyTraversable<V>.foreachKey(
    action: (Long) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> Long2AnyTraversable<V>.any(predicate: (Long, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> Long2AnyTraversable<V>.all(predicate: (Long, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> Long2AnyTraversable<V>.none(predicate: (Long, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R, V> Long2AnyTraversable<V>.fold(initial: R, operation: (accumulated: R, Long, V) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Long2AnyTraversable<V>.sumOf(selector: (Long, V) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Long2AnyTraversable<V>.sumOf(selector: (Long, V) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Long2AnyTraversable<V>.sumOf(selector: (Long, V) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2AnyTraverser : Long2AnyTraverser<Nothing> {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
