/**
 * Methods for dealing with Long2FloatTraversables.
 */
@file:JvmName("Long2FloatTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2FloatTraversable.traverse(action: LongFloatConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Long2FloatTraversable.traverseKeys(action: LongConsumer): Unit = traverseKeys { key -> action.accept(key) }

