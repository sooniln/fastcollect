/**
 * Methods for dealing with Int2ByteTraversables.
 */
@file:JvmName("Int2ByteTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2ByteTraversable.traverse(action: IntByteConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Int2ByteTraversable.traverseKeys(action: IntConsumer): Unit = traverseKeys { key -> action.accept(key) }

