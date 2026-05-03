package io.github.sooniln.fastcollect

internal object SystemProperties {
    const val KEY_WARN_ON_BOXING = "fastcollect-warn-on-boxing"

    val WARN_ON_BOXING: Boolean = getProperty(KEY_WARN_ON_BOXING)?.lowercase() == "true"
}

internal expect fun getProperty(key: String): String?
