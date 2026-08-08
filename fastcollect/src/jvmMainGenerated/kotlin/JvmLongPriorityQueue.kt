/**
 * Methods for dealing with primitive PriorityQueues.
 */
@file:JvmName("PriorityQueues")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.AbstractQueue
import java.util.Queue

public fun LongPriorityQueue.asQueue(): Queue<Long> = LongPriorityQueueWrapper(this)

private class LongPriorityQueueWrapper(private val queue: LongPriorityQueue) : AbstractQueue<Long>() {
    override val size: Int get() = queue.size

    override fun iterator(): MutableIterator<Long> {
        val it = queue.iterator()
        return object : MutableIterator<Long> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Long = it.nextLong()
            override fun remove(): Unit = throw UnsupportedOperationException()
        }
    }

    override fun offer(e: Long): Boolean {
        queue.add(e)
        return true
    }
    override fun poll(): Long? = if (queue.isEmpty()) null else queue.removeFirst()
    override fun peek(): Long? = if (queue.isEmpty()) null else queue.first()

    override fun contains(element: Long): Boolean = queue.contains(element)
    override fun remove(element: Long): Boolean = queue.remove(element)
    override fun clear() { queue.clear() }
    override fun removeAll(elements: Collection<Long>): Boolean = queue.removeAll(elements)
    override fun retainAll(elements: Collection<Long>): Boolean = queue.retainAll(elements)
}
