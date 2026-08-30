/**
 * Methods for dealing with DoubleTraversables.
 */
@file:JvmName("DoubleTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun DoubleTraversable.foreach(action: DoubleConsumer): Unit = foreach { action.accept(it) }
