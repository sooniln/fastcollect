/**
 * Methods for dealing with Int2AnyTraversables.
 */
@file:JvmName("Int2AnyTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun <V> Int2AnyTraversable<V>.foreach(action: IntAnyConsumer<V>): Unit = foreach { key, value -> action.accept(key, value) }

public fun <V> Int2AnyTraversable<V>.foreachKey(action: IntConsumer): Unit = foreachKey { key -> action.accept(key) }

