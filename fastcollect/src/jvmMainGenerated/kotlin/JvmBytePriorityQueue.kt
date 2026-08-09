/**
 * Methods for dealing with primitive PriorityQueues.
 */
@file:JvmName("PriorityQueues")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.AbstractQueue
import java.util.Queue

public fun BytePriorityQueue.asQueue(): Queue<Byte> = BytePriorityQueueWrapper(this)

private class BytePriorityQueueWrapper(private val queue: BytePriorityQueue) : AbstractQueue<Byte>() {
    override val size: Int get() = queue.size

    override fun iterator(): MutableIterator<Byte> {
        val it = queue.iterator()
        return object : MutableIterator<Byte> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Byte = it.nextByte()
            override fun remove(): Unit = throw UnsupportedOperationException()
        }
    }

    override fun offer(e: Byte): Boolean {
        queue.add(e)
        return true
    }
    override fun poll(): Byte? = if (queue.isEmpty()) null else queue.removeFirst()
    override fun peek(): Byte? = if (queue.isEmpty()) null else queue.first()

    override fun contains(element: Byte): Boolean = queue.contains(element)
    override fun remove(element: Byte): Boolean = queue.remove(element)
    override fun clear() { queue.clear() }
    override fun removeAll(elements: Collection<Byte>): Boolean = queue.removeAll(elements)
    override fun retainAll(elements: Collection<Byte>): Boolean = queue.retainAll(elements)
}
