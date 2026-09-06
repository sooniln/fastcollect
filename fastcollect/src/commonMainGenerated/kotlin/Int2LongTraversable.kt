/**
 * Methods for dealing with Int2LongTraversables.
 */
@file:JvmName("Int2LongTraversables")
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
public fun  emptyInt2LongTraverser(): MutableInt2LongTraverser = EmptyInt2LongTraverser as MutableInt2LongTraverser

/**
 * A primitively typed [Traversable] of Int to Long tuples.
 */
public interface Int2LongTraversable: Traversable<Int2LongMap.Entry> {
    override fun traverser(): Int2LongTraverser
}

/**
 * A primitively typed [MutableTraversable] of Int to Long tuples.
 */
public interface MutableInt2LongTraversable: MutableTraversable<Int2LongMap.Entry>, Int2LongTraversable {
    override fun traverser(): MutableInt2LongTraverser
}

/**
 * A primitively typed [Traverser] of Int to Long tuples.
 *
 * How to iterate with a Int2LongTraverser:
 *
 * ```kotlin
 * val traverser = int2LongMap.traverser()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Int2LongTraverser: Traverser<Int2LongMap.Entry> {
    public val key: Int
    public val value: Long

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.ERROR, message = "May cause boxing.")
    override val element: Int2LongMap.Entry get() = AbstractInt2LongMap.SimpleEntry(key, value)
}
/**
 * A primitively typed [MutableTraverser] of Int to Long tuples.
 */
public interface MutableInt2LongTraverser : Int2LongTraverser, MutableTraverser<Int2LongMap.Entry> {
    override var value: Long
}

public fun  Int2LongTraverser.asKeyTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Int get() = key
    }
}


public fun  Int2LongTraverser.asValueTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Long get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.traverse(
    action: (Int, Long) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.traverseKeys(
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
public inline fun  Int2LongTraversable.any(predicate: (Int, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    traverse { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.all(predicate: (Int, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.none(predicate: (Int, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2LongTraversable.fold(initial: R, operation: (accumulated: R, Int, Long) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    traverse { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2LongTraversable.sumOf(selector: (Int, Long) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    traverse { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2LongTraversable.sumOf(selector: (Int, Long) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    traverse { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2LongTraversable.sumOf(selector: (Int, Long) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    traverse { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2LongTraverser : MutableInt2LongTraverser {

    override fun forward(): Boolean = false
    override val key: Int get() = throw IllegalStateException()

    override var value: Long

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
