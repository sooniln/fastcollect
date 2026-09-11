/**
 * Methods for dealing with FloatArrayDeques.
 */
@file:JvmName("FloatArrayDeques")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun FloatArrayDeque.forEachWhile(action: FloatPredicate) {
    forEach { if (!action.test(it)) return }
}

public fun FloatArrayDeque.forEachWhileReverse(action: FloatPredicate) {
    forEachReverse { if (!action.test(it)) return }
}
