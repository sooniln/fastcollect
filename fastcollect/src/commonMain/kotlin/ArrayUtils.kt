/**
 * Methods for dealing with comparisons.
 */
@file:JvmName("ArrayUtils")

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.math.max

/**
 * A soft maximum array length imposed by array growth computations. Some JVMs (such as HotSpot) have an
 * implementation limit that will cause OutOfMemoryError to be thrown if a request is made to allocate an array of
 * some length near Integer.MAX_VALUE, even if there is sufficient heap available. The actual limit might depend on
 * some JVM implementation-specific characteristics such as the object header size. The soft maximum value is chosen
 * conservatively to be smaller than any implementation limit that is likely to be encountered.
 */
private const val SOFT_MAX_ARRAY_SIZE = Int.MAX_VALUE - 8

/**
 * Returns a new array size larger than the given old array size. Accepts two parameters, the minimum allowed growth
 * and the preferred growth. Will attempt to grow by the preferred amount if possible, but will always grow by at
 * least the minimum amount.
 */
internal fun growArraySize(oldSize: Int, minGrowth: Int, prefGrowth: Int = oldSize shr 1): Int {
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
        // why does kotlin have no multiplatform OOME?
        throw Error("Required array length $oldSize + $minGrowth is too large")
    } else if (minSize <= SOFT_MAX_ARRAY_SIZE) {
        SOFT_MAX_ARRAY_SIZE
    } else {
        minSize
    }
}

private const val MAXIMUM_CAPACITY: Int = 1 shl 30

internal fun minPowerOfTwo(cap: Int): Int {
    val n = -1 ushr (cap - 1).countLeadingZeroBits()
    return if (n < 0) 1 else if (n >= MAXIMUM_CAPACITY) MAXIMUM_CAPACITY else n + 1
}

@JvmOverloads
public fun BooleanArray.sort(fromIndex: Int = 0, toIndex: Int = size) {
    require(fromIndex in 0..toIndex && toIndex <= size)
    var index = fromIndex
    var falseIndex = index
    while (index < toIndex) {
        if (!get(index)) {
            if (index != falseIndex) {
                set(falseIndex, false)
            }
            ++falseIndex
        }
        ++index
    }
    while (falseIndex < index) {
        set(falseIndex++, true)
    }
}

@JvmOverloads
public fun BooleanArray.sortDescending(fromIndex: Int = 0, toIndex: Int = size) {
    require(fromIndex in 0..toIndex && toIndex <= size)
    var index = fromIndex
    var trueIndex = index
    while (index < toIndex) {
        if (get(index)) {
            if (index != trueIndex) {
                set(trueIndex, true)
            }
            ++trueIndex
        }
        ++index
    }
    while (trueIndex < index) {
        set(trueIndex++, false)
    }
}
