/**
 * Methods for dealing with Int2LongTraversables.
 */
@file:JvmName("Int2LongTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun  emptyInt2LongTraverser(): Int2LongTraverser = EmptyInt2LongTraverser

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
 */
public interface Int2LongTraverser: Traverser<Int2LongMap.Entry> {
    public val key: Int
    public val value: Long

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2LongMap.Entry get() = object: Int2LongMap.AbstractEntry() {
        override val key: Int get() = this@Int2LongTraverser.key
        override val value: Long get() = this@Int2LongTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Int to Long tuples.
 */
public interface MutableInt2LongTraverser : Int2LongTraverser, MutableTraverser<Int2LongMap.Entry> {
    override var value: Long

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableInt2LongMap.MutableEntry get() = object: MutableInt2LongMap.AbstractMutableEntry() {
        override val key: Int get() = this@MutableInt2LongTraverser.key
        override var value: Long
            get() = this@MutableInt2LongTraverser.value
            set(value) { this@MutableInt2LongTraverser.value = value }
    }
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


@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.foreach(
    action: (Int, Long) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.foreachKey(
    action: (Int) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.any(predicate: (Int, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.all(predicate: (Int, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2LongTraversable.none(predicate: (Int, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2LongTraversable.fold(initial: R, operation: (accumulated: R, Int, Long) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2LongTraversable.sumOf(selector: (Int, Long) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2LongTraversable.sumOf(selector: (Int, Long) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2LongTraversable.sumOf(selector: (Int, Long) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2LongTraverser : Int2LongTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
