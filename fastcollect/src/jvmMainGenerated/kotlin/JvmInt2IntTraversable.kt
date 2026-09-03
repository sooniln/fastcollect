/**
 * Methods for dealing with Int2IntTraversables.
 */
@file:JvmName("Int2IntTraversables")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass


public fun Int2IntTraversable.traverse(action: IntIntConsumer): Unit = traverse { key, value -> action.accept(key, value) }

public fun  Int2IntTraversable.traverseKeys(action: IntConsumer): Unit = traverseKeys { key -> action.accept(key) }

