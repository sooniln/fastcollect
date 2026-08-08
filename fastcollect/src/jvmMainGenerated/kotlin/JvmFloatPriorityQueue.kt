/**
 * Methods for dealing with primitive PriorityQueues.
 */
@file:JvmName("PriorityQueues")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.AbstractQueue
import java.util.Queue

public fun FloatPriorityQueue.asQueue(): Queue<Float> = FloatPriorityQueueWrapper(this)

private class FloatPriorityQueueWrapper(private val queue: FloatPriorityQueue) : AbstractQueue<Float>() {
    override val size: Int get() = queue.size

    override fun iterator(): MutableIterator<Float> {
        val it = queue.iterator()
        return object : MutableIterator<Float> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Float = it.nextFloat()
            override fun remove(): Unit = throw UnsupportedOperationException()
        }
    }

    override fun offer(e: Float): Boolean {
        queue.add(e)
        return true
    }
    override fun poll(): Float? = if (queue.isEmpty()) null else queue.removeFirst()
    override fun peek(): Float? = if (queue.isEmpty()) null else queue.first()

    override fun contains(element: Float): Boolean = queue.contains(element)
    override fun remove(element: Float): Boolean = queue.remove(element)
    override fun clear() { queue.clear() }
    override fun removeAll(elements: Collection<Float>): Boolean = queue.removeAll(elements)
    override fun retainAll(elements: Collection<Float>): Boolean = queue.retainAll(elements)
}
