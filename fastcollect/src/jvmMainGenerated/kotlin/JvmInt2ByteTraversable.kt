/**
 * Methods for dealing with Int2ByteTraversables.
 */
@file:JvmName("Int2ByteTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2ByteTraversable.foreach(action: IntByteConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Int2ByteTraversable.foreachKey(action: IntConsumer): Unit = foreachKey { key -> action.accept(key) }

