/**
 * Methods for dealing with primitive Long2AnyHashMaps.
 */
@file:JvmName("Long2AnyHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun <V> Long2AnyHashMap<V>.forEachWhile(action: Predicate<Long2AnyMap.Entry<V>>) {
    forEach { if (!action.test(it)) return }
}
