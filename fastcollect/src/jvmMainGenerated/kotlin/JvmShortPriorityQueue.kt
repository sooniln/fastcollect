/**
 * Methods for dealing with primitive PriorityQueues.
 */
@file:JvmName("PriorityQueues")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.AbstractQueue
import java.util.Queue

public fun ShortPriorityQueue.asQueue(): Queue<Short> = ShortPriorityQueueWrapper(this)

private class ShortPriorityQueueWrapper(private val queue: ShortPriorityQueue) : AbstractQueue<Short>() {
    override val size: Int get() = queue.size

    override fun iterator(): MutableIterator<Short> {
        val it = queue.iterator()
        return object : MutableIterator<Short> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Short = it.nextShort()
            override fun remove(): Unit = throw UnsupportedOperationException()
        }
    }

    override fun offer(e: Short): Boolean {
        queue.add(e)
        return true
    }
    override fun poll(): Short? = if (queue.isEmpty()) null else queue.removeFirst()
    override fun peek(): Short? = if (queue.isEmpty()) null else queue.first()

    override fun contains(element: Short): Boolean = queue.contains(element)
    override fun remove(element: Short): Boolean = queue.remove(element)
    override fun clear() { queue.clear() }
    override fun removeAll(elements: Collection<Short>): Boolean = queue.removeAll(elements)
    override fun retainAll(elements: Collection<Short>): Boolean = queue.retainAll(elements)
}
