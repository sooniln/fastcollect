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

public fun  emptyInt2FloatTraverser(): Int2FloatTraverser = EmptyInt2FloatTraverser

/**
 * A primitively typed [Traversable] of Int to Float tuples.
 */
public interface Int2FloatTraversable: Traversable<Int2FloatMap.Entry> {
    override fun traverser(): Int2FloatTraverser
}

/**
 * A primitively typed [MutableTraversable] of Int to Float tuples.
 */
public interface MutableInt2FloatTraversable: MutableTraversable<Int2FloatMap.Entry> {
    override fun traverser(): MutableInt2FloatTraverser
}

/**
 * A primitively typed [Traverser] of Int to Float tuples.
 */
public interface Int2FloatTraverser: Traverser<Int2FloatMap.Entry> {
    public val key: Int
    public val value: Float

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2FloatMap.Entry get() = object: Int2FloatMap.AbstractEntry() {
        override val key: Int get() = this@Int2FloatTraverser.key
        override val value: Float get() = this@Int2FloatTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Int to Float tuples.
 */
public interface MutableInt2FloatTraverser : Int2FloatTraverser, MutableTraverser<Int2FloatMap.Entry> {
    override var value: Float

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableInt2FloatMap.MutableEntry get() = object: MutableInt2FloatMap.AbstractMutableEntry() {
        override val key: Int get() = this@MutableInt2FloatTraverser.key
        override var value: Float
            get() = this@MutableInt2FloatTraverser.value
            set(value) { this@MutableInt2FloatTraverser.value = value }
    }
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

@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatTraversable.any(predicate: (Int, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatTraversable.all(predicate: (Int, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2FloatTraversable.none(predicate: (Int, Float) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2FloatTraversable.fold(initial: R, operation: (accumulated: R, Int, Float) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2FloatTraversable.sumOf(selector: (Int, Float) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2FloatTraversable.sumOf(selector: (Int, Float) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2FloatTraversable.sumOf(selector: (Int, Float) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2FloatTraverser : Int2FloatTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
