/**
 * Methods for dealing with primitive Long2FloatHashMaps.
 */
@file:JvmName("Long2FloatHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Long2FloatHashMap.forEachWhile(action: Predicate<Long2FloatMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
