/**
 * Methods for dealing with IntTraversables.
 */
@file:JvmName("IntTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun IntTraversable.foreach(action: IntConsumer): Unit = foreach { action.accept(it) }
