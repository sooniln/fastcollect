/**
 * Methods for dealing with Long2ByteTraversables.
 */
@file:JvmName("Long2ByteTraversables")
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
public fun  emptyLong2ByteTraverser(): MutableLong2ByteTraverser = EmptyLong2ByteTraverser as MutableLong2ByteTraverser

/**
 * A primitively typed [Traversable] of Long to Byte tuples.
 */
public interface Long2ByteTraversable: Traversable<Long2ByteMap.Entry> {
    override fun traverser(): Long2ByteTraverser
}

/**
 * A primitively typed [MutableTraversable] of Long to Byte tuples.
 */
public interface MutableLong2ByteTraversable: MutableTraversable<Long2ByteMap.Entry>, Long2ByteTraversable {
    override fun traverser(): MutableLong2ByteTraverser
}

/**
 * A primitively typed [Traverser] of Long to Byte tuples.
 *
 * How to iterate with a Long2ByteTraverser:
 *
 * ```kotlin
 * val traverser = long2ByteMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Long2ByteTraverser: Traverser<Long2ByteMap.Entry> {
    public val key: Long
    public val value: Byte

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2ByteMap.Entry get() = AbstractLong2ByteMap.SimpleEntry(key, value)
}
/**
 * A primitively typed [MutableTraverser] of Long to Byte tuples.
 */
public interface MutableLong2ByteTraverser : Long2ByteTraverser, MutableTraverser<Long2ByteMap.Entry> {
    override var value: Byte
}

public fun  Long2ByteTraverser.asKeyTraverser(): LongTraverser {
    return object: LongTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Long get() = key
    }
}


public fun  Long2ByteTraverser.asValueTraverser(): ByteTraverser {
    return object: ByteTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Byte get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteTraversable.foreach(
    action: (Long, Byte) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteTraversable.foreachKey(
    action: (Long) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteTraversable.any(predicate: (Long, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteTraversable.all(predicate: (Long, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteTraversable.none(predicate: (Long, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Long2ByteTraversable.fold(initial: R, operation: (accumulated: R, Long, Byte) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2ByteTraversable.sumOf(selector: (Long, Byte) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2ByteTraversable.sumOf(selector: (Long, Byte) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2ByteTraversable.sumOf(selector: (Long, Byte) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2ByteTraverser : MutableLong2ByteTraverser {

    override fun forward(): Boolean = false
    override val key: Long get() = throw IllegalStateException()

    override var value: Byte

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
