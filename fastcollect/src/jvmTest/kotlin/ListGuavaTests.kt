package io.github.sooniln.fastcollect

import com.google.common.collect.testing.ListTestSuiteBuilder
import com.google.common.collect.testing.SampleElements
import com.google.common.collect.testing.TestListGenerator
import com.google.common.collect.testing.features.CollectionFeature
import com.google.common.collect.testing.features.CollectionSize
import com.google.common.collect.testing.features.Feature
import com.google.common.collect.testing.features.ListFeature
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.AllTests

private val LIST_FEATURES = arrayOf<Feature<*>>(
    CollectionSize.ANY,
    CollectionFeature.ALLOWS_NULL_QUERIES,
    CollectionFeature.GENERAL_PURPOSE,
    ListFeature.GENERAL_PURPOSE,
)

// <type>ListOf() and <type>Array.as<Type>List() both hand back read-only implementations - EmptyIntList,
// SingletonIntList, a defensively copied deque, or the array wrapper - so they only claim the read half of the
// contract. (The array wrapper reflects later writes to the array it was handed, but exposes no mutators of its
// own; IntListDefaultsTests covers that aliasing.)
private val READ_ONLY_LIST_FEATURES = arrayOf<Feature<*>>(
    CollectionSize.ANY,
    CollectionFeature.ALLOWS_NULL_QUERIES,
)


// ============================= Byte =============================

private abstract class TestByteListGenerator : TestListGenerator<Byte> {
    override fun samples(): SampleElements<Byte> =
        SampleElements(1.toByte(), 2.toByte(), 3.toByte(), 4.toByte(), 5.toByte())

    override fun create(vararg elements: Any): List<Byte> =
        createList(elements.map { it as Byte }.toByteArray())

    protected abstract fun createList(elements: ByteArray): List<Byte>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Byte> = arrayOfNulls<Byte>(length) as Array<Byte>

    override fun order(insertionOrder: MutableList<Byte>): Iterable<Byte> = insertionOrder
}

@RunWith(AllTests::class)
class ByteListGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = ListTestSuiteBuilder
            .using(object : TestByteListGenerator() {
                override fun createList(elements: ByteArray): MutableList<Byte> =
                    mutableByteListOf(*elements).asList()
            })
            .named("ByteList")
            .withFeatures(*LIST_FEATURES)
            .createTestSuite()
    }
}

// ============================= Int =============================

private abstract class TestIntListGenerator : TestListGenerator<Int> {
    override fun samples(): SampleElements<Int> = SampleElements(1, 2, 3, 4, 5)

    override fun create(vararg elements: Any): List<Int> =
        createList(elements.map { it as Int }.toIntArray())

    protected abstract fun createList(elements: IntArray): List<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Int> = arrayOfNulls<Int>(length) as Array<Int>

    override fun order(insertionOrder: MutableList<Int>): Iterable<Int> = insertionOrder
}

@RunWith(AllTests::class)
class IntListGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = ListTestSuiteBuilder
            .using(object : TestIntListGenerator() {
                override fun createList(elements: IntArray): MutableList<Int> =
                    mutableIntListOf(*elements).asList()
            })
            .named("IntList")
            .withFeatures(*LIST_FEATURES)
            .createTestSuite()
    }
}

// ============================= Long =============================

private abstract class TestLongListGenerator : TestListGenerator<Long> {
    override fun samples(): SampleElements<Long> = SampleElements(1L, 2L, 3L, 4L, 5L)

    override fun create(vararg elements: Any): List<Long> =
        createList(elements.map { it as Long }.toLongArray())

    protected abstract fun createList(elements: LongArray): List<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Long> = arrayOfNulls<Long>(length) as Array<Long>

    override fun order(insertionOrder: MutableList<Long>): Iterable<Long> = insertionOrder
}

@RunWith(AllTests::class)
class LongListGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = ListTestSuiteBuilder
            .using(object : TestLongListGenerator() {
                override fun createList(elements: LongArray): MutableList<Long> =
                    mutableLongListOf(*elements).asList()
            })
            .named("LongList")
            .withFeatures(*LIST_FEATURES)
            .createTestSuite()
    }
}

// ============================= Float =============================

private abstract class TestFloatListGenerator : TestListGenerator<Float> {
    override fun samples(): SampleElements<Float> = SampleElements(1.0f, 2.0f, 3.0f, 4.0f, 5.0f)

    override fun create(vararg elements: Any): List<Float> =
        createList(elements.map { it as Float }.toFloatArray())

    protected abstract fun createList(elements: FloatArray): List<Float>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Float> = arrayOfNulls<Float>(length) as Array<Float>

    override fun order(insertionOrder: MutableList<Float>): Iterable<Float> = insertionOrder
}

@RunWith(AllTests::class)
class FloatListGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = ListTestSuiteBuilder
            .using(object : TestFloatListGenerator() {
                override fun createList(elements: FloatArray): MutableList<Float> =
                    mutableFloatListOf(*elements).asList()
            })
            .named("FloatList")
            .withFeatures(*LIST_FEATURES)
            .createTestSuite()
    }
}

// ============================= Double =============================

private abstract class TestDoubleListGenerator : TestListGenerator<Double> {
    override fun samples(): SampleElements<Double> = SampleElements(1.0, 2.0, 3.0, 4.0, 5.0)

    override fun create(vararg elements: Any): List<Double> =
        createList(elements.map { it as Double }.toDoubleArray())

    protected abstract fun createList(elements: DoubleArray): List<Double>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Double> = arrayOfNulls<Double>(length) as Array<Double>

    override fun order(insertionOrder: MutableList<Double>): Iterable<Double> = insertionOrder
}

@RunWith(AllTests::class)
class DoubleListGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = ListTestSuiteBuilder
            .using(object : TestDoubleListGenerator() {
                override fun createList(elements: DoubleArray): MutableList<Double> =
                    mutableDoubleListOf(*elements).asList()
            })
            .named("DoubleList")
            .withFeatures(*LIST_FEATURES)
            .createTestSuite()
    }
}

// ================= the other IntList implementations =================

// The suites above all run over <type>ArrayDeque. These two reach the immutable and array-backed implementations,
// which are otherwise never checked against the java.util.List contract. Int is enough: what differs here is the
// implementation, not the element type.

@RunWith(AllTests::class)
class ReadOnlyIntListGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = ListTestSuiteBuilder
            .using(object : TestIntListGenerator() {
                override fun createList(elements: IntArray): List<Int> = intListOf(*elements).asList()
            })
            .named("IntList[read-only]")
            .withFeatures(*READ_ONLY_LIST_FEATURES)
            .createTestSuite()
    }
}

@RunWith(AllTests::class)
class ArrayBackedIntListGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = ListTestSuiteBuilder
            .using(object : TestIntListGenerator() {
                override fun createList(elements: IntArray): List<Int> = elements.asIntList().asList()
            })
            .named("IntList[array-backed]")
            .withFeatures(*READ_ONLY_LIST_FEATURES)
            .createTestSuite()
    }
}
