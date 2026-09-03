/**
 * Methods for dealing with LongTraversables.
 */
@file:JvmName("LongTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun LongTraversable.traverse(action: LongConsumer): Unit = traverse { action.accept(it) }
