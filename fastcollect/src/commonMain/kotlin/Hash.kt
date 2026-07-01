package io.github.sooniln.fastcollect

public object Hash {
    private const val INT_PHI: Int = -0x61C88647 // == 0x9E3779B9
    private const val LONG_PHI: Long = -0x61C8864680B583EBL // == 0x9E3779B97F4A7C15L

    public fun fibonacciHash(k: Int, shift: Int): Int = (k * INT_PHI) ushr shift
    public fun fibonacciHash(k: Long, shift: Int): Int = ((k * LONG_PHI) ushr (32 + shift)).toInt()
}

public expect fun Int.reverseBits(): Int
