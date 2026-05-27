package io.github.sooniln.fastcollect

import com.google.common.collect.testing.SampleElements
import com.google.common.collect.testing.SetTestSuiteBuilder
import com.google.common.collect.testing.TestSetGenerator
import com.google.common.collect.testing.features.CollectionFeature
import com.google.common.collect.testing.features.CollectionSize
import com.google.common.collect.testing.features.Feature
import io.github.sooniln.fastcollect.ints.*
import io.github.sooniln.fastcollect.longs.*
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.AllTests

private val SET_FEATURES = arrayOf<Feature<*>>(
    CollectionSize.ANY,
    CollectionFeature.ALLOWS_NULL_QUERIES,
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
                    mutableIntSetOf(*elements).asMutableSet()
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
                    mutableLongSetOf(*elements).asMutableSet()
            })
            .named("LongSet")
            .withFeatures(*SET_FEATURES)
            .createTestSuite()
    }
}
