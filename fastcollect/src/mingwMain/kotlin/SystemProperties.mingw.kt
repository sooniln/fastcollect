package io.github.sooniln.fastcollect

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.get
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.windows.CommandLineToArgvW
import platform.windows.GetCommandLine
import platform.windows.LocalFree

@OptIn(ExperimentalForeignApi::class)
internal actual fun getProperty(key: String): String? {
    memScoped {
        val argc = alloc<IntVar>()
        val argv = CommandLineToArgvW(GetCommandLine?.invoke()?.toKString(), argc.ptr)
        if (argv != null) {
            try {
                val count = argc.value
                for (i in 0 until count) {
                    val arg = argv[i]?.toKString()
                    if (arg != null && arg.startsWith("--$key")) {
                        val remainder = arg.substring("--$key".length)
                        if (remainder.isEmpty()) return "true"
                        if (remainder.startsWith("=") || remainder.startsWith(",")) return remainder.substring(1)
                    }
                }
            } finally {
                LocalFree(argv)
            }
        }
        return null
    }
}
