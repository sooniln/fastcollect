/**
 * Methods for dealing with primitive Long2IntHashMaps.
 */
@file:JvmName("Long2IntHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Long2IntHashMap.forEachWhile(action: Predicate<Long2IntMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
