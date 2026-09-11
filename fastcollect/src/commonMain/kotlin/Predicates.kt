/**
 * Tests which accept primitively typed values.
 */
@file:JvmName("Predicates")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

public fun interface BytePredicate {
    public fun test(value: Byte): Boolean
}

public expect fun interface IntPredicate {
    public fun test(value: Int): Boolean
}

public expect fun interface LongPredicate {
    public fun test(value: Long): Boolean
}

public fun interface FloatPredicate {
    public fun test(value: Float): Boolean
}

public expect fun interface DoublePredicate {
    public fun test(value: Double): Boolean
}

@JvmSynthetic
public operator fun BytePredicate.invoke(value: Byte): Boolean = test(value)

@JvmSynthetic
public operator fun IntPredicate.invoke(value: Int): Boolean = test(value)

@JvmSynthetic
public operator fun LongPredicate.invoke(value: Long): Boolean = test(value)

@JvmSynthetic
public operator fun FloatPredicate.invoke(value: Float): Boolean = test(value)

@JvmSynthetic
public operator fun DoublePredicate.invoke(value: Double): Boolean = test(value)
