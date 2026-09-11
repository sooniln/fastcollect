/**
 * Methods for dealing with LongArrayDeques.
 */
@file:JvmName("LongArrayDeques")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun LongArrayDeque.forEachWhile(action: LongPredicate) {
    forEach { if (!action.test(it)) return }
}

public fun LongArrayDeque.forEachWhileReverse(action: LongPredicate) {
    forEachReverse { if (!action.test(it)) return }
}
