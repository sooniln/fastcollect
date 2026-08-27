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

public fun  emptyLong2LongTraverser(): Long2LongTraverser = EmptyLong2LongTraverser

/**
 * A primitively typed [Traversable] of Long to Long tuples.
 */
public interface Long2LongTraversable: Traversable<Long2LongMap.Entry> {
    override fun traverser(): Long2LongTraverser
}

/**
 * A primitively typed [MutableTraversable] of Long to Long tuples.
 */
public interface MutableLong2LongTraversable: MutableTraversable<Long2LongMap.Entry> {
    override fun traverser(): MutableLong2LongTraverser
}

/**
 * A primitively typed [Traverser] of Long to Long tuples.
 */
public interface Long2LongTraverser: Traverser<Long2LongMap.Entry> {
    public val key: Long
    public val value: Long

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2LongMap.Entry get() = object: Long2LongMap.AbstractEntry() {
        override val key: Long get() = this@Long2LongTraverser.key
        override val value: Long get() = this@Long2LongTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Long to Long tuples.
 */
public interface MutableLong2LongTraverser : Long2LongTraverser, MutableTraverser<Long2LongMap.Entry> {
    override var value: Long

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableLong2LongMap.MutableEntry get() = object: MutableLong2LongMap.AbstractMutableEntry() {
        override val key: Long get() = this@MutableLong2LongTraverser.key
        override var value: Long
            get() = this@MutableLong2LongTraverser.value
            set(value) { this@MutableLong2LongTraverser.value = value }
    }
}

public fun  Long2LongTraverser.asKeyTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Long get() = key
    }
}


public fun  Long2LongTraverser.asValueTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Long get() = this@asValueTraverser.value
    }
}


@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.foreach(
    action: (Long, Long) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.foreachKey(
    action: (Long) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.any(predicate: (Long, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.all(predicate: (Long, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Long2LongTraversable.none(predicate: (Long, Long) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R> Long2LongTraversable.fold(initial: R, operation: (accumulated: R, Long, Long) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2LongTraversable.sumOf(selector: (Long, Long) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2LongTraversable.sumOf(selector: (Long, Long) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2LongTraversable.sumOf(selector: (Long, Long) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2LongTraverser : Long2LongTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
