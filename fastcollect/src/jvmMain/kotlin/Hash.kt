/**
 * Methods for dealing with primitive hashes.
 */
@file:JvmName("Hashes")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

public actual fun Int.reverseBits(): Int = Integer.reverse(this)

public actual fun Int.reverseBytes(): Int = Integer.reverseBytes(this)
