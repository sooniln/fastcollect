/**
 * Methods for dealing with Long2AnyTraversables.
 */
@file:JvmName("Long2AnyTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun <V> Long2AnyTraversable<V>.foreach(action: LongAnyConsumer<V>): Unit = foreach { key, value -> action.accept(key, value) }

public fun <V> Long2AnyTraversable<V>.foreachKey(action: LongConsumer): Unit = foreachKey { key -> action.accept(key) }

