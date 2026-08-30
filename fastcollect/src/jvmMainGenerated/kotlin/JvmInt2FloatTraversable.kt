/**
 * Methods for dealing with Int2FloatTraversables.
 */
@file:JvmName("Int2FloatTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2FloatTraversable.foreach(action: IntFloatConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Int2FloatTraversable.foreachKey(action: IntConsumer): Unit = foreachKey { key -> action.accept(key) }

