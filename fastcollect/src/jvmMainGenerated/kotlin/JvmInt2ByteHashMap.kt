/**
 * Methods for dealing with primitive Int2ByteHashMaps.
 */
@file:JvmName("Int2ByteHashMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.function.Predicate

public fun  Int2ByteHashMap.forEachWhile(action: Predicate<Int2ByteMap.Entry>) {
    forEach { if (!action.test(it)) return }
}
