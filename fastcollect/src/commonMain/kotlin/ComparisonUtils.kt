package io.github.sooniln.fastcollect

// equalsBoxed implements comparisons that matches the semantics of boxed values on the JVM (notably NaN == NaN)

public infix fun Byte.equalsBoxed(other: Byte): Boolean = this == other

public infix fun Short.equalsBoxed(other: Short): Boolean = this == other

public infix fun Int.equalsBoxed(other: Int): Boolean = this == other

public infix fun Long.equalsBoxed(other: Long): Boolean = this == other

public infix fun Float.equalsBoxed(other: Float): Boolean = toBits() == other.toBits()

public infix fun Double.equalsBoxed(other: Double): Boolean = toBits() == other.toBits()

public infix fun Any?.equalsBoxed(other: Any?): Boolean = this == other
