/**
 * Methods for dealing with ByteTraversables.
 */
@file:JvmName("ByteTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun ByteTraversable.traverse(action: ByteConsumer): Unit = traverse { action.accept(it) }
