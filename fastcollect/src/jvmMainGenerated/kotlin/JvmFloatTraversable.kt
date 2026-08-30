/**
 * Methods for dealing with FloatTraversables.
 */
@file:JvmName("FloatTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun FloatTraversable.foreach(action: FloatConsumer): Unit = foreach { action.accept(it) }
