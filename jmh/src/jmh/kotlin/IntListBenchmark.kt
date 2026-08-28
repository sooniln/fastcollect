package io.github.sooniln.fastcollect

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Timeout
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.random.Random


/**
 * A JVM specific benchmark which measures the performance of various list libraries.
 */
@Fork(1)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class IntListBenchmark {

    @State(Scope.Benchmark)
    open class BaseState {
        protected val rnd = Random(123)

        @Param("3000", "12000", "48000", "192000", "768000", "3072000", "12288000")
        var size: Int = 3000

        lateinit var list: IntArrayList

        var idx = 0
        lateinit var elements: IntArray

        @Setup(Level.Trial)
        open fun setup() {
            list = IntArrayList()

            elements = IntArray(size) { rnd.nextInt() }

            for (e in elements) {
                list.add(e)
            }

            elements.shuffle()
        }

        inline fun <T> nextInElement(crossinline action: BaseState.(Int) -> T): T {
            val t = action(elements[idx])
            if (++idx == elements.size) {
                idx = 0
            }
            return t
        }
    }

    @State(Scope.Benchmark)
    open class EmptyState : BaseState() {
        @Setup(Level.Trial)
        override fun setup() {
            super.setup()
            list.clear()
        }

        inline fun <T> nextClearedInElement(crossinline action: EmptyState.(Int) -> T): T {
            val t = action(elements[idx])
            if (++idx == elements.size) {
                idx = 0
                list.clear()
            }
            return t
        }
    }

    @Benchmark
    fun naiveCopy(state: BaseState): IntArrayList {
        val copy = IntArrayList()
        state.list.foreach { key -> copy.add(key) }
        return copy
    }

    @Benchmark
    fun preAllocatedCopy(state: BaseState) = IntArrayList(state.list)

    @Benchmark
    fun add(state: EmptyState) = state.nextClearedInElement { element -> list.add(element) }

    @Benchmark
    fun indexOf(state: BaseState) = state.nextInElement { key -> list.indexOf(key) }

    @Benchmark
    fun removeAndAdd(state: BaseState) {
        val t = state.list.removeAt(0)
        state.list.add(t)
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun iterate(state: BaseState, bh: Blackhole) {
        for (element in state.list) {
            bh.consume(element)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun foreach(state: BaseState, bh: Blackhole) {
        state.list.foreach { element -> bh.consume(element) }
    }
}
