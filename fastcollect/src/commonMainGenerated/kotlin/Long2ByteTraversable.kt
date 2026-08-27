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

public fun  emptyLong2ByteTraverser(): Long2ByteTraverser = EmptyLong2ByteTraverser

/**
 * A primitively typed [Traversable] of Long to Byte tuples.
 */
public interface Long2ByteTraversable: Traversable<Long2ByteMap.Entry> {
    override fun traverser(): Long2ByteTraverser
}

/**
 * A primitively typed [MutableTraversable] of Long to Byte tuples.
 */
public interface MutableLong2ByteTraversable: MutableTraversable<Long2ByteMap.Entry> {
    override fun traverser(): MutableLong2ByteTraverser
}

/**
 * A primitively typed [Traverser] of Long to Byte tuples.
 */
public interface Long2ByteTraverser: Traverser<Long2ByteMap.Entry> {
    public val key: Long
    public val value: Byte

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Long2ByteMap.Entry get() = object: Long2ByteMap.AbstractEntry() {
        override val key: Long get() = this@Long2ByteTraverser.key
        override val value: Byte get() = this@Long2ByteTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Long to Byte tuples.
 */
public interface MutableLong2ByteTraverser : Long2ByteTraverser, MutableTraverser<Long2ByteMap.Entry> {
    override var value: Byte

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableLong2ByteMap.MutableEntry get() = object: MutableLong2ByteMap.AbstractMutableEntry() {
        override val key: Long get() = this@MutableLong2ByteTraverser.key
        override var value: Byte
            get() = this@MutableLong2ByteTraverser.value
            set(value) { this@MutableLong2ByteTraverser.value = value }
    }
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

@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteTraversable.any(predicate: (Long, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteTraversable.all(predicate: (Long, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteTraversable.none(predicate: (Long, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R> Long2ByteTraversable.fold(initial: R, operation: (accumulated: R, Long, Byte) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2ByteTraversable.sumOf(selector: (Long, Byte) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2ByteTraversable.sumOf(selector: (Long, Byte) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Long2ByteTraversable.sumOf(selector: (Long, Byte) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyLong2ByteTraverser : Long2ByteTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
