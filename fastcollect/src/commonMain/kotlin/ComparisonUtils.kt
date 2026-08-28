/**
 * Methods for dealing with comparisons.
 */
@file:JvmName("ComparisonUtils")

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmName

// equalsRaw implements comparisons via raw bit values - more closely (but not exactly) matches the semantics of
// boxed values on the JVM

public infix fun Byte.equalsRaw(other: Byte): Boolean = this == other

public infix fun Int.equalsRaw(other: Int): Boolean = this == other

public infix fun Long.equalsRaw(other: Long): Boolean = this == other

public infix fun Float.equalsRaw(other: Float): Boolean = toRawBits() == other.toRawBits()

public infix fun Double.equalsRaw(other: Double): Boolean = toRawBits() == other.toRawBits()

public infix fun Any?.equalsRaw(other: Any?): Boolean = this == other

public infix fun Byte.notEqualsRaw(other: Byte): Boolean = this != other

public infix fun Int.notEqualsRaw(other: Int): Boolean = this != other

public infix fun Long.notEqualsRaw(other: Long): Boolean = this != other

public infix fun Float.notEqualsRaw(other: Float): Boolean = toRawBits() != other.toRawBits()

public infix fun Double.notEqualsRaw(other: Double): Boolean = toRawBits() != other.toRawBits()

public infix fun Any?.notEqualsRaw(other: Any?): Boolean = this != other
