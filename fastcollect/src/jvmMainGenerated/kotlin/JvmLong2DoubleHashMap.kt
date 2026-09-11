/**
 * Methods for dealing with primitive Long2DoubleHashMaps.
 */
@file:JvmName("Long2DoubleHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Long2DoubleHashMap.forEachWhile(action: Predicate<Long2DoubleMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
