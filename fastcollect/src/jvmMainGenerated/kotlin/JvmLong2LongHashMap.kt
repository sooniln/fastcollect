/**
 * Methods for dealing with primitive Long2LongHashMaps.
 */
@file:JvmName("Long2LongHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Long2LongHashMap.forEachWhile(action: Predicate<Long2LongMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
