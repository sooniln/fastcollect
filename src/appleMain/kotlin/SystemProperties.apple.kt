package io.github.sooniln.fastcollect

import platform.Foundation.NSProcessInfo

internal actual fun getProperty(key: String): String? = NSProcessInfo.processInfo.environment[key] as? String?
