/**
 * Methods for dealing with FloatTraversables.
 */
@file:JvmName("FloatTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun FloatTraversable.traverse(action: FloatConsumer): Unit = traverse { action.accept(it) }
