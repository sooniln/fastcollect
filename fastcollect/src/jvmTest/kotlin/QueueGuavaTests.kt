package io.github.sooniln.fastcollect

import com.google.common.collect.testing.QueueTestSuiteBuilder
import com.google.common.collect.testing.SampleElements
import com.google.common.collect.testing.TestQueueGenerator
import com.google.common.collect.testing.features.CollectionFeature
import com.google.common.collect.testing.features.CollectionSize
import com.google.common.collect.testing.features.Feature
import junit.framework.TestSuite
import org.junit.runner.RunWith
import org.junit.runners.AllTests
import java.util.Queue

private val QUEUE_FEATURES = arrayOf<Feature<*>>(
    CollectionSize.ANY,
    CollectionFeature.SUPPORTS_ADD,
    CollectionFeature.SUPPORTS_REMOVE,
)

// ============================= Byte =============================

private abstract class TestByteQueueGenerator : TestQueueGenerator<Byte> {
    override fun samples(): SampleElements<Byte> = SampleElements(1.toByte(), 2.toByte(), 3.toByte(), 4.toByte(), 5.toByte())

    override fun create(vararg elements: Any): Queue<Byte> =
        createQueue(elements.map { it as Byte }.toByteArray())

    protected abstract fun createQueue(elements: ByteArray): Queue<Byte>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Byte> = arrayOfNulls<Byte>(length) as Array<Byte>

    override fun order(insertionOrder: MutableList<Byte>): Iterable<Byte> = insertionOrder
}

@RunWith(AllTests::class)
class ByteQueueGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = QueueTestSuiteBuilder
            .using(object : TestByteQueueGenerator() {
                override fun createQueue(elements: ByteArray): Queue<Byte> = BytePriorityQueue(elements).asQueue()
            })
            .named("BytePriorityQueue")
            .withFeatures(*QUEUE_FEATURES)
            .createTestSuite()
    }
}

// ============================= Short =============================

private abstract class TestShortQueueGenerator : TestQueueGenerator<Short> {
    override fun samples(): SampleElements<Short> =
        SampleElements(1.toShort(), 2.toShort(), 3.toShort(), 4.toShort(), 5.toShort())

    override fun create(vararg elements: Any): Queue<Short> =
        createQueue(elements.map { it as Short }.toShortArray())

    protected abstract fun createQueue(elements: ShortArray): Queue<Short>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Short> = arrayOfNulls<Short>(length) as Array<Short>

    override fun order(insertionOrder: MutableList<Short>): Iterable<Short> = insertionOrder
}

@RunWith(AllTests::class)
class ShortQueueGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = QueueTestSuiteBuilder
            .using(object : TestShortQueueGenerator() {
                override fun createQueue(elements: ShortArray): Queue<Short> = ShortPriorityQueue(elements).asQueue()
            })
            .named("ShortPriorityQueue")
            .withFeatures(*QUEUE_FEATURES)
            .createTestSuite()
    }
}

// ============================= Int =============================

private abstract class TestIntQueueGenerator : TestQueueGenerator<Int> {
    override fun samples(): SampleElements<Int> = SampleElements(1, 2, 3, 4, 5)

    override fun create(vararg elements: Any): Queue<Int> =
        createQueue(elements.map { it as Int }.toIntArray())

    protected abstract fun createQueue(elements: IntArray): Queue<Int>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Int> = arrayOfNulls<Int>(length) as Array<Int>

    override fun order(insertionOrder: MutableList<Int>): Iterable<Int> = insertionOrder
}

@RunWith(AllTests::class)
class IntQueueGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = QueueTestSuiteBuilder
            .using(object : TestIntQueueGenerator() {
                override fun createQueue(elements: IntArray): Queue<Int> = IntPriorityQueue(elements).asQueue()
            })
            .named("IntPriorityQueue")
            .withFeatures(*QUEUE_FEATURES)
            .createTestSuite()
    }
}

// ============================= Long =============================

private abstract class TestLongQueueGenerator : TestQueueGenerator<Long> {
    override fun samples(): SampleElements<Long> = SampleElements(1L, 2L, 3L, 4L, 5L)

    override fun create(vararg elements: Any): Queue<Long> =
        createQueue(elements.map { it as Long }.toLongArray())

    protected abstract fun createQueue(elements: LongArray): Queue<Long>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Long> = arrayOfNulls<Long>(length) as Array<Long>

    override fun order(insertionOrder: MutableList<Long>): Iterable<Long> = insertionOrder
}

@RunWith(AllTests::class)
class LongQueueGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = QueueTestSuiteBuilder
            .using(object : TestLongQueueGenerator() {
                override fun createQueue(elements: LongArray): Queue<Long> = LongPriorityQueue(elements).asQueue()
            })
            .named("LongPriorityQueue")
            .withFeatures(*QUEUE_FEATURES)
            .createTestSuite()
    }
}

// ============================= Float =============================

private abstract class TestFloatQueueGenerator : TestQueueGenerator<Float> {
    override fun samples(): SampleElements<Float> = SampleElements(1f, 2f, 3f, 4f, 5f)

    override fun create(vararg elements: Any): Queue<Float> =
        createQueue(elements.map { it as Float }.toFloatArray())

    protected abstract fun createQueue(elements: FloatArray): Queue<Float>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Float> = arrayOfNulls<Float>(length) as Array<Float>

    override fun order(insertionOrder: MutableList<Float>): Iterable<Float> = insertionOrder
}

@RunWith(AllTests::class)
class FloatQueueGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = QueueTestSuiteBuilder
            .using(object : TestFloatQueueGenerator() {
                override fun createQueue(elements: FloatArray): Queue<Float> = FloatPriorityQueue(elements).asQueue()
            })
            .named("FloatPriorityQueue")
            .withFeatures(*QUEUE_FEATURES)
            .createTestSuite()
    }
}

// ============================= Double =============================

private abstract class TestDoubleQueueGenerator : TestQueueGenerator<Double> {
    override fun samples(): SampleElements<Double> = SampleElements(1.0, 2.0, 3.0, 4.0, 5.0)

    override fun create(vararg elements: Any): Queue<Double> =
        createQueue(elements.map { it as Double }.toDoubleArray())

    protected abstract fun createQueue(elements: DoubleArray): Queue<Double>

    @Suppress("UNCHECKED_CAST")
    override fun createArray(length: Int): Array<Double> = arrayOfNulls<Double>(length) as Array<Double>

    override fun order(insertionOrder: MutableList<Double>): Iterable<Double> = insertionOrder
}

@RunWith(AllTests::class)
class DoubleQueueGuavaTest {
    companion object {
        @JvmStatic
        fun suite(): TestSuite = QueueTestSuiteBuilder
            .using(object : TestDoubleQueueGenerator() {
                override fun createQueue(elements: DoubleArray): Queue<Double> =
                    DoublePriorityQueue(elements).asQueue()
            })
            .named("DoublePriorityQueue")
            .withFeatures(*QUEUE_FEATURES)
            .createTestSuite()
    }
}
