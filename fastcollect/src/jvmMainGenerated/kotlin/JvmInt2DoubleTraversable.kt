/**
 * Methods for dealing with Int2DoubleTraversables.
 */
@file:JvmName("Int2DoubleTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2DoubleTraversable.traverse(action: IntDoubleConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Int2DoubleTraversable.traverseKeys(action: IntConsumer): Unit = traverseKeys { key -> action.accept(key) }

