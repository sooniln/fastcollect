package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntArrayDeque
import io.github.sooniln.fastcollect.ints.IntHashSet
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
 * A JVM specific benchmark which measures the performance of various list libraries.
 */
@Fork(1)
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
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

        @Param("3000", "12000", "48000", "192000", "768000", "3072000")
        open var size: Int = 192000

        var index = 0
        lateinit var inElements: IntArray

        lateinit var fastcollect: IntArrayDeque

        private fun generateInElements(size: Int) {
            inElements = IntArray(size)
            generateRandomInElements(rnd, inElements)
        }

        @Setup(Level.Trial)
        open fun setupTrial() {
            generateInElements(size)

            fastcollect = IntArrayDeque(size).apply { for (e in inElements) add(e) }
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
        }
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 20, batchSize = 5000000)
    @Measurement(iterations = 40, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun add(s: EmptyState) = s.fastcollect.add(s.nextInKey())

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun grow(s: ReadState) = IntArrayDeque().apply { repeat(s.size) { add(s.inElements[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun search(s: ReadState): Int {
        val element = s.nextWrappingInKey()
        return s.fastcollect.indexOf(element) + s.fastcollect.lastIndexOf(element)
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun iterate(s: ReadState, bh: Blackhole) {
        for (v in s.fastcollect) {
            bh.consume(v)
        }
    }
}
