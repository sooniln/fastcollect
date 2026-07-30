package io.github.sooniln.fastcollect.ints

import java.util.AbstractQueue
import java.util.Queue

public fun IntPriorityQueue.asQueue(): Queue<Int> = IntPriorityQueueWrapper(this)

private class IntPriorityQueueWrapper(private val queue: IntPriorityQueue) : AbstractQueue<Int>() {
    override val size: Int get() = queue.size

    override fun iterator(): MutableIterator<Int> {
        val it = queue.iterator()
        return object : MutableIterator<Int> {
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Int = it.nextInt()
            override fun remove(): Unit = throw UnsupportedOperationException()
        }
    }

    override fun offer(e: Int): Boolean {
        queue.add(e)
        return true
    }
    override fun poll(): Int? = if (queue.isEmpty()) null else queue.removeFirst()
    override fun peek(): Int? = if (queue.isEmpty()) null else queue.first()

    override fun contains(element: Int): Boolean = queue.contains(element)
    override fun remove(element: Int): Boolean = queue.remove(element)
    override fun clear() { queue.clear() }
    override fun removeAll(elements: Collection<Int>): Boolean = queue.removeAll(elements)
    override fun retainAll(elements: Collection<Int>): Boolean = queue.retainAll(elements)
}
