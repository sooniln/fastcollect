/**
 * Methods for dealing with ByteArrayDeques.
 */
@file:JvmName("ByteArrayDeques")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun ByteArrayDeque.forEachWhile(action: BytePredicate) {
    forEach { if (!action.test(it)) return }
}

public fun ByteArrayDeque.forEachWhileReverse(action: BytePredicate) {
    forEachReverse { if (!action.test(it)) return }
}
