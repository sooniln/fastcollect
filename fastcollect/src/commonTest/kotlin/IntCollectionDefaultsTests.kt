package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * Coverage for the default method bodies in Collection.kte. These are inherited by every list, set and map view
 * in the library, so they are tested once here against both an ordered (IntArrayDeque) and an unordered
 * (IntHashSet) implementation rather than repeated per primitive.
 */
class IntCollectionDefaultsTests {

    private fun collections(vararg elements: Int): List<MutableIntCollection> =
        listOf(IntArrayDeque(elements), IntHashSet(elements.asIntList()))

    @Test
    fun contains_usesRawEquality() {
        for (collection in collections(1, 2, 3)) {
            assertTrue(collection.contains(1))
            assertTrue(collection.contains(3))
            assertFalse(collection.contains(4))
        }
    }

    @Test
    fun containsAll_withPrimitiveCollection() {
        for (collection in collections(1, 2, 3)) {
            assertTrue(collection.containsAll(intListOf(1, 3)))
            assertTrue(collection.containsAll(intListOf()))
            assertTrue(collection.containsAll(intListOf(1, 1, 2)))
            assertFalse(collection.containsAll(intListOf(1, 4)))
        }
    }

    @Test
    fun containsAll_withBoxedCollection() {
        for (collection in collections(1, 2, 3)) {
            assertTrue(collection.containsAll(listOf(1, 3)))
            assertTrue(collection.containsAll(emptyList()))
            assertFalse(collection.containsAll(listOf(1, 4)))
        }
    }

    @Test
    fun copyInto_writesEveryElementAtTheGivenOffset() {
        val list = IntArrayDeque(intArrayOf(1, 2, 3))
        assertEquals(listOf(0, 1, 2, 3, 0), list.copyInto(IntArray(5), 1).toList())
    }

    @Test
    fun addAll_reportsWhetherAnythingChanged() {
        val list = IntArrayDeque(intArrayOf(1))
        assertTrue(list.addAll(intListOf(2, 3)))
        assertTrue(list.addAll(listOf(4)))
        assertFalse(list.addAll(intListOf()))
        assertEquals(listOf(1, 2, 3, 4), list.toBoxedList())

        // a set reports false when every element is already present
        val set = IntHashSet(intListOf(1, 2))
        assertFalse(set.addAll(intListOf(1, 2)))
        assertTrue(set.addAll(intListOf(2, 3)))
    }

    @Test
    fun clear_emptiesEveryCollectionKind() {
        for (collection in collections(1, 2, 3, 4, 5)) {
            collection.clear()
            assertTrue(collection.isEmpty())
            assertFalse(collection.iterator().hasNext())
            assertFalse(collection.traverser().forward())
        }
    }

    @Test
    fun removeAll_andRetainAll_filterInPlace() {
        for (collection in collections(1, 2, 3, 4)) {
            assertTrue(collection.removeAll(intListOf(2, 4)))
            assertEquals(listOf(1, 3), collection.toBoxedList().sorted())
            assertFalse(collection.removeAll(intListOf(99)))
        }

        for (collection in collections(1, 2, 3, 4)) {
            assertTrue(collection.retainAll(listOf(2, 4)))
            assertEquals(listOf(2, 4), collection.toBoxedList().sorted())
            assertFalse(collection.retainAll(listOf(2, 4)))
        }
    }

    @Test
    fun retainAll_withNoOverlap_emptiesTheCollection() {
        for (collection in collections(1, 2, 3)) {
            assertTrue(collection.retainAll(intListOf(9)))
            assertTrue(collection.isEmpty())
        }
    }

    @Test
    fun plusAssign_addsSingleElementsAndCollections() {
        for (collection in collections()) {
            collection += 1
            collection += intListOf(2, 3)
            collection += listOf(4)
            assertEquals(listOf(1, 2, 3, 4), collection.toBoxedList().sorted())
        }
    }

    @Test
    fun minusAssign_removesSingleElementsAndCollections() {
        for (collection in collections(1, 2, 3, 4, 5)) {
            collection -= 1
            collection -= intListOf(2)
            collection -= listOf(3)
            assertEquals(listOf(4, 5), collection.toBoxedList().sorted())
        }
    }

    @Test
    fun toString_rendersTheStandardCollectionForm() {
        assertEquals("[1, 2, 3]", IntArrayDeque(intArrayOf(1, 2, 3)).toString())
        assertEquals("[]", IntArrayDeque().toString())
        assertEquals("[]", IntHashSet().toString())
        assertEquals("[7]", IntHashSet(intListOf(7)).toString())
    }
}
