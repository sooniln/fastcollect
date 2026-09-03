/**
 * Methods for dealing with Long2LongTraversables.
 */
@file:JvmName("Long2LongTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2LongTraversable.traverse(action: LongLongConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Long2LongTraversable.traverseKeys(action: LongConsumer): Unit = traverseKeys { key -> action.accept(key) }

