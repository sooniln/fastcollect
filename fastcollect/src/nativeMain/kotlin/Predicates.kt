/**
 * Tests which accept primitively typed values.
 */

package io.github.sooniln.fastcollect

public actual fun interface IntPredicate {
    public actual fun test(value: Int): Boolean
}

public actual fun interface LongPredicate {
    public actual fun test(value: Long): Boolean
}

public actual fun interface DoublePredicate {
    public actual fun test(value: Double): Boolean
}
