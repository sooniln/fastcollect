/**
 * Methods for dealing with primitive LongHashSets.
 */
@file:JvmName("LongHashSets")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

public fun LongHashSet.forEachWhile(action: LongPredicate) {
    forEach { if (!action.test(it)) return }
}
