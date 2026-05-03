package io.github.sooniln.fastcollect

@Suppress("NOTHING_TO_INLINE")
internal inline fun assertBoxing() {
    if (SystemProperties.WARN_ON_BOXING) AssertionError("Detected call that will result in boxing").printStackTrace()
}
