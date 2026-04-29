package io.github.sooniln.fastcollect

public actual fun IntArray.fastIndexOf(element: Int, fromIndex: Int, toIndex: Int): Int {
    for (i in fromIndex until toIndex) {
        if (this[i] == element) return i
    }
    return -1
}
