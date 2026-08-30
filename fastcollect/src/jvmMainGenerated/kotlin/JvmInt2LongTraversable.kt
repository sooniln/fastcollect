/**
 * Methods for dealing with Int2LongTraversables.
 */
@file:JvmName("Int2LongTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2LongTraversable.foreach(action: IntLongConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Int2LongTraversable.foreachKey(action: IntConsumer): Unit = foreachKey { key -> action.accept(key) }

