/**
 * Methods for dealing with ByteTraversables.
 */
@file:JvmName("ByteTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun ByteTraversable.foreach(action: ByteConsumer): Unit = foreach { action.accept(it) }
