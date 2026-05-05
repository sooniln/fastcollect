package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntArrayDeque
import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.IntArrayList
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.infra.IterationParams
import java.util.concurrent.TimeUnit
import kotlin.random.Random


/**
 * A JVM specific benchmark which measures the performance of various list libraries.
 */
@Fork(1)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class ListBenchmark {

    companion object {
        private fun generateRandomInElements(rnd: Random, inKeys: IntArray) {
            val inSet = IntHashSet(inKeys.size)
            while (inSet.size < inKeys.size) {
                val key = rnd.nextInt()
                if (!inSet.contains(key)) {
                    inKeys[inSet.size] = key
                    inSet.add(key)
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class ReadState {
        protected val rnd = Random(123)

        open val size: Int get() = 5000000

        var index = 0
        lateinit var inElements: IntArray

        lateinit var fastcollect: IntArrayDeque
        lateinit var fastutil: IntArrayList
        lateinit var kds: korlibs.datastructure.IntArrayList
        lateinit var kotlin: ArrayList<Int>

        private fun generateInElements(size: Int) {
            inElements = IntArray(size)
            generateRandomInElements(rnd, inElements)
        }

        @Setup(Level.Trial)
        open fun setupTrial() {
            generateInElements(size)

            fastcollect = IntArrayDeque(size).apply { for (e in inElements) add(e) }
            fastutil = IntArrayList(size).apply { for (e in inElements) add(e) }
            kds = korlibs.datastructure.IntArrayList().apply { for (e in inElements) add(e) }
            kotlin = ArrayList<Int>(size).apply { for (e in inElements) add(e) }
        }

        @Setup(Level.Iteration)
        open fun setupIteration(params: IterationParams) {
            if (params.batchSize > inElements.size) {
                generateInElements(params.batchSize)
            }

            index = 0
        }

        inline fun nextIndex(): Int {
            val i = index
            if (index == inElements.size - 1) {
                index = 0
            } else {
                index++
            }
            return i
        }

        inline fun nextInKey() = inElements[index++]
        inline fun nextWrappingInKey() = inElements[nextIndex()]
    }

    @State(Scope.Benchmark)
    open class EmptyState : ReadState() {
        @Setup(Level.Iteration)
        override fun setupIteration(params: IterationParams) {
            super.setupIteration(params)
            fastcollect.clear()
            fastutil.clear()
            kds.clear()
            kotlin.clear()
        }
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 20, batchSize = 5000000)
    @Measurement(iterations = 40, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastcollectAdd(s: EmptyState) = s.fastcollect.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectGrow(s: ReadState) = IntArrayDeque().apply { repeat(s.size) { add(s.inElements[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectSearch(s: ReadState): Int {
        val element = s.nextWrappingInKey()
        return s.fastcollect.indexOf(element) + s.fastcollect.lastIndexOf(element)
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectIterate(s: ReadState, bh: Blackhole) {
        for (v in s.fastcollect) {
            bh.consume(v)
        }
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 20, batchSize = 5000000)
    @Measurement(iterations = 40, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastutilAdd(s: EmptyState) = s.fastutil.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilGrow(s: ReadState) = IntArrayList().apply { repeat(s.size) { add(s.inElements[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilSearch(s: ReadState): Int {
        val element = s.nextWrappingInKey()
        return s.fastutil.indexOf(element) + s.fastutil.lastIndexOf(element)
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilIterate(s: ReadState, bh: Blackhole) {
        val it = s.fastutil.iterator()
        while (it.hasNext()) {
            bh.consume(it.nextInt())
        }
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 20, batchSize = 5000000)
    @Measurement(iterations = 40, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kdsAdd(s: EmptyState) = s.kds.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kdsGrow(s: ReadState) = korlibs.datastructure.IntArrayList().apply { repeat(s.size) { add(s.inElements[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kdsSearch(s: ReadState): Int {
        val element = s.nextWrappingInKey()
        return s.kds.indexOf(element) + s.kds.lastIndexOf(element)
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kdsIterate(s: ReadState, bh: Blackhole) {
        for (v in s.kds) {
            bh.consume(v)
        }
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 20, batchSize = 5000000)
    @Measurement(iterations = 40, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kotlinAdd(s: EmptyState) = s.kotlin.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinGrow(s: ReadState) = ArrayList<Int>().apply { repeat(s.size) { add(s.inElements[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinSearch(s: ReadState): Int {
        val element = s.nextWrappingInKey()
        return s.kotlin.indexOf(element) + s.kotlin.lastIndexOf(element)
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinIterate(s: ReadState, bh: Blackhole) {
        for (v in s.kotlin) {
            bh.consume(v)
        }
    }
}
