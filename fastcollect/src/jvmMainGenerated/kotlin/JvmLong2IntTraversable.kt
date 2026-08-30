/**
 * Methods for dealing with Long2IntTraversables.
 */
@file:JvmName("Long2IntTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2IntTraversable.foreach(action: LongIntConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Long2IntTraversable.foreachKey(action: LongConsumer): Unit = foreachKey { key -> action.accept(key) }

