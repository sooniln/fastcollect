/**
 * Methods for dealing with Traversables.
 */
@file:JvmName("Traversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Consumer

public fun <T> Traversable<T>.traverse(action: Consumer<T>): Unit = traverse { action.accept(it) }
