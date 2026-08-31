package io.github.sooniln.fastcollect.javatest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sooniln.fastcollect.AbstractIntPriorityQueue;
import io.github.sooniln.fastcollect.IntLists;
import io.github.sooniln.fastcollect.IntPriorityQueue;
import io.github.sooniln.fastcollect.IntPriorityQueues;
import io.github.sooniln.fastcollect.IntTraverser;
import io.github.sooniln.fastcollect.PriorityQueues;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.Test;

/** Smoke tests that IntPriorityQueue and the IntPriorityQueues factories are usable from Java. */
class IntPriorityQueueJavaTest {

    @Test
    void constructors() {
        assertTrue(new IntPriorityQueue().isEmpty());
        assertTrue(new IntPriorityQueue(true).isEmpty());
        assertTrue(new IntPriorityQueue(64).isEmpty());
        assertTrue(new IntPriorityQueue(64, true).isEmpty());

        int[] source = {3, 1, 2};
        assertEquals(3, new IntPriorityQueue(source).size());
        assertEquals(2, new IntPriorityQueue(source, 1).size());
        assertEquals(1, new IntPriorityQueue(source, 1, 2).size());
        assertEquals(1, new IntPriorityQueue(source, 1, 2, true).size());

        assertEquals(3, new IntPriorityQueue(IntLists.intListOf(1, 2, 3)).size());
        assertEquals(3, new IntPriorityQueue(IntLists.intListOf(1, 2, 3), true).size());
        assertEquals(3, new IntPriorityQueue(Arrays.asList(1, 2, 3)).size());
        assertEquals(3, new IntPriorityQueue(Arrays.asList(1, 2, 3), true).size());
    }

    @Test
    void factories() {
        assertEquals(1, IntPriorityQueues.intPriorityQueueOf(3, 1, 2).first());
        assertEquals(3, IntPriorityQueues.intDescendingPriorityQueueOf(3, 1, 2).first());
    }

    @Test
    void queueOperations() {
        IntPriorityQueue queue = new IntPriorityQueue();

        // add() returns void, not boolean
        queue.add(3);
        queue.add(1);
        queue.add(2);

        assertEquals(3, queue.size());
        assertFalse(queue.isEmpty());
        assertEquals(1, queue.first());
        assertTrue(queue.contains(2));
        assertFalse(queue.contains(99));

        assertEquals(1, queue.removeFirst());
        assertEquals(2, queue.removeFirst());
        assertTrue(queue.remove(3));
        assertFalse(queue.remove(3));
        assertTrue(queue.isEmpty());
    }

    @Test
    void bulkOperations() {
        IntPriorityQueue queue = new IntPriorityQueue();

        queue.addAll(new int[] {1, 2, 3});
        queue.addAll(new int[] {4, 5}, 1);
        queue.addAll(new int[] {6, 7, 8}, 0, 2);
        queue.addAll(IntLists.intListOf(9));
        queue.addAll(List.of(10));
        assertEquals(8, queue.size());

        assertTrue(queue.removeAll(IntLists.intListOf(1, 2)));
        assertEquals(6, queue.size());

        assertTrue(queue.retainAll(IntLists.intListOf(3, 5)));
        assertEquals(2, queue.size());

        queue.ensureCapacity(128);
        queue.trimToSize();
        assertNotNull(queue.toString());

        queue.clear();
        assertTrue(queue.isEmpty());
    }

    @Test
    void predicateFiltering() {
        IntPriorityQueue queue = IntPriorityQueues.intPriorityQueueOf(1, 2, 3, 4);

        assertTrue(IntPriorityQueues.removeAll(queue, value -> value % 2 == 0));
        assertEquals(2, queue.size());

        assertTrue(IntPriorityQueues.retainAll(queue, value -> value == 1));
        assertEquals(1, queue.size());
    }

    @Test
    void iteration() {
        IntPriorityQueue queue = IntPriorityQueues.intPriorityQueueOf(1, 2, 3);

        // NOTE: iterator() is declared to return the relocated kotlin.collections.IntIterator, so it
        // cannot be named as a fastcollect type here. nextInt() is still callable.
        int sum = 0;
        var iterator = queue.iterator();
        while (iterator.hasNext()) {
            sum += iterator.nextInt();
        }
        assertEquals(6, sum);

        int traversedSum = 0;
        for (IntTraverser t = queue.traverser(); t.forward(); ) {
            traversedSum += t.getValue();
        }
        assertEquals(6, traversedSum);

        assertEquals(3, queue.copyInto(new int[3], 0).length);
    }

    @Test
    void customOrdering() {
        // AbstractIntPriorityQueue is the extension point for custom orderings
        AbstractIntPriorityQueue byAbsoluteValue = new AbstractIntPriorityQueue() {
            @Override
            protected boolean isHigherPriority(int a, int b) {
                return Math.abs(a) < Math.abs(b);
            }
        };

        byAbsoluteValue.add(-5);
        byAbsoluteValue.add(3);
        byAbsoluteValue.add(-1);
        assertEquals(-1, byAbsoluteValue.first());
    }

    @Test
    void boxedView() {
        Queue<Integer> view = PriorityQueues.asQueue(IntPriorityQueues.intPriorityQueueOf(3, 1, 2));
        assertEquals(3, view.size());
        assertEquals(Integer.valueOf(1), view.peek());
    }
}
