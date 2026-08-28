/**
 * Methods for dealing with Int2IntTraversables.
 */
@file:JvmName("Int2IntTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2IntTraverser(): MutableInt2IntTraverser = EmptyInt2IntTraverser as MutableInt2IntTraverser

/**
 * A primitively typed [Traversable] of Int to Int tuples.
 */
public interface Int2IntTraversable: Traversable<Int2IntMap.Entry> {
    override fun traverser(): Int2IntTraverser
}

/**
 * A primitively typed [MutableTraversable] of Int to Int tuples.
 */
public interface MutableInt2IntTraversable: MutableTraversable<Int2IntMap.Entry>, Int2IntTraversable {
    override fun traverser(): MutableInt2IntTraverser
}

/**
 * A primitively typed [Traverser] of Int to Int tuples.
 *
 * How to iterate with a Int2IntTraverser:
 *
 * ```kotlin
 * val traverser = int2IntMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Int2IntTraverser: Traverser<Int2IntMap.Entry> {
    public val key: Int
    public val value: Int

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2IntMap.Entry get() = AbstractInt2IntMap.SimpleEntry(key, value)
}
/**
 * A primitively typed [MutableTraverser] of Int to Int tuples.
 */
public interface MutableInt2IntTraverser : Int2IntTraverser, MutableTraverser<Int2IntMap.Entry> {
    override var value: Int
}

public fun  Int2IntTraverser.asKeyTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Int get() = key
    }
}


public fun  Int2IntTraverser.asValueTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Int get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntTraversable.foreach(
    action: (Int, Int) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntTraversable.foreachKey(
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
public inline fun  Int2IntTraversable.any(predicate: (Int, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntTraversable.all(predicate: (Int, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntTraversable.none(predicate: (Int, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2IntTraversable.fold(initial: R, operation: (accumulated: R, Int, Int) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2IntTraversable.sumOf(selector: (Int, Int) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2IntTraversable.sumOf(selector: (Int, Int) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2IntTraversable.sumOf(selector: (Int, Int) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2IntTraverser : MutableInt2IntTraverser {

    override fun forward(): Boolean = false
    override val key: Int get() = throw IllegalStateException()

    override var value: Int

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
