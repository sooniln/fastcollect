/**
 * Methods for dealing with Int2AnyTraversables.
 */
@file:JvmName("Int2AnyTraversables")
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
public fun <V> emptyInt2AnyTraverser(): MutableInt2AnyTraverser<V> = EmptyInt2AnyTraverser as MutableInt2AnyTraverser<V>

/**
 * A primitively typed [Traversable] of Int to V tuples.
 */
public interface Int2AnyTraversable<out V>: Traversable<Int2AnyMap.Entry<V>> {
    override fun traverser(): Int2AnyTraverser<V>
}

/**
 * A primitively typed [MutableTraversable] of Int to V tuples.
 */
public interface MutableInt2AnyTraversable<V>: MutableTraversable<Int2AnyMap.Entry<V>>, Int2AnyTraversable<V> {
    override fun traverser(): MutableInt2AnyTraverser<V>
}

/**
 * A primitively typed [Traverser] of Int to V tuples.
 *
 * How to iterate with a Int2AnyTraverser:
 *
 * ```kotlin
 * val traverser = int2AnyMap.traverser()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Int2AnyTraverser<out V>: Traverser<Int2AnyMap.Entry<V>> {
    public val key: Int
    public val value: V

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.ERROR, message = "May cause boxing.")
    override val element: Int2AnyMap.Entry<V> get() = AbstractInt2AnyMap.SimpleEntry(key, value)
}
/**
 * A primitively typed [MutableTraverser] of Int to V tuples.
 */
public interface MutableInt2AnyTraverser<V> : Int2AnyTraverser<V>, MutableTraverser<Int2AnyMap.Entry<V>> {
    override var value: V
}

public fun <V> Int2AnyTraverser<V>.asKeyTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Int get() = key
    }
}


public fun <V> Int2AnyTraverser<V>.asValueTraverser(): Traverser<V> {
    return object: Traverser<V> {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val element: V get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.traverse(
    action: (Int, V) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.traverseKeys(
    action: (Int) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.any(predicate: (Int, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    traverse { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.all(predicate: (Int, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.none(predicate: (Int, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R, V> Int2AnyTraversable<V>.fold(initial: R, operation: (accumulated: R, Int, V) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    traverse { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Int2AnyTraversable<V>.sumOf(selector: (Int, V) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    traverse { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Int2AnyTraversable<V>.sumOf(selector: (Int, V) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    traverse { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Int2AnyTraversable<V>.sumOf(selector: (Int, V) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    traverse { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2AnyTraverser : MutableInt2AnyTraverser<Any?> {

    override fun forward(): Boolean = false
    override val key: Int get() = throw IllegalStateException()

    override var value: Any?

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
