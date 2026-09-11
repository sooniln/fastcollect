/**
 * Methods for dealing with primitive Int2AnyHashMaps.
 */
@file:JvmName("Int2AnyHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun <V> Int2AnyHashMap<V>.forEachWhile(action: Predicate<Int2AnyMap.Entry<V>>) {
    forEach { if (!action.test(it)) return }
}
