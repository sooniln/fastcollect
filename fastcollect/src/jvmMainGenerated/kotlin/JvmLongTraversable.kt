/**
 * Methods for dealing with LongTraversables.
 */
@file:JvmName("LongTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun LongTraversable.foreach(action: LongConsumer): Unit = foreach { action.accept(it) }
