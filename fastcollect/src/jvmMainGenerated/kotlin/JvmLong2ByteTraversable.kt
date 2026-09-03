/**
 * Methods for dealing with Long2ByteTraversables.
 */
@file:JvmName("Long2ByteTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Long2ByteTraversable.traverse(action: LongByteConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Long2ByteTraversable.traverseKeys(action: LongConsumer): Unit = traverseKeys { key -> action.accept(key) }

