package io.github.sooniln.fastcollect.javatest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sooniln.fastcollect.IntHashSet;
import io.github.sooniln.fastcollect.IntLists;
import io.github.sooniln.fastcollect.IntSet;
import io.github.sooniln.fastcollect.IntSets;
import io.github.sooniln.fastcollect.IntTraverser;
import io.github.sooniln.fastcollect.MutableIntIterator;
import io.github.sooniln.fastcollect.MutableIntSet;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Smoke tests that IntHashSet and the IntSets factories are usable from Java. */
class IntSetJavaTest {

    @Test
    void constructors() {
        assertTrue(new IntHashSet().isEmpty());
        assertTrue(new IntHashSet(64).isEmpty());
        assertEquals(3, new IntHashSet(IntLists.intListOf(1, 2, 3)).getSize());
        assertEquals(3, new IntHashSet(Arrays.asList(1, 2, 3)).getSize());
    }

    @Test
    void factories() {
        assertTrue(IntSets.emptyIntSet().isEmpty());
        assertEquals(0, IntSets.intSetOf().getSize());
        assertEquals(1, IntSets.intSetOf(7).getSize());
        assertEquals(3, IntSets.intSetOf(1, 2, 3).getSize());

        assertEquals(0, IntSets.mutableIntSetOf().getSize());
        assertEquals(1, IntSets.mutableIntSetOf(7).getSize());
        assertEquals(3, IntSets.mutableIntSetOf(1, 2, 3).getSize());
    }

    @Test
    void mutation() {
        IntHashSet set = new IntHashSet();

        assertTrue(set.add(1));
        assertFalse(set.add(1));
        assertTrue(set.contains(1));
        assertEquals(1, set.getSize());

        assertTrue(set.addAll(IntLists.intListOf(2, 3)));
        assertTrue(set.addAll(Arrays.asList(4, 5)));
        assertEquals(5, set.getSize());

        assertTrue(set.remove(1));
        assertFalse(set.remove(1));
        assertTrue(set.removeAll(IntLists.intListOf(2, 3)));
        assertTrue(set.retainAll(IntLists.intListOf(4)));
        assertEquals(1, set.getSize());

        set.ensureCapacity(128);
        set.trimToSize();

        set.clear();
        assertTrue(set.isEmpty());
    }

    @Test
    void setAlgebra() {
        IntSet a = IntSets.intSetOf(1, 2, 3);
        IntSet b = IntSets.intSetOf(3, 4);

        assertEquals(4, IntSets.union(a, b).getSize());
        assertEquals(1, IntSets.intersect(a, b).getSize());
        assertEquals(2, IntSets.subtract(a, b).getSize());
    }

    @Test
    void iteration() {
        MutableIntSet set = IntSets.mutableIntSetOf(1, 2, 3);

        int sum = 0;
        for (MutableIntIterator it = set.iterator(); it.hasNext(); ) {
            sum += it.nextInt();
        }
        assertEquals(6, sum);

        int traversedSum = 0;
        for (IntTraverser t = set.traverser(); t.forward(); ) {
            traversedSum += t.getValue();
        }
        assertEquals(6, traversedSum);
    }

    @Test
    void copyInto() {
        int[] dest = IntSets.intSetOf(5).copyInto(new int[1], 0);
        assertEquals(5, dest[0]);
    }

    @Test
    void boxedView() {
        Set<Integer> view = IntSets.asSet(IntSets.intSetOf(1, 2, 3));
        assertEquals(3, view.size());
        assertTrue(view.contains(2));

        MutableIntSet backing = IntSets.mutableIntSetOf(1, 2);
        Set<Integer> mutableView = IntSets.asSet(backing);
        assertTrue(mutableView.add(3));
        assertEquals(3, backing.getSize());
    }
}
