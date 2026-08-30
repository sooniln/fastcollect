package io.github.sooniln.fastcollect.javatest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.sooniln.fastcollect.Int2AnyHashMap;
import io.github.sooniln.fastcollect.Int2IntHashMap;
import io.github.sooniln.fastcollect.Int2IntMap;
import io.github.sooniln.fastcollect.Int2IntMaps;
import io.github.sooniln.fastcollect.Int2IntTraverser;
import io.github.sooniln.fastcollect.MutableInt2IntMap;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/** Smoke tests that the primitive-keyed maps and the Int2IntMaps factories are usable from Java. */
class Int2IntMapJavaTest {

    @Test
    void constructors() {
        assertTrue(new Int2IntHashMap().isEmpty());
        assertTrue(new Int2IntHashMap(64).isEmpty());
        assertTrue(new Int2IntHashMap(64, -1).isEmpty());

        Int2IntHashMap source = new Int2IntHashMap();
        source.set(1, 10);
        assertEquals(1, new Int2IntHashMap(source).size());
        assertEquals(1, new Int2IntHashMap(source, -1).size());
        assertEquals(1, new Int2IntHashMap(Collections.singletonMap(1, 10)).size());
        assertEquals(1, new Int2IntHashMap(Collections.singletonMap(1, 10), -1).size());
    }

    @Test
    @SuppressWarnings("unchecked") // generic Map.Entry varargs
    void factories() {
        assertTrue(Int2IntMaps.emptyInt2IntMap().isEmpty());
        assertTrue(Int2IntMaps.int2IntMapOf().isEmpty());
        assertEquals(1, Int2IntMaps.int2IntMapOf(1, 10).size());
        assertEquals(1, Int2IntMaps.int2IntMapOf(Map.entry(1, 10)).size());
        assertEquals(2, Int2IntMaps.int2IntMapOf(Map.entry(1, 10), Map.entry(2, 20)).size());

        assertTrue(Int2IntMaps.mutableInt2IntMapOf().isEmpty());
        assertEquals(1, Int2IntMaps.mutableInt2IntMapOf(Map.entry(1, 10)).size());
        assertEquals(2, Int2IntMaps.mutableInt2IntMapOf(Map.entry(1, 10), Map.entry(2, 20)).size());
    }

    @Test
    void reads() {
        Int2IntMap map = Int2IntMaps.int2IntMapOf(1, 10);

        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
        assertTrue(map.containsKey(1));
        assertFalse(map.containsKey(2));
        assertTrue(map.containsValue(10));
        assertEquals(10, map.get(1));
        assertEquals(10, map.getValue(1));
        assertEquals(-1, map.getOrDefault(2, -1));

        assertEquals(1, map.keys().getSize());
        assertEquals(1, map.values().getSize());
    }

    @Test
    void absentKeySemantics() {
        Int2IntHashMap map = new Int2IntHashMap(0, -1);

        // get() falls back to the map's default value, getValue() throws
        assertEquals(-1, map.get(99));
        assertTrue(map.isDefaultValue(map.get(99)));
        assertThrows(NoSuchElementException.class, () -> map.getValue(99));
        assertThrows(NoSuchElementException.class, () -> map.removeKey(99));
        assertThrows(NoSuchElementException.class, () -> map.replace(99, 1));
    }

    @Test
    void mutation() {
        MutableInt2IntMap map = Int2IntMaps.mutableInt2IntMapOf();

        map.set(1, 10);
        assertEquals(10, map.put(1, 11));
        assertEquals(11, map.putIfAbsent(1, 12));
        assertEquals(11, map.replace(1, 13));
        assertEquals(13, map.get(1));

        map.putAll(Int2IntMaps.int2IntMapOf(2, 20));
        map.putAll(Collections.singletonMap(3, 30));
        assertEquals(3, map.size());

        assertEquals(13, map.remove(1));
        assertEquals(20, map.removeKey(2));
        assertTrue(map.remove(3, 30));
        assertFalse(map.remove(3, 30));
        assertTrue(map.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked") // generic Map.Entry varargs
    void iteration() {
        MutableInt2IntMap map = Int2IntMaps.mutableInt2IntMapOf(Map.entry(1, 10), Map.entry(2, 20));

        int keySum = 0;
        int valueSum = 0;
        for (Iterator<MutableInt2IntMap.MutableEntry> it = map.iterator(); it.hasNext(); ) {
            MutableInt2IntMap.MutableEntry next = it.next();
            keySum += next.getKey();
            valueSum += next.getValue();
            next.setValue(next.getValue() + 1);
        }
        assertEquals(3, keySum);
        assertEquals(30, valueSum);
        assertEquals(11, map.get(1));

        int traversedKeys = 0;
        for (Int2IntTraverser t = map.traverser(); t.forward(); ) {
            traversedKeys += t.getKey();
        }
        assertEquals(3, traversedKeys);
    }

    @Test
    void boxedViews() {
        Map<Integer, Integer> view = Int2IntMaps.asMap(Int2IntMaps.int2IntMapOf(1, 10));
        assertEquals(1, view.size());
        assertEquals(Integer.valueOf(10), view.get(1));

        MutableInt2IntMap backing = Int2IntMaps.mutableInt2IntMapOf();
        Map<Integer, Integer> mutableView = Int2IntMaps.asMap(backing);
        mutableView.put(1, 10);
        assertEquals(1, backing.size());

        Map.Entry<Integer, Integer> asEntry = Int2IntMaps.asEntry(backing.iterator().next());
        assertEquals(Integer.valueOf(1), asEntry.getKey());
        assertEquals(Integer.valueOf(10), asEntry.getValue());
    }

    @Test
    void referenceValuedMap() {
        // Int2AnyHashMap has no (capacity, defaultValue) constructor - the default value is null
        Int2AnyHashMap<String> map = new Int2AnyHashMap<>();

        map.set(1, "one");
        assertEquals("one", map.put(1, "uno"));
        assertEquals("uno", map.get(1));
        assertNull(map.get(99));
        assertTrue(map.isDefaultValue(map.get(99)));
        assertTrue(map.containsKey(1));
        assertTrue(map.containsValue("uno"));
        assertEquals(1, map.keys().getSize());
        assertEquals(1, map.values().size());

        assertEquals("uno", map.remove(1));
        assertTrue(map.isEmpty());

        assertEquals(1, new Int2AnyHashMap<>(Collections.singletonMap(1, "one")).size());
    }
}
