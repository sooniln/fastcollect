/**
 * Methods for dealing with primitive FloatHashSets.
 */
@file:JvmName("FloatHashSets")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

public fun FloatHashSet.forEachWhile(action: FloatPredicate) {
    forEach { if (!action.test(it)) return }
}
