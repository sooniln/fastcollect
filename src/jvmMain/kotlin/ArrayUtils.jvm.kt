package io.github.sooniln.fastcollect

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout
// JAVA_LONG_UNALIGNED is @since 22; withByteAlignment(1) is the Java 21-compatible equivalent.
private val LONG_UNALIGNED: ValueLayout.OfLong = ValueLayout.JAVA_LONG.withByteAlignment(1) as ValueLayout.OfLong

public actual fun IntArray.fastIndexOf(element: Int, fromIndex: Int, toIndex: Int): Int {
    val target = element.toLong() and 0xFFFFFFFFL
    val broadcast = target or (target shl 32)
    val segment = MemorySegment.ofArray(this)

    var i = fromIndex
    while (i < toIndex - 1) {
        val xored = segment.get(LONG_UNALIGNED, i.toLong() * 4L) xor broadcast
        // SWAR zero-detection for 32-bit groups:
        // A group is (approximately) zero when (x-1) has its high bit set and x does not.
        // False positives for the upper group can only occur when the lower group is a true zero,
        // so the explicit re-check below is always correct.
        if ((xored - 0x0000000100000001L) and xored.inv() and -0x7fffffff80000000L != 0L) {
            if (this[i] == element) return i
            return i + 1
        }
        i += 2
    }

    if (i < toIndex && this[i] == element) return i
    return -1
}