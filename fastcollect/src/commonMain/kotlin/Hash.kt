/**
 * Methods for dealing with primitive hashes.
 */
@file:JvmName("Hashes")

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmName

internal object Hash {
    private const val INT_PHI: Int = -0x61C88647 // == 0x9E3779B9
    private const val LONG_PHI: Long = -0x61C8864680B583EBL // == 0x9E3779B97F4A7C15L

    fun mix(k: Int): Int = ((k xor (k ushr 16)) * INT_PHI).reverseBytes()
    fun mix(k: Long): Int = (((k xor (k ushr 32)) * LONG_PHI) ushr 32).toInt().reverseBytes()
    fun mix(k: Float): Int = mix(k.toBits())
    fun mix(k: Double): Int = mix(k.toBits())
}

public expect fun Int.reverseBits(): Int

public expect fun Int.reverseBytes(): Int
