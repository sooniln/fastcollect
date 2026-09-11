/**
 * Methods for dealing with primitive IntHashSets.
 */
@file:JvmName("IntHashSets")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

public fun IntHashSet.forEachWhile(action: IntPredicate) {
    forEach { if (!action.test(it)) return }
}
