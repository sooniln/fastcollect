/**
 * Methods for dealing with Int2FloatTraversables.
 */
@file:JvmName("Int2FloatTraversables")
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
public fun  emptyInt2FloatTraverser(): MutableInt2FloatTraverser = EmptyInt2FloatTraverser as MutableInt2FloatTraverser

/**
 * A primitively typed [Traversable] of Int to Float tuples.
 */
public interface Int2FloatTraversable: Traversable<Int2FloatMap.Entry> {
    override fun traverser(): Int2FloatTraverser
}

/**
 * A primitively typed [MutableTraversable] of Int to Float tuples.
 */
public interface MutableInt2FloatTraversable: MutableTraversable<Int2FloatMap.Entry>, Int2FloatTraversable {
    override fun traverser(): MutableInt2FloatTraverser
}

/**
 * A primitively typed [Traverser] of Int to Float tuples.
 *
 * How to iterate with a Int2FloatTraverser:
 *
 * ```kotlin
 * val traverser = int2FloatMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Int2FloatTraverser: Traverser<Int2FloatMap.Entry> {
    public val key: Int
    public val value: Float

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2FloatMap.Entry get() = AbstractInt2FloatMap.SimpleEntry(key, value)
}
/**
 * A primitively typed [MutableTraverser] of Int to Float tuples.
 */
public interface MutableInt2FloatTraverser : Int2FloatTraverser, MutableTraverser<Int2FloatMap.Entry> {
    override var value: Float
}

public fun  Int2FloatTraverser.asKeyTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Int get() = key
    }
}


public fun  Int2FloatTraverser.asValueTraverser(): FloatTraverser {
    return object: FloatTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Float get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatTraversable.foreach(
    action: (Int, Float) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatTraversable.foreachKey(
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
public inline fun  Int2FloatTraversable.any(predicate: (Int, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatTraversable.all(predicate: (Int, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatTraversable.none(predicate: (Int, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2FloatTraversable.fold(initial: R, operation: (accumulated: R, Int, Float) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2FloatTraversable.sumOf(selector: (Int, Float) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2FloatTraversable.sumOf(selector: (Int, Float) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2FloatTraversable.sumOf(selector: (Int, Float) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2FloatTraverser : MutableInt2FloatTraverser {

    override fun forward(): Boolean = false
    override val key: Int get() = throw IllegalStateException()

    override var value: Float

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
