/**
 * Methods for dealing with Int2AnyTraversables.
 */
@file:JvmName("Int2AnyTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun <V> Int2AnyTraversable<V>.traverse(action: IntAnyConsumer<V>): Unit = traverse { key, value -> action.accept(key, value) }

public fun <V> Int2AnyTraversable<V>.traverseKeys(action: IntConsumer): Unit = traverseKeys { key -> action.accept(key) }

