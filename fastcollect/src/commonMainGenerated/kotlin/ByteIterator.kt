/**
 * Methods for dealing with primitive Iterators.
 */
@file:JvmName("Iterators")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

public fun emptyByteIterator(): MutableByteIterator = EmptyByteIterator
public fun byteIteratorOf(value: Byte): ByteIterator = SingletonByteIterator(value)

public abstract class MutableByteIterator : ByteIterator(), MutableIterator<Byte>

public abstract class ByteListIterator : ByteIterator(), ListIterator<Byte> {
    public abstract fun previousByte(): Byte
    final override fun previous(): Byte = previousByte()
}

public abstract class MutableByteListIterator : ByteListIterator(), MutableListIterator<Byte> {
    abstract override fun set(element: Byte)
    abstract override fun add(element: Byte)
}

private object EmptyByteIterator : MutableByteIterator() {
    override fun hasNext(): Boolean = false
    override fun nextByte(): Byte = throw NoSuchElementException()
    override fun remove() = throw IllegalStateException()
}

private class SingletonByteIterator(private val value: Byte) : ByteIterator() {
    private var done = false

    override fun hasNext(): Boolean = !done
    override fun nextByte(): Byte {
        if (done) throw NoSuchElementException()
        done = true
        return value
    }
}
