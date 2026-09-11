/**
 * Methods for dealing with primitive Int2DoubleHashMaps.
 */
@file:JvmName("Int2DoubleHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Int2DoubleHashMap.forEachWhile(action: Predicate<Int2DoubleMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
