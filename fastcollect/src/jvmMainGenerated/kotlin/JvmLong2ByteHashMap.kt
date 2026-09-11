/**
 * Methods for dealing with primitive Long2ByteHashMaps.
 */
@file:JvmName("Long2ByteHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Long2ByteHashMap.forEachWhile(action: Predicate<Long2ByteMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
