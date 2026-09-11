package io.github.sooniln.fastcollect.javatest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sooniln.fastcollect.IntList;
import io.github.sooniln.fastcollect.IntListIterator;
import io.github.sooniln.fastcollect.IntLists;
import io.github.sooniln.fastcollect.MutableIntList;
import io.github.sooniln.fastcollect.MutableIntListIterator;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Smoke tests that IntList/MutableIntList and the IntLists factories are usable from Java. */
class IntListJavaTest {

    @Test
    void factories() {
        assertTrue(IntLists.emptyIntList().isEmpty());
        assertEquals(0, IntLists.intListOf().size());
        assertEquals(1, IntLists.intListOf(7).size());

        IntList list = IntLists.intListOf(1, 2, 3);
        assertEquals(3, list.size());
        assertEquals(2, list.get(1));

        MutableIntList mutable = IntLists.mutableIntListOf(1, 2, 3);
        assertEquals(3, mutable.size());
        assertEquals(0, IntLists.mutableIntListOf().size());
        assertEquals(1, IntLists.mutableIntListOf(7).size());

        assertEquals(3, IntLists.asIntList(new int[] {4, 5, 6}).size());
    }

    @Test
    void reads() {
        IntList list = IntLists.intListOf(10, 20, 30, 20);

        assertFalse(list.isEmpty());
        assertTrue(list.contains(20));
        assertFalse(list.contains(99));
        assertEquals(1, list.indexOf(20));
        assertEquals(3, list.lastIndexOf(20));
        assertEquals(2, list.subList(1, 3).size());
        assertTrue(list.containsAll(IntLists.intListOf(10, 30)));
    }

    @Test
    void mutation() {
        MutableIntList list = IntLists.mutableIntListOf();

        assertTrue(list.add(1));
        list.addLast(3);
        list.addFirst(0);
        list.add(2, 2);
        assertArrayEquals(new int[] {0, 1, 2, 3}, list.copyInto(new int[4], 0));

        // set() is void; replace() returns the previous value
        list.set(0, 9);
        assertEquals(9, list.replace(0, 0));

        assertEquals(0, list.removeFirst());
        assertEquals(3, list.removeLast());
        assertEquals(1, list.removeAt(0));
        assertTrue(list.remove(2));
        assertTrue(list.isEmpty());

        assertTrue(list.addAll(IntLists.intListOf(5, 4, 6)));
        list.sort();
        assertArrayEquals(new int[] {4, 5, 6}, list.copyInto(new int[3], 0));
        list.sortDescending();
        assertArrayEquals(new int[] {6, 5, 4}, list.copyInto(new int[3], 0));
        list.reverse();
        assertArrayEquals(new int[] {4, 5, 6}, list.copyInto(new int[3], 0));

        list.addAll(0, IntLists.intListOf(1, 2, 3));
        assertEquals(6, list.size());
        list.removeRange(0, 3);
        assertEquals(3, list.size());

        list.shuffle();
        assertEquals(3, list.size());

        list.fill(7);
        assertArrayEquals(new int[] {7, 7, 7}, list.copyInto(new int[3], 0));

        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    void copyIntoRange() {
        IntList list = IntLists.intListOf(1, 2, 3, 4);
        int[] dest = new int[2];
        assertArrayEquals(new int[] {2, 3}, list.copyInto(dest, 0, 1, 3));
    }

    @Test
    void listIterator() {
        IntList list = IntLists.intListOf(1, 2, 3);

        int sum = 0;
        for (IntListIterator it = list.listIterator(0); it.hasNext(); ) {
            sum += it.nextInt();
        }
        assertEquals(6, sum);

        IntListIterator backwards = list.listIterator(list.size());
        assertTrue(backwards.hasPrevious());
        assertEquals(3, backwards.previousInt());
        assertEquals(2, backwards.nextIndex());
    }

    @Test
    void mutableListIterator() {
        MutableIntList list = IntLists.mutableIntListOf(1, 2, 3);

        MutableIntListIterator it = list.listIterator(0);
        assertTrue(it.hasNext());
        assertEquals(1, it.nextInt());
        it.set(9);
        assertEquals(9, list.get(0));

        it.add(8);
        assertEquals(4, list.size());

        assertTrue(it.hasNext());
        assertEquals(2, it.nextInt());
        it.remove();
        assertEquals(3, list.size());
    }

    @Test
    void boxedView() {
        List<Integer> view = IntLists.asList(IntLists.intListOf(1, 2, 3));
        assertEquals(3, view.size());
        assertEquals(Integer.valueOf(2), view.get(1));
        assertTrue(view.contains(3));

        MutableIntList backing = IntLists.mutableIntListOf(1, 2);
        List<Integer> mutableView = IntLists.asList(backing);
        assertTrue(mutableView.add(3));
        assertEquals(3, backing.size());
    }
}
