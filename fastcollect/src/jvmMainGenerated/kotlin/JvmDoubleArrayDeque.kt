/**
 * Methods for dealing with DoubleArrayDeques.
 */
@file:JvmName("DoubleArrayDeques")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun DoubleArrayDeque.forEachWhile(action: DoublePredicate) {
    forEach { if (!action.test(it)) return }
}

public fun DoubleArrayDeque.forEachWhileReverse(action: DoublePredicate) {
    forEachReverse { if (!action.test(it)) return }
}
