/**
 * Callbacks which accept primitively typed values.
 */

package io.github.sooniln.fastcollect

public fun interface ByteConsumer {
    public fun accept(value: Byte)
}

public expect fun interface IntConsumer {
    public fun accept(value: Int)
}

public expect fun interface LongConsumer {
    public fun accept(value: Long)
}

public fun interface FloatConsumer {
    public fun accept(value: Float)
}

public expect fun interface DoubleConsumer {
    public fun accept(value: Double)
}

public fun interface IntByteConsumer {
    public fun accept(key: Int, value: Byte)
}

public fun interface IntIntConsumer {
    public fun accept(key: Int, value: Int)
}

public fun interface IntLongConsumer {
    public fun accept(key: Int, value: Long)
}

public fun interface IntFloatConsumer {
    public fun accept(key: Int, value: Float)
}

public fun interface IntDoubleConsumer {
    public fun accept(key: Int, value: Double)
}

public fun interface IntAnyConsumer<in V> {
    public fun accept(key: Int, value: V)
}

public fun interface LongByteConsumer {
    public fun accept(key: Long, value: Byte)
}

public fun interface LongIntConsumer {
    public fun accept(key: Long, value: Int)
}

public fun interface LongLongConsumer {
    public fun accept(key: Long, value: Long)
}

public fun interface LongFloatConsumer {
    public fun accept(key: Long, value: Float)
}

public fun interface LongDoubleConsumer {
    public fun accept(key: Long, value: Double)
}

public fun interface LongAnyConsumer<in V> {
    public fun accept(key: Long, value: V)
}
