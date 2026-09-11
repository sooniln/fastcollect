/**
 * Methods for dealing with primitive Int2IntHashMaps.
 */
@file:JvmName("Int2IntHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Int2IntHashMap.forEachWhile(action: Predicate<Int2IntMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
