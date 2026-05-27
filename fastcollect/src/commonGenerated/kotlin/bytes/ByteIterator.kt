package io.github.sooniln.fastcollect.bytes

public fun emptyByteIterator(): ByteListIterator = EmptyByteIterator

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

private object EmptyByteIterator : ByteListIterator() {
    override fun previousByte(): Byte = throw NoSuchElementException()
    override fun nextByte(): Byte = throw NoSuchElementException()

    override fun hasNext(): Boolean = false
    override fun hasPrevious(): Boolean = false

    override fun nextIndex(): Int = 0
    override fun previousIndex(): Int = -1
}
