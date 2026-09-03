/**
 * Methods for dealing with Int2FloatTraversables.
 */
@file:JvmName("Int2FloatTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2FloatTraversable.traverse(action: IntFloatConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Int2FloatTraversable.traverseKeys(action: IntConsumer): Unit = traverseKeys { key -> action.accept(key) }

