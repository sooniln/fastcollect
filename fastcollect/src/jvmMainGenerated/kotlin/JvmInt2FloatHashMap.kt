/**
 * Methods for dealing with primitive Int2FloatHashMaps.
 */
@file:JvmName("Int2FloatHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Int2FloatHashMap.forEachWhile(action: Predicate<Int2FloatMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
