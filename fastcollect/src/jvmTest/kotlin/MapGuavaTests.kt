package io.github.sooniln.fastcollect

import com.google.common.collect.testing.Helpers
import com.google.common.collect.testing.MapTestSuiteBuilder
import com.google.common.collect.testing.SampleElements
import com.google.common.collect.testing.TestMapGenerator
import com.google.common.collect.testing.features.CollectionFeature
import com.google.common.collect.testing.features.CollectionSize
import com.google.common.collect.testing.features.Feature
import com.google.common.collect.testing.features.MapFeature
import com.google.common.collect.testing.testers.MapGetOrDefaultTester
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.AllTests

private val MAP_FEATURES = arrayOf<Feature<*>>(
    CollectionSize.ANY,
    CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
    MapFeature.GENERAL_PURPOSE,
)

// Primitive-value maps don't support null as a getOrDefault default value.
private val SUPPRESSED_MAP_TESTS = listOf(
    MapGetOrDefaultTester::class.java.getMethod("testGetOrDefault_absentNullDefault"),
    MapGetOrDefaultTester::class.java.getMethod("testGetOrDefault_presentNullDefault"),
)

// ============================= Int2Byte =============================

private abstract class AbstractInt2ByteMapGenerator : TestMapGenerator<Int, Byte> {
    override fun samples(): SampleElements<Map.Entry<Int, Byte>> = SampleElements(
        Helpers.mapEntry(1, 10.toByte()),
        Helpers.mapEntry(2, 20.toByte()),
        Helpers.mapEntry(3, 30.toByte()),
        Helpers.mapEntry(4, 40.toByte()),
        Helpers.mapEntry(5, 50.toByte()),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Int, Byte> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Int, Byte> })

    protected abstract fun createMap(entries: Array<Map.Entry<Int, Byte>>): Map<Int, Byte>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Int, Byte>> =
        arrayOfNulls<Map.Entry<Int, Byte>>(length) as Array<Map.Entry<Int, Byte>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Int> =
        arrayOfNulls<Int>(length) as Array<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Byte> =
        arrayOfNulls<Byte>(length) as Array<Byte>

    override fun order(insertionOrder: MutableList<Map.Entry<Int, Byte>>): Iterable<Map.Entry<Int, Byte>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Int2ByteMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractInt2ByteMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Int, Byte>>): MutableMap<Int, Byte> =
                    mutableInt2ByteMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Int2ByteMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Int2Short =============================

private abstract class AbstractInt2ShortMapGenerator : TestMapGenerator<Int, Short> {
    override fun samples(): SampleElements<Map.Entry<Int, Short>> = SampleElements(
        Helpers.mapEntry(1, 10.toShort()),
        Helpers.mapEntry(2, 20.toShort()),
        Helpers.mapEntry(3, 30.toShort()),
        Helpers.mapEntry(4, 40.toShort()),
        Helpers.mapEntry(5, 50.toShort()),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Int, Short> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Int, Short> })

    protected abstract fun createMap(entries: Array<Map.Entry<Int, Short>>): Map<Int, Short>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Int, Short>> =
        arrayOfNulls<Map.Entry<Int, Short>>(length) as Array<Map.Entry<Int, Short>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Int> =
        arrayOfNulls<Int>(length) as Array<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Short> =
        arrayOfNulls<Short>(length) as Array<Short>

    override fun order(insertionOrder: MutableList<Map.Entry<Int, Short>>): Iterable<Map.Entry<Int, Short>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Int2ShortMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractInt2ShortMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Int, Short>>): MutableMap<Int, Short> =
                    mutableInt2ShortMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Int2ShortMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Int2Int =============================

private abstract class AbstractInt2IntMapGenerator : TestMapGenerator<Int, Int> {
    override fun samples(): SampleElements<Map.Entry<Int, Int>> = SampleElements(
        Helpers.mapEntry(1, 10),
        Helpers.mapEntry(2, 20),
        Helpers.mapEntry(3, 30),
        Helpers.mapEntry(4, 40),
        Helpers.mapEntry(5, 50),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Int, Int> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Int, Int> })

    protected abstract fun createMap(entries: Array<Map.Entry<Int, Int>>): Map<Int, Int>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Int, Int>> =
        arrayOfNulls<Map.Entry<Int, Int>>(length) as Array<Map.Entry<Int, Int>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Int> =
        arrayOfNulls<Int>(length) as Array<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Int> =
        arrayOfNulls<Int>(length) as Array<Int>

    override fun order(insertionOrder: MutableList<Map.Entry<Int, Int>>): Iterable<Map.Entry<Int, Int>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Int2IntMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractInt2IntMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Int, Int>>): MutableMap<Int, Int> =
                    mutableInt2IntMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Int2IntMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Int2Long =============================

private abstract class AbstractInt2LongMapGenerator : TestMapGenerator<Int, Long> {
    override fun samples(): SampleElements<Map.Entry<Int, Long>> = SampleElements(
        Helpers.mapEntry(1, 10L),
        Helpers.mapEntry(2, 20L),
        Helpers.mapEntry(3, 30L),
        Helpers.mapEntry(4, 40L),
        Helpers.mapEntry(5, 50L),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Int, Long> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Int, Long> })

    protected abstract fun createMap(entries: Array<Map.Entry<Int, Long>>): Map<Int, Long>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Int, Long>> =
        arrayOfNulls<Map.Entry<Int, Long>>(length) as Array<Map.Entry<Int, Long>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Int> =
        arrayOfNulls<Int>(length) as Array<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    override fun order(insertionOrder: MutableList<Map.Entry<Int, Long>>): Iterable<Map.Entry<Int, Long>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Int2LongMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractInt2LongMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Int, Long>>): MutableMap<Int, Long> =
                    mutableInt2LongMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Int2LongMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Int2Float =============================

private abstract class AbstractInt2FloatMapGenerator : TestMapGenerator<Int, Float> {
    override fun samples(): SampleElements<Map.Entry<Int, Float>> = SampleElements(
        Helpers.mapEntry(1, 1.0f),
        Helpers.mapEntry(2, 2.0f),
        Helpers.mapEntry(3, 3.0f),
        Helpers.mapEntry(4, 4.0f),
        Helpers.mapEntry(5, 5.0f),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Int, Float> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Int, Float> })

    protected abstract fun createMap(entries: Array<Map.Entry<Int, Float>>): Map<Int, Float>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Int, Float>> =
        arrayOfNulls<Map.Entry<Int, Float>>(length) as Array<Map.Entry<Int, Float>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Int> =
        arrayOfNulls<Int>(length) as Array<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Float> =
        arrayOfNulls<Float>(length) as Array<Float>

    override fun order(insertionOrder: MutableList<Map.Entry<Int, Float>>): Iterable<Map.Entry<Int, Float>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Int2FloatMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractInt2FloatMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Int, Float>>): MutableMap<Int, Float> =
                    mutableInt2FloatMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Int2FloatMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Int2Double =============================

private abstract class AbstractInt2DoubleMapGenerator : TestMapGenerator<Int, Double> {
    override fun samples(): SampleElements<Map.Entry<Int, Double>> = SampleElements(
        Helpers.mapEntry(1, 1.0),
        Helpers.mapEntry(2, 2.0),
        Helpers.mapEntry(3, 3.0),
        Helpers.mapEntry(4, 4.0),
        Helpers.mapEntry(5, 5.0),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Int, Double> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Int, Double> })

    protected abstract fun createMap(entries: Array<Map.Entry<Int, Double>>): Map<Int, Double>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Int, Double>> =
        arrayOfNulls<Map.Entry<Int, Double>>(length) as Array<Map.Entry<Int, Double>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Int> =
        arrayOfNulls<Int>(length) as Array<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Double> =
        arrayOfNulls<Double>(length) as Array<Double>

    override fun order(insertionOrder: MutableList<Map.Entry<Int, Double>>): Iterable<Map.Entry<Int, Double>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Int2DoubleMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractInt2DoubleMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Int, Double>>): MutableMap<Int, Double> =
                    mutableInt2DoubleMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Int2DoubleMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Int2Any<String?> =============================

private abstract class AbstractInt2AnyStringMapGenerator : TestMapGenerator<Int, String?> {
    override fun samples(): SampleElements<Map.Entry<Int, String?>> = SampleElements(
        Helpers.mapEntry(1, "a"),
        Helpers.mapEntry(2, "b"),
        Helpers.mapEntry(3, "c"),
        Helpers.mapEntry(4, "d"),
        Helpers.mapEntry(5, "e"),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Int, String?> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Int, String?> })

    protected abstract fun createMap(entries: Array<Map.Entry<Int, String?>>): Map<Int, String?>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Int, String?>> =
        arrayOfNulls<Map.Entry<Int, String?>>(length) as Array<Map.Entry<Int, String?>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Int> = arrayOfNulls<Int>(length) as Array<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<String?> = arrayOfNulls<String?>(length)

    override fun order(insertionOrder: MutableList<Map.Entry<Int, String?>>): Iterable<Map.Entry<Int, String?>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Int2AnyMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractInt2AnyStringMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Int, String?>>): MutableMap<Int, String?> =
                    mutableInt2AnyMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Int2AnyMap")
            .withFeatures(*MAP_FEATURES, MapFeature.ALLOWS_NULL_VALUES)
            .createTestSuite()
    }
}

// ============================= Long2Byte =============================

private abstract class AbstractLong2ByteMapGenerator : TestMapGenerator<Long, Byte> {
    override fun samples(): SampleElements<Map.Entry<Long, Byte>> = SampleElements(
        Helpers.mapEntry(1L, 10.toByte()),
        Helpers.mapEntry(2L, 20.toByte()),
        Helpers.mapEntry(3L, 30.toByte()),
        Helpers.mapEntry(4L, 40.toByte()),
        Helpers.mapEntry(5L, 50.toByte()),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Long, Byte> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Long, Byte> })

    protected abstract fun createMap(entries: Array<Map.Entry<Long, Byte>>): Map<Long, Byte>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Long, Byte>> =
        arrayOfNulls<Map.Entry<Long, Byte>>(length) as Array<Map.Entry<Long, Byte>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Byte> =
        arrayOfNulls<Byte>(length) as Array<Byte>

    override fun order(insertionOrder: MutableList<Map.Entry<Long, Byte>>): Iterable<Map.Entry<Long, Byte>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Long2ByteMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractLong2ByteMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Long, Byte>>): MutableMap<Long, Byte> =
                    mutableLong2ByteMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Long2ByteMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Long2Short =============================

private abstract class AbstractLong2ShortMapGenerator : TestMapGenerator<Long, Short> {
    override fun samples(): SampleElements<Map.Entry<Long, Short>> = SampleElements(
        Helpers.mapEntry(1L, 10.toShort()),
        Helpers.mapEntry(2L, 20.toShort()),
        Helpers.mapEntry(3L, 30.toShort()),
        Helpers.mapEntry(4L, 40.toShort()),
        Helpers.mapEntry(5L, 50.toShort()),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Long, Short> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Long, Short> })

    protected abstract fun createMap(entries: Array<Map.Entry<Long, Short>>): Map<Long, Short>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Long, Short>> =
        arrayOfNulls<Map.Entry<Long, Short>>(length) as Array<Map.Entry<Long, Short>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Short> =
        arrayOfNulls<Short>(length) as Array<Short>

    override fun order(insertionOrder: MutableList<Map.Entry<Long, Short>>): Iterable<Map.Entry<Long, Short>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Long2ShortMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractLong2ShortMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Long, Short>>): MutableMap<Long, Short> =
                    mutableLong2ShortMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Long2ShortMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Long2Int =============================

private abstract class AbstractLong2IntMapGenerator : TestMapGenerator<Long, Int> {
    override fun samples(): SampleElements<Map.Entry<Long, Int>> = SampleElements(
        Helpers.mapEntry(1L, 10),
        Helpers.mapEntry(2L, 20),
        Helpers.mapEntry(3L, 30),
        Helpers.mapEntry(4L, 40),
        Helpers.mapEntry(5L, 50),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Long, Int> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Long, Int> })

    protected abstract fun createMap(entries: Array<Map.Entry<Long, Int>>): Map<Long, Int>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Long, Int>> =
        arrayOfNulls<Map.Entry<Long, Int>>(length) as Array<Map.Entry<Long, Int>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Int> =
        arrayOfNulls<Int>(length) as Array<Int>

    override fun order(insertionOrder: MutableList<Map.Entry<Long, Int>>): Iterable<Map.Entry<Long, Int>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Long2IntMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractLong2IntMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Long, Int>>): MutableMap<Long, Int> =
                    mutableLong2IntMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Long2IntMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Long2Long =============================

private abstract class AbstractLong2LongMapGenerator : TestMapGenerator<Long, Long> {
    override fun samples(): SampleElements<Map.Entry<Long, Long>> = SampleElements(
        Helpers.mapEntry(1L, 10L),
        Helpers.mapEntry(2L, 20L),
        Helpers.mapEntry(3L, 30L),
        Helpers.mapEntry(4L, 40L),
        Helpers.mapEntry(5L, 50L),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Long, Long> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Long, Long> })

    protected abstract fun createMap(entries: Array<Map.Entry<Long, Long>>): Map<Long, Long>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Long, Long>> =
        arrayOfNulls<Map.Entry<Long, Long>>(length) as Array<Map.Entry<Long, Long>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    override fun order(insertionOrder: MutableList<Map.Entry<Long, Long>>): Iterable<Map.Entry<Long, Long>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Long2LongMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractLong2LongMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Long, Long>>): MutableMap<Long, Long> =
                    mutableLong2LongMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Long2LongMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Long2Float =============================

private abstract class AbstractLong2FloatMapGenerator : TestMapGenerator<Long, Float> {
    override fun samples(): SampleElements<Map.Entry<Long, Float>> = SampleElements(
        Helpers.mapEntry(1L, 1.0f),
        Helpers.mapEntry(2L, 2.0f),
        Helpers.mapEntry(3L, 3.0f),
        Helpers.mapEntry(4L, 4.0f),
        Helpers.mapEntry(5L, 5.0f),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Long, Float> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Long, Float> })

    protected abstract fun createMap(entries: Array<Map.Entry<Long, Float>>): Map<Long, Float>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Long, Float>> =
        arrayOfNulls<Map.Entry<Long, Float>>(length) as Array<Map.Entry<Long, Float>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Float> =
        arrayOfNulls<Float>(length) as Array<Float>

    override fun order(insertionOrder: MutableList<Map.Entry<Long, Float>>): Iterable<Map.Entry<Long, Float>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Long2FloatMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractLong2FloatMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Long, Float>>): MutableMap<Long, Float> =
                    mutableLong2FloatMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Long2FloatMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Long2Double =============================

private abstract class AbstractLong2DoubleMapGenerator : TestMapGenerator<Long, Double> {
    override fun samples(): SampleElements<Map.Entry<Long, Double>> = SampleElements(
        Helpers.mapEntry(1L, 1.0),
        Helpers.mapEntry(2L, 2.0),
        Helpers.mapEntry(3L, 3.0),
        Helpers.mapEntry(4L, 4.0),
        Helpers.mapEntry(5L, 5.0),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Long, Double> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Long, Double> })

    protected abstract fun createMap(entries: Array<Map.Entry<Long, Double>>): Map<Long, Double>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Long, Double>> =
        arrayOfNulls<Map.Entry<Long, Double>>(length) as Array<Map.Entry<Long, Double>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<Double> =
        arrayOfNulls<Double>(length) as Array<Double>

    override fun order(insertionOrder: MutableList<Map.Entry<Long, Double>>): Iterable<Map.Entry<Long, Double>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Long2DoubleMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractLong2DoubleMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Long, Double>>): MutableMap<Long, Double> =
                    mutableLong2DoubleMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Long2DoubleMap")
            .withFeatures(*MAP_FEATURES)
            .suppressing(SUPPRESSED_MAP_TESTS)
            .createTestSuite()
    }
}

// ============================= Long2Any<String?> =============================

private abstract class AbstractLong2AnyStringMapGenerator : TestMapGenerator<Long, String?> {
    override fun samples(): SampleElements<Map.Entry<Long, String?>> = SampleElements(
        Helpers.mapEntry(1L, "a"),
        Helpers.mapEntry(2L, "b"),
        Helpers.mapEntry(3L, "c"),
        Helpers.mapEntry(4L, "d"),
        Helpers.mapEntry(5L, "e"),
    )

    @Suppress("UNCHECKED_CAST")
    override fun create(vararg elements: Any): Map<Long, String?> =
        createMap(Array(elements.size) { index -> elements[index] as Map.Entry<Long, String?> })

    protected abstract fun createMap(entries: Array<Map.Entry<Long, String?>>): Map<Long, String?>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Map.Entry<Long, String?>> =
        arrayOfNulls<Map.Entry<Long, String?>>(length) as Array<Map.Entry<Long, String?>>

    @Suppress("UNCHECKED_CAST")
    override fun createKeyArray(length: Int): Array<Long> =
        arrayOfNulls<Long>(length) as Array<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createValueArray(length: Int): Array<String?> =
        arrayOfNulls<String?>(length)

    override fun order(insertionOrder: MutableList<Map.Entry<Long, String?>>): Iterable<Map.Entry<Long, String?>> =
        insertionOrder
}

@RunWith(AllTests::class)
class Long2AnyMapGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = MapTestSuiteBuilder
            .using(object : AbstractLong2AnyStringMapGenerator() {
                override fun createMap(entries: Array<Map.Entry<Long, String?>>): MutableMap<Long, String?> =
                    mutableLong2AnyMapOf(*entries.map { it.key to it.value }.toTypedArray()).asMutableMap()
            })
            .named("Long2AnyMap")
        .withFeatures(*MAP_FEATURES, MapFeature.ALLOWS_NULL_VALUES)
        .createTestSuite()
    }
}
