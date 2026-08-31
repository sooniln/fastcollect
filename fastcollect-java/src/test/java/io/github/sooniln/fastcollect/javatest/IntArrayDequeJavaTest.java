package io.github.sooniln.fastcollect.javatest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sooniln.fastcollect.IntArrayDeque;
import io.github.sooniln.fastcollect.IntLists;
import io.github.sooniln.fastcollect.IntTraverser;
import io.github.sooniln.fastcollect.MutableIntIterator;
import io.github.sooniln.fastcollect.MutableIntListTraverser;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Smoke tests that IntArrayDeque - the concrete MutableIntList - is usable from Java. */
class IntArrayDequeJavaTest {

    @Test
    void constructors() {
        assertTrue(new IntArrayDeque().isEmpty());
        assertTrue(new IntArrayDeque(64).isEmpty());
        assertEquals(3, new IntArrayDeque(IntLists.intListOf(1, 2, 3)).size());
        assertEquals(3, new IntArrayDeque(Arrays.asList(1, 2, 3)).size());

        // there is no single-argument (int[]) constructor - the range is always explicit
        int[] source = {1, 2, 3, 4};
        assertEquals(2, new IntArrayDeque(source, 1, 3).size());
    }

    @Test
    void dequeOperations() {
        IntArrayDeque deque = new IntArrayDeque();

        deque.addLast(2);
        deque.addLast(3);
        deque.addFirst(1);
        assertArrayEquals(new int[] {1, 2, 3}, deque.copyInto(new int[3], 0, 0, 3));

        assertEquals(1, deque.removeFirst());
        assertEquals(3, deque.removeLast());
        assertEquals(1, deque.size());
    }

    @Test
    void listOperations() {
        IntArrayDeque deque = new IntArrayDeque(IntLists.intListOf(10, 20, 30, 20));

        assertEquals(20, deque.get(1));
        assertEquals(1, deque.indexOf(20));
        assertEquals(3, deque.lastIndexOf(20));
        assertTrue(deque.contains(30));
        assertFalse(deque.contains(99));

        deque.set(0, 40);
        assertEquals(40, deque.get(0));

        deque.add(0, 5);
        assertEquals(5, deque.size());
        assertEquals(5, deque.removeAt(0));

        deque.removeRange(0, 2);
        assertEquals(2, deque.size());

        assertTrue(deque.addAll(IntLists.intListOf(1, 2)));
        assertTrue(deque.addAll(Arrays.asList(3, 4)));
        assertTrue(deque.removeAll(IntLists.intListOf(1, 2)));
        assertTrue(deque.retainAll(IntLists.intListOf(3, 4)));
        assertEquals(2, deque.size());

        assertEquals(1, deque.subList(0, 1).size());
    }

    @Test
    void bulkOperations() {
        IntArrayDeque deque = new IntArrayDeque(IntLists.intListOf(3, 1, 2));

        deque.sort();
        assertArrayEquals(new int[] {1, 2, 3}, deque.copyInto(new int[3], 0, 0, 3));

        deque.sortDescending();
        assertArrayEquals(new int[] {3, 2, 1}, deque.copyInto(new int[3], 0, 0, 3));

        deque.reverse();
        assertArrayEquals(new int[] {1, 2, 3}, deque.copyInto(new int[3], 0, 0, 3));

        deque.fill(7);
        assertArrayEquals(new int[] {7, 7, 7}, deque.copyInto(new int[3], 0, 0, 3));

        deque.ensureCapacity(128);
        deque.trimToSize();
        assertEquals(3, deque.size());

        deque.clear();
        assertTrue(deque.isEmpty());
    }

    @Test
    void iteration() {
        IntArrayDeque deque = new IntArrayDeque(IntLists.intListOf(1, 2, 3));

        int sum = 0;
        for (MutableIntIterator it = deque.iterator(); it.hasNext(); ) {
            sum += it.nextInt();
        }
        assertEquals(6, sum);

        int traversedSum = 0;
        for (IntTraverser t = deque.traverser(); t.forward(); ) {
            traversedSum += t.getValue();
        }
        assertEquals(6, traversedSum);

        MutableIntListTraverser positioned = deque.traverser(1);
        assertTrue(positioned.forward());
        assertEquals(2, positioned.getValue());
    }

    @Test
    void equalsAndHashCode() {
        IntArrayDeque a = new IntArrayDeque(IntLists.intListOf(1, 2, 3));
        IntArrayDeque b = new IntArrayDeque(IntLists.intListOf(1, 2, 3));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
