/**
 * Methods for dealing with Long2FloatTraversables.
 */
@file:JvmName("Long2FloatTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2FloatTraversable.foreach(action: LongFloatConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Long2FloatTraversable.foreachKey(action: LongConsumer): Unit = foreachKey { key -> action.accept(key) }

