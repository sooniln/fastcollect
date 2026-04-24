package io.github.sooniln.fastcollect

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.EOF
import platform.posix.fclose
import platform.posix.fgetc
import platform.posix.fopen
import platform.posix.getpid

@OptIn(ExperimentalForeignApi::class)
internal actual fun getProperty(key: String): String? {
    val cmdlinePath = "/proc/${getpid()}/cmdline"
    val file = fopen(cmdlinePath, "r") ?: return null

    try {
        val args = mutableListOf<String>()
        val stringBuilder = StringBuilder()
        while (true) {
            val char = fgetc(file)
            if (char == EOF) break

            if (char == 0) { // Arguments are separated by null bytes
                args.add(stringBuilder.toString())
                stringBuilder.clear()
            } else {
                stringBuilder.append(char.toChar())
            }
        }

        args.find { arg ->
            if (arg.startsWith("--$key")) {
                var remainder = arg.substring("--$key".length)
                if (remainder.isEmpty()) return "true"
                if (remainder.startsWith("=") || remainder.startsWith(",")) return remainder.substring(1)
            }
            return@find false
        }

        return null
    } finally {
        fclose(file)
    }
}
