/**
 * Methods for dealing with Traversables.
 */
@file:JvmName("Traversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun <V> emptyInt2AnyTraverser(): Int2AnyTraverser<V> = EmptyInt2AnyTraverser

/**
 * A primitively typed [Traversable] of Int to V tuples.
 */
public interface Int2AnyTraversable<out V>: Traversable<Int2AnyMap.Entry<V>> {
    override fun traverser(): Int2AnyTraverser<V>
}

/**
 * A primitively typed [MutableTraversable] of Int to V tuples.
 */
public interface MutableInt2AnyTraversable<V>: MutableTraversable<Int2AnyMap.Entry<V>> {
    override fun traverser(): MutableInt2AnyTraverser<V>
}

/**
 * A primitively typed [Traverser] of Int to V tuples.
 */
public interface Int2AnyTraverser<out V>: Traverser<Int2AnyMap.Entry<V>> {
    public val key: Int
    public val value: V

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2AnyMap.Entry<V> get() = object: Int2AnyMap.AbstractEntry<V>() {
        override val key: Int get() = this@Int2AnyTraverser.key
        override val value: V get() = this@Int2AnyTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Int to V tuples.
 */
public interface MutableInt2AnyTraverser<V> : Int2AnyTraverser<V>, MutableTraverser<Int2AnyMap.Entry<V>> {
    override var value: V

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableInt2AnyMap.MutableEntry<V> get() = object: MutableInt2AnyMap.AbstractMutableEntry<V>() {
        override val key: Int get() = this@MutableInt2AnyTraverser.key
        override var value: V
            get() = this@MutableInt2AnyTraverser.value
            set(value) { this@MutableInt2AnyTraverser.value = value }
    }
}

public fun <V> Int2AnyTraverser<V>.asKeyTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Int get() = key
    }
}



@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.foreach(
    action: (Int, V) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.foreachKey(
    action: (Int) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.any(predicate: (Int, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.all(predicate: (Int, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun <V> Int2AnyTraversable<V>.none(predicate: (Int, V) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R, V> Int2AnyTraversable<V>.fold(initial: R, operation: (accumulated: R, Int, V) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Int2AnyTraversable<V>.sumOf(selector: (Int, V) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Int2AnyTraversable<V>.sumOf(selector: (Int, V) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun <V> Int2AnyTraversable<V>.sumOf(selector: (Int, V) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2AnyTraverser : Int2AnyTraverser<Nothing> {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
