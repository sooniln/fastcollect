package io.github.sooniln.fastcollect.doubles

import java.util.AbstractQueue
import java.util.Queue

public fun DoublePriorityQueue.asQueue(): Queue<Double> = DoublePriorityQueueWrapper(this)

private class DoublePriorityQueueWrapper(private val queue: DoublePriorityQueue) : AbstractQueue<Double>() {
    override val size: Int get() = queue.size

    override fun iterator(): MutableIterator<Double> {
        val it = queue.iterator()
        return object : MutableIterator<Double> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Double = it.nextDouble()
            override fun remove(): Unit = throw UnsupportedOperationException()
        }
    }

    override fun offer(e: Double): Boolean {
        queue.add(e)
        return true
    }
    override fun poll(): Double? = if (queue.isEmpty()) null else queue.removeFirst()
    override fun peek(): Double? = if (queue.isEmpty()) null else queue.first()

    override fun contains(element: Double): Boolean = queue.contains(element)
    override fun remove(element: Double): Boolean = queue.remove(element)
    override fun clear() { queue.clear() }
    override fun removeAll(elements: Collection<Double>): Boolean = queue.removeAll(elements)
    override fun retainAll(elements: Collection<Double>): Boolean = queue.retainAll(elements)
}
