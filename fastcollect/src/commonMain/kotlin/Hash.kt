package io.github.sooniln.fastcollect

public object Hash {
    private const val INT_PHI: Int = -0x61C88647 // == 0x9E3779B9
    private const val LONG_PHI: Long = -0x61C8864680B583EBL // == 0x9E3779B97F4A7C15L

    public fun fibonacciHash(k: Int): Int = (k * INT_PHI).reverseBits()
    public fun fibonacciHash(k: Long): Int = ((k * LONG_PHI) ushr 32).toInt().reverseBits()

    public fun fibonacciHashEven(k: Int): Int = (k * INT_PHI).reverseBits() shl 1
    public fun fibonacciHashEven(k: Long): Int = ((k * LONG_PHI) ushr 33).toInt().reverseBits()
}

public expect fun Int.reverseBits(): Int
