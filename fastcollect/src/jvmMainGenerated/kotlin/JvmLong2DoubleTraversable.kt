/**
 * Methods for dealing with Long2DoubleTraversables.
 */
@file:JvmName("Long2DoubleTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2DoubleTraversable.traverse(action: LongDoubleConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Long2DoubleTraversable.traverseKeys(action: LongConsumer): Unit = traverseKeys { key -> action.accept(key) }

