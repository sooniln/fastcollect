/**
 * Tests which accept primitively typed values.
 */

package io.github.sooniln.fastcollect

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
