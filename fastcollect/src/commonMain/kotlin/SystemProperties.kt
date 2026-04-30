package io.github.sooniln.fastcollect

internal object SystemProperties {
    const val KEY_THROW_ON_BOXING = "fastcollect-throw-on-boxing"

    val THROW_ON_BOXING: Boolean = getProperty(KEY_THROW_ON_BOXING)?.lowercase() == "true"
}

internal expect fun getProperty(key: String): String?
