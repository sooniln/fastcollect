/**
 * Methods for dealing with IntArrayDeques.
 */
@file:JvmName("IntArrayDeques")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun IntArrayDeque.forEachWhile(action: IntPredicate) {
    forEach { if (!action.test(it)) return }
}

public fun IntArrayDeque.forEachWhileReverse(action: IntPredicate) {
    forEachReverse { if (!action.test(it)) return }
}
