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

public fun  emptyInt2IntTraverser(): Int2IntTraverser = EmptyInt2IntTraverser

/**
 * A primitively typed [Traversable] of Int to Int tuples.
 */
public interface Int2IntTraversable: Traversable<Int2IntMap.Entry> {
    override fun traverser(): Int2IntTraverser
}

/**
 * A primitively typed [MutableTraversable] of Int to Int tuples.
 */
public interface MutableInt2IntTraversable: MutableTraversable<Int2IntMap.Entry> {
    override fun traverser(): MutableInt2IntTraverser
}

/**
 * A primitively typed [Traverser] of Int to Int tuples.
 */
public interface Int2IntTraverser: Traverser<Int2IntMap.Entry> {
    public val key: Int
    public val value: Int

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: Int2IntMap.Entry get() = object: Int2IntMap.AbstractEntry() {
        override val key: Int get() = this@Int2IntTraverser.key
        override val value: Int get() = this@Int2IntTraverser.value
    }
}
/**
 * A primitively typed [MutableTraverser] of Int to Int tuples.
 */
public interface MutableInt2IntTraverser : Int2IntTraverser, MutableTraverser<Int2IntMap.Entry> {
    override var value: Int

    /** DO NOT USE. May cause boxing. */
    @Deprecated(level = DeprecationLevel.HIDDEN, message = "May cause boxing.")
    @get:JvmSynthetic
    override val element: MutableInt2IntMap.MutableEntry get() = object: MutableInt2IntMap.AbstractMutableEntry() {
        override val key: Int get() = this@MutableInt2IntTraverser.key
        override var value: Int
            get() = this@MutableInt2IntTraverser.value
            set(value) { this@MutableInt2IntTraverser.value = value }
    }
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

@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntTraversable.any(predicate: (Int, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    foreach { key, value -> if (predicate(key, value)) return true }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntTraversable.all(predicate: (Int, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { key, value -> !predicate(key, value) }
}

@OptIn(ExperimentalContracts::class)
public inline fun  Int2IntTraversable.none(predicate: (Int, Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)

public inline fun <R> Int2IntTraversable.fold(initial: R, operation: (accumulated: R, Int, Int) -> R): R {

    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    foreach { key, value -> accumulated = operation(accumulated, key, value) }
    return accumulated
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2IntTraversable.sumOf(selector: (Int, Int) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2IntTraversable.sumOf(selector: (Int, Int) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0L
    foreach { key, value -> sum += selector(key, value) }
    return sum
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
@OverloadResolutionByLambdaReturnType
public inline fun  Int2IntTraversable.sumOf(selector: (Int, Int) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    var sum = 0.0
    foreach { key, value -> sum += selector(key, value) }
    return sum
}


private object EmptyInt2IntTraverser : Int2IntTraverser {

    override fun forward(): Boolean = false
    override val key: Nothing get() = throw IllegalStateException()
    override val value: Nothing get() = throw IllegalStateException()
}
