/**
 * Callbacks which accept primitively typed values.
 */

package io.github.sooniln.fastcollect

public actual fun interface IntConsumer {
    public actual fun accept(value: Int)
}

public actual fun interface LongConsumer {
    public actual fun accept(value: Long)
}

public actual fun interface DoubleConsumer {
    public actual fun accept(value: Double)
}
