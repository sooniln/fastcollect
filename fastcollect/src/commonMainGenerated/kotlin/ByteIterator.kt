/**
 * Methods for dealing with primitive Iterators.
 */
@file:JvmName("Iterators")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun emptyByteIterator(): ByteListIterator = EmptyByteIterator
public fun emptyMutableByteIterator(): MutableByteIterator = EmptyMutableByteIterator

public fun byteIteratorOf(value: Byte): ByteListIterator = SingletonByteIterator(value)

public abstract class MutableByteIterator : ByteIterator(), MutableIterator<Byte>

public abstract class ByteListIterator: ByteIterator(), ListIterator<Byte> {
    @Deprecated(
        message = "Use previousByte() instead.",
        replaceWith = ReplaceWith("previousByte()"),
        level = DeprecationLevel.WARNING)
    final override fun previous(): Byte = previousByte()
    public abstract fun previousByte(): Byte
}

public abstract class MutableByteListIterator: ByteListIterator(), MutableListIterator<Byte>

private object EmptyByteIterator : MutableByteListIterator() {
    override fun previousByte(): Byte = throw NoSuchElementException()
    override fun nextByte(): Byte = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1

    override fun remove() = throw IllegalStateException()
    override fun set(element: Byte) = throw IllegalStateException()
    override fun add(element: Byte) = throw UnsupportedOperationException()
}

private object EmptyMutableByteIterator : MutableByteIterator() {
    override fun nextByte(): Byte = throw NoSuchElementException()

    override fun hasNext(): Boolean = false

    override fun remove() = throw IllegalStateException()
}

private class SingletonByteIterator(private val value: Byte) : ByteListIterator() {
    private var pos = 0

    override fun previousByte(): Byte {
        if (pos == 0) throw NoSuchElementException()
        --pos
        return value
    }
    override fun nextByte(): Byte {
        if (pos == 1) throw NoSuchElementException()
        ++pos
        return value
    }

    override fun hasNext(): Boolean = pos == 0
    override fun hasPrevious(): Boolean = pos == 1

    override fun nextIndex(): Int = pos
    override fun previousIndex(): Int = pos - 1
}
