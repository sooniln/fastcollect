/**
 * Methods for dealing with Long2AnyTraversables.
 */
@file:JvmName("Long2AnyTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun <V> Long2AnyTraversable<V>.traverse(action: LongAnyConsumer<V>): Unit = traverse { key, value -> action.accept(key, value) }

public fun <V> Long2AnyTraversable<V>.traverseKeys(action: LongConsumer): Unit = traverseKeys { key -> action.accept(key) }

