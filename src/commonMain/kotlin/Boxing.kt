package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.SystemProperties.KEY_THROW_ON_BOXING

internal object Boxing {
    fun assertBoxing() {
        if (SystemProperties.THROW_ON_BOXING) {
            throw AssertionError("Call that will result in boxing ($KEY_THROW_ON_BOXING=true)")
        }
    }
}
