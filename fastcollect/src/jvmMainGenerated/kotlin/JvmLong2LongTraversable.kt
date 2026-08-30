/**
 * Methods for dealing with Long2LongTraversables.
 */
@file:JvmName("Long2LongTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2LongTraversable.foreach(action: LongLongConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Long2LongTraversable.foreachKey(action: LongConsumer): Unit = foreachKey { key -> action.accept(key) }

