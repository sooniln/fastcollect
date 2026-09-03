/**
 * Methods for dealing with Int2LongTraversables.
 */
@file:JvmName("Int2LongTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2LongTraversable.traverse(action: IntLongConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Int2LongTraversable.traverseKeys(action: IntConsumer): Unit = traverseKeys { key -> action.accept(key) }

