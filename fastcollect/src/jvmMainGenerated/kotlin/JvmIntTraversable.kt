/**
 * Methods for dealing with IntTraversables.
 */
@file:JvmName("IntTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun IntTraversable.traverse(action: IntConsumer): Unit = traverse { action.accept(it) }
