/**
 * Methods for dealing with primitive Int2LongHashMaps.
 */
@file:JvmName("Int2LongHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Int2LongHashMap.forEachWhile(action: Predicate<Int2LongMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
