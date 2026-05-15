package io.github.sooniln.fastcollect

import androidx.collection.MutableIntSet
import io.github.bluuewhale.hashsmith.SwissSet
import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.infra.IterationParams
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * A JVM specific benchmark which measures the performance of various set libraries.
 */
@Fork(1, jvmArgsAppend = ["--add-modules", "jdk.incubator.vector"])
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class SetBenchmark {

    companion object {
        private fun generateRandomInOutKeys(rnd: Random, inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            val inSet = IntOpenHashSet(inKeys.size)
            while (inSet.size < inKeys.size) {
                val key = rnd.nextInt()
                if (!inSet.contains(key)) {
                    inKeys[inSet.size] = key
                    inSet.add(key)
                }
            }

            var i = 0
            while (i < inKeys.size) {
                val r = rnd.nextInt()
                if (!inSet.contains(r)) {
                    outKeys[i++] = r
                }
            }
        }

        private fun generateSequentialLowInOutKeys(inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            repeat(inKeys.size) { inKeys[it] = it + 1 }
            repeat(outKeys.size) { outKeys[it] = inKeys[it] + inKeys.size + 1 }
        }

        private fun generateSequentialHighInOutKeys(inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            repeat(inKeys.size) { inKeys[it] = Int.MAX_VALUE - it }
            repeat(outKeys.size) { outKeys[it] = inKeys[it] - inKeys.size - 1 }
        }

        private fun generateEvenLowInOutKeys(inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            repeat(inKeys.size) { inKeys[it] = 2*it + 2 }
            repeat(outKeys.size) { outKeys[it] = 2*it + 1 }
        }

        private fun generateEvenHighInOutKeys(inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            repeat(inKeys.size) { inKeys[it] = Int.MAX_VALUE - 1 - 2*it }
            repeat(outKeys.size) { outKeys[it] = Int.MAX_VALUE - 2*it }
        }

        private fun generatePartitionLowInOutKeys(inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            repeat(inKeys.size/2) { inKeys[it] = 2*it + 2 }
            repeat(inKeys.size/2) { inKeys[it] = 2*it + 1 }
            repeat(outKeys.size) { outKeys[it] = Int.MAX_VALUE - it }
        }

        private fun generatePartitionHighInOutKeys(inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            repeat(inKeys.size/2) { inKeys[it] = Int.MAX_VALUE - 2*it }
            repeat(inKeys.size/2) { inKeys[it] = Int.MAX_VALUE - 2*it - 1 }
            repeat(outKeys.size) { outKeys[it] = it + 1 }
        }

        private fun generateHighBitsInOutKeys(inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            repeat(inKeys.size) { inKeys[it] = Integer.reverse(it + 1) }
            repeat(outKeys.size) { outKeys[it] = Integer.reverse(it + inKeys.size + 2) }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class ReadState {
        protected val rnd = Random(101)

        //@Param("seqLow", "seqHigh", "random", "evenLow", "evenHigh", "partitionLow", "partitionHigh", "highBits")
        var order: String = "random"

        @Param("3000", "12000", "48000", "192000", "768000", "3072000", "12288000")
        open var size: Int = 192000

        var index = 0
        lateinit var inKeys: IntArray
        lateinit var outKeys: IntArray

        lateinit var fastcollect: IntHashSet
        lateinit var fastutil: IntOpenHashSet
        lateinit var eclipse: org.eclipse.collections.impl.set.mutable.primitive.IntHashSet
        lateinit var androidx: MutableIntSet
        lateinit var hashsmith: SwissSet<Int>
        lateinit var kotlin: HashSet<Int>

        private fun generateInOutKeys(size: Int) {
            inKeys = IntArray(size)
            outKeys = IntArray(size)

            when(order) {
                "seqLow" -> generateSequentialLowInOutKeys(inKeys, outKeys)
                "seqHigh" -> generateSequentialHighInOutKeys(inKeys, outKeys)
                "random" -> generateRandomInOutKeys(rnd, inKeys, outKeys)
                "evenLow" -> generateEvenLowInOutKeys(inKeys, outKeys)
                "evenHigh" -> generateEvenHighInOutKeys(inKeys, outKeys)
                "partitionLow" -> generatePartitionLowInOutKeys(inKeys, outKeys)
                "partitionHigh" -> generatePartitionHighInOutKeys(inKeys, outKeys)
                "highBits" -> generateHighBitsInOutKeys(inKeys, outKeys)
                else -> throw IllegalArgumentException("Unexpected order: $order")
            }
        }

        @Setup(Level.Trial)
        open fun setupTrial() {
            generateInOutKeys(size)

            fastcollect = IntHashSet(size).apply { for (key in inKeys) add(key) }
            fastutil = IntOpenHashSet(size).apply { for (key in inKeys) add(key) }
            eclipse = org.eclipse.collections.impl.set.mutable.primitive.IntHashSet(size).apply { for (key in inKeys) add(key) }
            androidx = MutableIntSet(size).apply { for (key in inKeys) add(key) }
            hashsmith = SwissSet<Int>(size).apply { for (key in inKeys) add(key) }
            kotlin = HashSet<Int>(size).apply { for (key in inKeys) add(key) }
        }

        @Setup(Level.Iteration)
        open fun setupIteration(params: IterationParams) {
            if (params.batchSize > inKeys.size) {
                generateInOutKeys(params.batchSize)
            }

            index = 0
        }

        inline fun nextIndex(): Int {
            val i = index
            if (index == inKeys.size - 1) {
                index = 0
            } else {
                index++
            }
            return i
        }

        inline fun nextInKey() = inKeys[index++]
        inline fun nextWrappingInKey() = inKeys[nextIndex()]
        inline fun nextWrappingOutKey() = outKeys[nextIndex()]
    }

    @State(Scope.Benchmark)
    open class EmptyState: ReadState() {
        @Setup(Level.Iteration)
        override fun setupIteration(params: IterationParams) {
            super.setupIteration(params)
            fastcollect.clear()
            fastutil.clear()
            androidx.clear()
            eclipse.clear()
            kotlin.clear()
        }
    }

    @Benchmark
    fun fastcollectGetHit(s: ReadState) = s.fastcollect.contains(s.nextWrappingInKey())

    @Benchmark
    fun fastcollectGetMiss(s: ReadState) = s.fastcollect.contains(s.nextWrappingOutKey())

    @Benchmark
    fun fastcollectPutHit(s: ReadState) = s.fastcollect.add(s.nextWrappingInKey())

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastcollectPutMiss(s: EmptyState) = s.fastcollect.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectGrow(s: ReadState) = IntHashSet().apply { repeat(s.size) { add(s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectIterate(s: ReadState, bh: Blackhole) {
        for (i in s.fastcollect) {
            bh.consume(i)
        }
    }

    @Benchmark
    fun fastutilGetHit(s: ReadState) = s.fastutil.contains(s.nextWrappingInKey())

    @Benchmark
    fun fastutilGetMiss(s: ReadState) = s.fastutil.contains(s.nextWrappingOutKey())

    @Benchmark
    fun fastutilPutHit(s: ReadState) = s.fastutil.add(s.nextWrappingInKey())

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastutilPutMiss(s: EmptyState) = s.fastutil.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilGrow(s: ReadState) = IntOpenHashSet().apply { repeat(s.size) { add(s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilIterate(s: ReadState, bh: Blackhole) {
        val it = s.fastutil.iterator()
        while (it.hasNext()) {
            bh.consume(it.nextInt())
        }
    }

    @Benchmark
    fun eclipseGetHit(s: ReadState) = s.eclipse.contains(s.nextWrappingInKey())

    @Benchmark
    fun eclipseGetMiss(s: ReadState) = s.eclipse.contains(s.nextWrappingOutKey())

    @Benchmark
    fun eclipsePutHit(s: ReadState) = s.eclipse.add(s.nextWrappingInKey())

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun eclipsePutMiss(s: EmptyState) = s.eclipse.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun eclipseGrow(s: ReadState) = org.eclipse.collections.impl.set.mutable.primitive.IntHashSet().apply { repeat(s.size) { add(s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun eclipseIterate(s: ReadState, bh: Blackhole) {
        val it = s.eclipse.intIterator()
        while (it.hasNext()) {
            bh.consume(it.next())
        }
    }

    @Benchmark
    fun androidxGetHit(s: ReadState) = s.androidx.contains(s.nextWrappingInKey())

    @Benchmark
    fun androidxGetMiss(s: ReadState) = s.androidx.contains(s.nextWrappingOutKey())

    @Benchmark
    fun androidxPutHit(s: ReadState) = s.androidx.add(s.nextWrappingInKey())

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun androidxPutMiss(s: EmptyState) = s.androidx.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun androidxGrow(s: ReadState) = MutableIntSet().apply { repeat(s.size) { add(s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun androidxIterate(s: ReadState): Int {
        var value = 0
        s.androidx.forEach { value = value xor it }
        return value
    }

    @Benchmark
    fun hashsmithGetHit(s: ReadState) = s.hashsmith.contains(s.nextWrappingInKey())

    @Benchmark
    fun hashsmithGetMiss(s: ReadState) = s.hashsmith.contains(s.nextWrappingOutKey())

    @Benchmark
    fun hashsmithPutHit(s: ReadState) = s.hashsmith.add(s.nextWrappingInKey())

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun hashsmithPutMiss(s: EmptyState) = s.hashsmith.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun hashsmithGrow(s: ReadState) = IntOpenHashSet().apply { repeat(s.size) { add(s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun hashsmithIterate(s: ReadState, bh: Blackhole) {
        val it = s.hashsmith.iterator()
        while (it.hasNext()) {
            bh.consume(it.next())
        }
    }

    @Benchmark
    fun kotlinGetHit(s: ReadState) = s.kotlin.contains(s.nextWrappingInKey())

    @Benchmark
    fun kotlinGetMiss(s: ReadState) = s.kotlin.contains(s.nextWrappingOutKey())

    @Benchmark
    fun kotlinPutHit(s: ReadState) = s.kotlin.add(s.nextWrappingInKey())

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kotlinPutMiss(s: EmptyState) = s.kotlin.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinGrow(s: ReadState) = HashSet<Int>().apply { repeat(s.size) { add(s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinIterate(s: ReadState, bh: Blackhole) {
        for (i in s.kotlin) {
            bh.consume(i)
        }
    }
}
