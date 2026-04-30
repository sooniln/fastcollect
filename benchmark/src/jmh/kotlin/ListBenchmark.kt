package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntArrayDeque
import it.unimi.dsi.fastutil.ints.IntArrayList
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
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Fork(value = 1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 4, time = 2, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class ListBenchmark {

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class ReadState {
        private val rnd = Random(123)

        @Param("16", "100", "10000", "1000000")
        var size: Int = 0

        lateinit var fastutil: IntArrayList
        lateinit var fastcollect: IntArrayDeque
        lateinit var jvm: ArrayList<Int>

        var index = 0
        lateinit var inElements: IntArray

        @Setup(Level.Trial)
        fun setupTrial() {
            fastutil = IntArrayList(size)
            fastcollect = IntArrayDeque(size)
            jvm = ArrayList(size)

            inElements = IntArray(size)

            repeat(size) { i ->
                val element = rnd.nextInt()
                fastutil.add(element)
                fastcollect.add(element)
                jvm.add(element)
                inElements[i] = element
            }

            inElements.shuffle(rnd)
        }

        @Setup(Level.Invocation)
        fun setup() {
            index = 0
        }

        inline fun nextIndex(): Int {
            val i  = index
            if (index == inElements.size - 1) {
                index = 0
            } else {
                index++
            }
            return i
        }

        inline fun nextInElement() = inElements[nextIndex()]
    }

    @Benchmark
    fun fastcollectSearch(s: ReadState): Int {
        val element = s.nextInElement()
        return s.fastcollect.indexOf(element) + s.fastcollect.lastIndexOf(element)
    }

    @Benchmark
    fun fastcollectIterate(s: ReadState): Int {
        var value = 0
        for (v in s.fastcollect) {
            value += v
        }
        return value
    }

    @Benchmark
    fun fastutilSearch(s: ReadState): Int {
        val element = s.nextInElement()
        return s.fastutil.indexOf(element) + s.fastutil.lastIndexOf(element)
    }

    @Benchmark
    fun fastutilIterate(s: ReadState): Int {
        val it = s.fastutil.iterator()
        var value = 0
        while (it.hasNext()) {
            value += it.nextInt()
        }
        return value
    }

    @Benchmark
    fun jvmSearch(s: ReadState): Int {
        val element = s.nextInElement()
        return s.jvm.indexOf(element) + s.jvm.lastIndexOf(element)
    }

    @Benchmark
    fun jvmIterate(s: ReadState): Int {
        var value = 0
        for (v in s.jvm) {
            value += v
        }
        return value
    }
}
