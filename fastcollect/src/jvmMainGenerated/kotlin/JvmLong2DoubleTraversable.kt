/**
 * Methods for dealing with Long2DoubleTraversables.
 */
@file:JvmName("Long2DoubleTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2DoubleTraversable.foreach(action: LongDoubleConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Long2DoubleTraversable.foreachKey(action: LongConsumer): Unit = foreachKey { key -> action.accept(key) }

