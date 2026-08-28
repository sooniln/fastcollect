package io.github.sooniln.fastcollect

import com.google.common.collect.testing.SampleElements
import com.google.common.collect.testing.SetTestSuiteBuilder
import com.google.common.collect.testing.TestSetGenerator
import com.google.common.collect.testing.features.CollectionFeature
import com.google.common.collect.testing.features.CollectionSize
import com.google.common.collect.testing.features.Feature
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.AllTests

private val SET_FEATURES = arrayOf<Feature<*>>(
    CollectionSize.ANY,
    CollectionFeature.SUPPORTS_ADD,
    CollectionFeature.SUPPORTS_REMOVE,
    CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
)

// ============================= Int =============================

private abstract class TestIntSetGenerator : TestSetGenerator<Int> {
    override fun samples(): SampleElements<Int> = SampleElements(1, 2, 3, 4, 5)

    override fun create(vararg elements: Any): Set<Int> =
        createSet(elements.map { it as Int }.toIntArray())

    protected abstract fun createSet(elements: IntArray): Set<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Int> = arrayOfNulls<Int>(length) as Array<Int>

    override fun order(insertionOrder: MutableList<Int>): Iterable<Int> = insertionOrder
}

@RunWith(AllTests::class)
class IntSetGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = SetTestSuiteBuilder
            .using(object : TestIntSetGenerator() {
                override fun createSet(elements: IntArray): MutableSet<Int> =
                    mutableIntSetOf(*elements).asSet()
            })
            .named("IntSet")
            .withFeatures(*SET_FEATURES)
            .createTestSuite()
    }
}

// ============================= Long =============================

private abstract class TestLongSetGenerator : TestSetGenerator<Long> {
    override fun samples(): SampleElements<Long> = SampleElements(1L, 2L, 3L, 4L, 5L)

    override fun create(vararg elements: Any): Set<Long> =
        createSet(elements.map { it as Long }.toLongArray())

    protected abstract fun createSet(elements: LongArray): Set<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Long> = arrayOfNulls<Long>(length) as Array<Long>

    override fun order(insertionOrder: MutableList<Long>): Iterable<Long> = insertionOrder
}

@RunWith(AllTests::class)
class LongSetGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = SetTestSuiteBuilder
            .using(object : TestLongSetGenerator() {
                override fun createSet(elements: LongArray): MutableSet<Long> =
                    mutableLongSetOf(*elements).asSet()
            })
            .named("LongSet")
            .withFeatures(*SET_FEATURES)
            .createTestSuite()
    }
}

// ============================= Float =============================

// Float and Double are where the library's raw-bit element equality can diverge from boxed equals, so running the
// java.util.Set contract over them is worth more here than anywhere else. The samples stay away from NaN and -0.0
// (Guava compares with equals(), not equalsRaw) - those cases are pinned in FloatDoubleSemanticsTests.
private abstract class TestFloatSetGenerator : TestSetGenerator<Float> {
    override fun samples(): SampleElements<Float> = SampleElements(1f, 2f, 3f, 4f, 5f)

    override fun create(vararg elements: Any): Set<Float> =
        createSet(elements.map { it as Float }.toFloatArray())

    protected abstract fun createSet(elements: FloatArray): Set<Float>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Float> = arrayOfNulls<Float>(length) as Array<Float>

    override fun order(insertionOrder: MutableList<Float>): Iterable<Float> = insertionOrder
}

@RunWith(AllTests::class)
class FloatSetGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = SetTestSuiteBuilder
            .using(object : TestFloatSetGenerator() {
                override fun createSet(elements: FloatArray): MutableSet<Float> =
                    mutableFloatSetOf(*elements).asSet()
            })
            .named("FloatSet")
            .withFeatures(*SET_FEATURES)
            .createTestSuite()
    }
}

// ============================= Double =============================

private abstract class TestDoubleSetGenerator : TestSetGenerator<Double> {
    override fun samples(): SampleElements<Double> = SampleElements(1.0, 2.0, 3.0, 4.0, 5.0)

    override fun create(vararg elements: Any): Set<Double> =
        createSet(elements.map { it as Double }.toDoubleArray())

    protected abstract fun createSet(elements: DoubleArray): Set<Double>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Double> = arrayOfNulls<Double>(length) as Array<Double>

    override fun order(insertionOrder: MutableList<Double>): Iterable<Double> = insertionOrder
}

@RunWith(AllTests::class)
class DoubleSetGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = SetTestSuiteBuilder
            .using(object : TestDoubleSetGenerator() {
                override fun createSet(elements: DoubleArray): MutableSet<Double> =
                    mutableDoubleSetOf(*elements).asSet()
            })
            .named("DoubleSet")
            .withFeatures(*SET_FEATURES)
            .createTestSuite()
    }
}
