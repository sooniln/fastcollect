/**
 * Methods for dealing with Int2ByteTraversables.
 */
@file:JvmName("Int2ByteTraversables")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST", "UNNECESSARY_CAST")
public fun  emptyInt2ByteTraverser(): MutableInt2ByteTraverser = EmptyInt2ByteTraverser as MutableInt2ByteTraverser

/**
 * A primitively typed [Traversable] of Int to Byte tuples.
 */
public interface Int2ByteTraversable: Traversable<Int2ByteMap.Entry> {
    override fun traverser(): Int2ByteTraverser
}

/**
 * A primitively typed [MutableTraversable] of Int to Byte tuples.
 */
public interface MutableInt2ByteTraversable: MutableTraversable<Int2ByteMap.Entry>, Int2ByteTraversable {
    override fun traverser(): MutableInt2ByteTraverser
}

/**
 * A primitively typed [Traverser] of Int to Byte tuples.
 *
 * How to iterate with a Int2ByteTraverser:
 *
 * ```kotlin
 * val traverser = int2ByteMap.traverse()
 * while (traverser.forward()) {
 *     doSomethingPrimitive(traverser.key, traverser.value)
 * }
 * ```
 */
public interface Int2ByteTraverser: Traverser<Int2ByteMap.Entry> {
    public val key: Int
    public val value: Byte

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2ByteMap.Entry get() = object: Int2ByteMap.AbstractEntry() {
        override val key: Int get() = this@Int2ByteTraverser.key
        override val value: Byte get() = this@Int2ByteTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Int to Byte tuples.
 */
public interface MutableInt2ByteTraverser : Int2ByteTraverser, MutableTraverser<Int2ByteMap.Entry> {
    override var value: Byte

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2ByteMap.Entry get() = object: Int2ByteMap.AbstractEntry() {
        override val key: Int = this@MutableInt2ByteTraverser.key
        override val value: Byte = this@MutableInt2ByteTraverser.value
    }
}

public fun  Int2ByteTraverser.asKeyTraverser(): IntTraverser {
    return object: IntTraverser {
        override fun forward(): Boolean = this@asKeyTraverser.forward()
        override val value: Int get() = key
    }
}


public fun  Int2ByteTraverser.asValueTraverser(): ByteTraverser {
    return object: ByteTraverser {
        override fun forward(): Boolean = this@asValueTraverser.forward()
        override val value: Byte get() = this@asValueTraverser.value
    }
}


@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2ByteTraversable.foreach(
    action: (Int, Byte) -> Unit
) {
    contract { callsInPlace(action, InvocationKind.UNKNOWN) }

    val traverser = traverser()
    while (traverser.forward()) {
        action(traverser.key, traverser.value)
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2ByteTraversable.foreachKey(
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
public inline fun  Int2ByteTraversable.any(predicate: (Int, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2ByteTraversable.all(predicate: (Int, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Int2ByteTraversable.none(predicate: (Int, Byte) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2ByteTraversable.fold(initial: R, operation: (accumulated: R, Int, Byte) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@JvmSynthetic
@JvmName("intSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2ByteTraversable.sumOf(selector: (Int, Byte) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("longSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2ByteTraversable.sumOf(selector: (Int, Byte) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@JvmSynthetic
@JvmName("doubleSumOf")
@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2ByteTraversable.sumOf(selector: (Int, Byte) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2ByteTraverser : MutableInt2ByteTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()

    override var value: Byte

        get() = throw IllegalStateException()
        set(_) = throw IllegalStateException()
    override fun remove() = throw IllegalStateException()
}
