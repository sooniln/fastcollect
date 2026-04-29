package io.github.sooniln.fastcollect

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
@Suppress("NOTHING_TO_INLINE")
internal actual inline fun assert(value: Boolean) = kotlin.assert(value)
