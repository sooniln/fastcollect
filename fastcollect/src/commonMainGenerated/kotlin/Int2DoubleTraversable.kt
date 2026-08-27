/**
 * Methods for dealing with Int2DoubleTraversables.
 */
@file:JvmName("Int2DoubleTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun  emptyInt2DoubleTraverser(): Int2DoubleTraverser = EmptyInt2DoubleTraverser

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
 */
public interface Int2DoubleTraverser: Traverser<Int2DoubleMap.Entry> {
    public val key: Int
    public val value: Double

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2DoubleMap.Entry get() = object: Int2DoubleMap.AbstractEntry() {
        override val key: Int get() = this@Int2DoubleTraverser.key
        override val value: Double get() = this@Int2DoubleTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Int to Double tuples.
 */
public interface MutableInt2DoubleTraverser : Int2DoubleTraverser, MutableTraverser<Int2DoubleMap.Entry> {
    override var value: Double

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableInt2DoubleMap.MutableEntry get() = object: MutableInt2DoubleMap.AbstractMutableEntry() {
        override val key: Int get() = this@MutableInt2DoubleTraverser.key
        override var value: Double
            get() = this@MutableInt2DoubleTraverser.value
            set(value) { this@MutableInt2DoubleTraverser.value = value }
    }
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

@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleTraversable.any(predicate: (Int, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleTraversable.all(predicate: (Int, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2DoubleTraversable.none(predicate: (Int, Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2DoubleTraversable.fold(initial: R, operation: (accumulated: R, Int, Double) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2DoubleTraversable.sumOf(selector: (Int, Double) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2DoubleTraversable.sumOf(selector: (Int, Double) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2DoubleTraversable.sumOf(selector: (Int, Double) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2DoubleTraverser : Int2DoubleTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
