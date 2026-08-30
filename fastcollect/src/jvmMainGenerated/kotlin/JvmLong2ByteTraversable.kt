/**
 * Methods for dealing with Long2ByteTraversables.
 */
@file:JvmName("Long2ByteTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2ByteTraversable.foreach(action: LongByteConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Long2ByteTraversable.foreachKey(action: LongConsumer): Unit = foreachKey { key -> action.accept(key) }

