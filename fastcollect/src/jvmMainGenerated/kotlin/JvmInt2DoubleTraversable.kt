/**
 * Methods for dealing with Int2DoubleTraversables.
 */
@file:JvmName("Int2DoubleTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2DoubleTraversable.foreach(action: IntDoubleConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Int2DoubleTraversable.foreachKey(action: IntConsumer): Unit = foreachKey { key -> action.accept(key) }

