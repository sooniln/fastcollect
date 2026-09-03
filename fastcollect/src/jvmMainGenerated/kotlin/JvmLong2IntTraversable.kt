/**
 * Methods for dealing with Long2IntTraversables.
 */
@file:JvmName("Long2IntTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2IntTraversable.traverse(action: LongIntConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Long2IntTraversable.traverseKeys(action: LongConsumer): Unit = traverseKeys { key -> action.accept(key) }

