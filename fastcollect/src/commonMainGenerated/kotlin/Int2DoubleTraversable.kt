/**
 * Methods for dealing with Int2DoubleTraversables.
 */
@file:JvmName("Int2DoubleTraversables")
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
public fun  emptyInt2DoubleTraverser(): MutableInt2DoubleTraverser = EmptyInt2DoubleTraverser as MutableInt2DoubleTraverser

/**
 * A primitively typed [Traversable] of Int to Double tuples.
 */
public interface Int2DoubleTraversable: Traversable<Int2DoubleMap.Entry> {
    override fun traverser(): Int2DoubleTraverser
}

/**
 * A primitively typed [MutableTraversable] of Int to Double tuples.
 */
public interface MutableInt2DoubleTraversable: MutableTraversable<Int2DoubleMap.Entry>, Int2DoubleTraversable {
    override fun traverser(): MutableInt2DoubleTraverser
}

/**
 * A primitively typed [Traverser] of Int to Double tuples.
 *
 * How to iterate with a Int2DoubleTraverser:
 *
 * ```kotlin
 * val traverser = int2DoubleMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Int2DoubleTraverser: Traverser<Int2DoubleMap.Entry> {
    public val key: Int
    public val value: Double

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2DoubleMap.Entry get() = AbstractInt2DoubleMap.SimpleEntry(key, value)
}
/**
 * A primitively typed [MutableTraverser] of Int to Double tuples.
 */
public interface MutableInt2DoubleTraverser : Int2DoubleTraverser, MutableTraverser<Int2DoubleMap.Entry> {
    override var value: Double
}

public fun  Int2DoubleTraverser.asKeyTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Int get() = key
    }
}


public fun  Int2DoubleTraverser.asValueTraverser(): DoubleTraverser {
    return object: DoubleTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Double get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleTraversable.foreach(
    action: (Int, Double) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleTraversable.foreachKey(
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
public inline fun  Int2DoubleTraversable.any(predicate: (Int, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleTraversable.all(predicate: (Int, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleTraversable.none(predicate: (Int, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2DoubleTraversable.fold(initial: R, operation: (accumulated: R, Int, Double) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2DoubleTraversable.sumOf(selector: (Int, Double) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2DoubleTraversable.sumOf(selector: (Int, Double) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2DoubleTraversable.sumOf(selector: (Int, Double) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2DoubleTraverser : MutableInt2DoubleTraverser {

    override fun forward(): Boolean = false
    override val key: Int get() = throw IllegalStateException()

    override var value: Double

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
