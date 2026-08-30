/**
 * Methods for dealing with Int2IntTraversables.
 */
@file:JvmName("Int2IntTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2IntTraversable.foreach(action: IntIntConsumer): Unit = foreach { key, value -> action.accept(key, value) }

public fun  Int2IntTraversable.foreachKey(action: IntConsumer): Unit = foreachKey { key -> action.accept(key) }

