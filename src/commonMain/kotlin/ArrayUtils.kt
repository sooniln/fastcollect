package io.github.sooniln.fastcollect

import kotlin.math.max

public object ArrayUtils {

    /**
     * A soft maximum array length imposed by array growth computations. Some JVMs (such as HotSpot) have an
     * implementation limit that will cause OutOfMemoryError to be thrown if a request is made to allocate an array of
     * some length near Integer.MAX_VALUE, even if there is sufficient heap available. The actual limit might depend on
     * some JVM implementation-specific characteristics such as the object header size. The soft maximum value is chosen
     * conservatively to be smaller than any implementation limit that is likely to be encountered.
     */
    private const val SOFT_MAX_ARRAY_SIZE = Int.MAX_VALUE - 8

    public fun growArraySize(oldSize: Int, minGrowth: Int, prefGrowth: Int = oldSize shr 1): Int {
        require(minGrowth > 0)
        val prefSize = oldSize + max(minGrowth, prefGrowth) // might overflow
        return if (prefSize in 1..SOFT_MAX_ARRAY_SIZE) {
            prefSize
        } else {
            // cold path in a separate method
            hugeSize(oldSize, minGrowth)
        }
    }

    private fun hugeSize(oldSize: Int, minGrowth: Int): Int {
        val minSize = oldSize + minGrowth
        return if (minSize < 0) {
            // TODO: why can't OOME be used?
            throw Error("Required array length $oldSize + $minGrowth is too large")
        } else if (minSize <= SOFT_MAX_ARRAY_SIZE) {
            SOFT_MAX_ARRAY_SIZE
        } else {
            minSize
        }
    }
}