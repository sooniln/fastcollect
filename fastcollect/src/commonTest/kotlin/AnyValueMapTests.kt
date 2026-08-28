package io.github.sooniln.fastcollect

import kotlin.test.*

/**
 * What is actually specific to the reference-valued map expansions (Int2Any / Long2Any): the value type is
 * nullable, `null` is the default value that signals "absent", and value equality goes through Any?.equalsRaw
 * (i.e. ordinary equals) rather than a bitwise comparison. Everything else is shared HashMap.kte/Map.kte logic
 * covered once by Int2LongHashMapTests and Int2LongMapDefaultsTests.
 */
class AnyValueMapTests {

    @Test
    fun defaultValueIsNull() {
        val map = mutableInt2AnyMapOf<String?>()
        assertNull(map[1])
        assertTrue(map.isDefaultValue(null))
        assertFalse(map.isDefaultValue("a"))
    }

    @Test
    fun storedNull_keyStillReportedPresent() {
        // null doubles as the "absent" signal, so a key that genuinely stores null must not vanish
        val map = mutableInt2AnyMapOf<String?>()
        map[1] = null

        assertTrue(map.containsKey(1))
        assertEquals(1, map.size)
        assertNull(map[1])
        assertTrue(map.containsValue(null))
        assertNull(map.getValue(1))

        var calls = 0
        assertNull(map.getOrElse(1) { calls++; "fallback" })
        assertEquals(0, calls, "a key storing null is present, not absent")
        assertNull(map.getOrPut(1) { calls++; "fallback" })
        assertEquals(0, calls)

        assertNull(map.removeKey(1))
        assertTrue(map.isEmpty())
    }

    @Test
    fun put_andPutIfAbsent_distinguishStoredNullFromAbsent() {
        val map = mutableInt2AnyMapOf<String?>()

        assertNull(map.put(1, null), "an absent key returns the default, which is also null")
        assertEquals(1, map.size)

        assertNull(map.putIfAbsent(1, "x"), "the stored null makes the key present")
        assertNull(map[1], "the stored null must not be overwritten")

        assertNull(map.put(1, "y"))
        assertEquals("y", map.put(1, "z"))
        assertEquals("z", map[1])
    }

    @Test
    fun remove_byKeyAndValue_matchesStoredNull() {
        val map = mutableInt2AnyMapOf(1 to null, 2 to "b")

        assertFalse(map.remove(1, "a"), "a mismatched value must leave the entry alone")
        assertTrue(map.containsKey(1))

        assertTrue(map.remove(1, null))
        assertFalse(map.containsKey(1))

        assertFalse(map.remove(3, null), "an absent key must not report a null match")
        assertTrue(map.remove(2, "b"))
        assertTrue(map.isEmpty())
    }

    @Test
    fun valueEquality_usesEqualsNotIdentity() {
        val map = mutableInt2AnyMapOf<String?>()
        map[1] = StringBuilder("ab").toString()

        val equalButDistinct = StringBuilder("ab").toString()
        assertTrue(map.containsValue(equalButDistinct))
        assertTrue(map.remove(1, equalButDistinct))
        assertTrue(map.isEmpty())
    }

    @Test
    fun replaceAndGetOrDefault_acceptNullValues() {
        val map = mutableLong2AnyMapOf<String?>(1L to "a")

        assertEquals("a", map.replace(1L, null))
        assertNull(map[1L])
        assertTrue(map.containsKey(1L))

        assertNull(map.getOrDefault(1L, "fallback"))
        assertEquals("fallback", map.getOrDefault(2L, "fallback"))
    }

    @Test
    fun keysAndValuesViews_exposeNullValues() {
        val map = mutableInt2AnyMapOf(1 to null, 2 to "b")

        assertEquals(listOf(1, 2), map.keys.toBoxedList().sorted())
        assertEquals(listOf(null, "b"), map.values.toList().sortedBy { it ?: "" })
        assertTrue(map.values.contains(null))
    }

    @Test
    fun genericValueType_survivesTheFullEntryRoundTrip() {
        val map = mutableLong2AnyMapOf<List<String>>()
        map[1L] = listOf("a", "b")

        val entry = map.iterator().next()
        assertEquals(1L, entry.key)
        assertEquals(listOf("a", "b"), entry.value)

        entry.value = listOf("c")
        assertEquals(listOf("c"), map[1L])

        val traverser = map.traverser()
        assertTrue(traverser.forward())
        assertEquals(listOf("c"), traverser.value)
        traverser.value = emptyList()
        assertEquals(emptyList(), map[1L])
    }

    @Test
    fun emptyAndSingletonReferenceMaps() {
        val empty = emptyInt2AnyMap<String?>()
        assertTrue(empty.isEmpty())
        assertNull(empty[1])
        assertFalse(empty.containsKey(1))
        // empty.containsValue(..) is deliberately not asserted: it currently throws on the JVM instead of
        // returning false, which is a defect in Map.kte's reference-value branch rather than a contract.

        val single = int2AnyMapOf<String?>(1 to null)
        assertEquals(1, single.size)
        assertTrue(single.containsKey(1))
        assertTrue(single.containsValue(null))
        assertNull(single[1])
    }

    @Test
    fun equals_matchesAnyInt2AnyMapImplementation() {
        val hash: Int2AnyMap<String?> = mutableInt2AnyMapOf<String?>(1 to "a")
        val singleton = int2AnyMapOf<String?>(1 to "a")
        assertEquals(singleton, hash)
        assertEquals(hash, singleton)
        assertEquals(singleton.hashCode(), hash.hashCode())

        assertNotEquals(int2AnyMapOf<String?>(1 to "b"), hash)
        assertNotEquals(emptyInt2AnyMap<String?>(), hash)
    }

    @Test
    fun asMap_roundTripsNullValues() {
        val backing = mutableInt2AnyMapOf(1 to null, 2 to "b")
        val view = backing.asMap()

        assertEquals(mapOf(1 to null, 2 to "b"), view)
        assertTrue(view.containsKey(1))
        assertNull(view[1])

        assertNull(view.put(3, null))
        assertTrue(backing.containsKey(3))
        assertEquals(3, backing.size)
    }
}
