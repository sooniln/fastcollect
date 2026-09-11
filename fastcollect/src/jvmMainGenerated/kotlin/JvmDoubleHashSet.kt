/**
 * Methods for dealing with primitive DoubleHashSets.
 */
@file:JvmName("DoubleHashSets")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

public fun DoubleHashSet.forEachWhile(action: DoublePredicate) {
    forEach { if (!action.test(it)) return }
}
