package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntArrayDeque
import io.github.sooniln.fastcollect.ints.buildIntList
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
@Measurement(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class IntListBenchmark {

    @State(Scope.Benchmark)
    open class CreateState {
        private val rnd = Random(123)

        @Param("1000000")
        var size: Int = 0

        lateinit var elements: IntArray

        @Setup(Level.Trial)
        fun setup() {
            elements = IntArray(size) {
                rnd.nextInt()
            }
        }
    }

    @State(Scope.Benchmark)
    open class ReadState {
        private val rnd = Random(123)

        @Param("1000000")
        var size: Int = 0

        lateinit var fastutil: IntArrayList
        lateinit var fastcollect: IntArrayDeque
        lateinit var jvm: ArrayList<Int>

        lateinit var selectedElements: IntArray

        @Setup(Level.Trial)
        fun setup() {
            fastutil = IntArrayList(size)
            fastcollect = IntArrayDeque(size)
            jvm = ArrayList(size)

            selectedElements = IntArray(size)

            repeat(size) { i ->
                val value = rnd.nextInt()
                fastutil.add(value)
                fastcollect.add(value)
                jvm.add(value)
                selectedElements[i] = value
            }

            selectedElements.shuffle(rnd)
            selectedElements = selectedElements.copyOf((size * .001).toInt())
        }
    }

    @Benchmark
    fun fastutilCreate(s: CreateState): List<Int> {
        val list = IntArrayList()
        for (element in s.elements) {
            list.add(element)
        }
        return list
    }

    @Benchmark
    fun fastcollectCreate(s: CreateState): List<Int> {
        return buildIntList {
            for (element in s.elements) {
                add(element)
            }
        }
    }

    @Benchmark
    fun jvmCreate(s: CreateState): List<Int> {
        val list = IntArrayList()
        for (element in s.elements) {
            list.add(element)
        }
        return list
    }

    @Benchmark
    fun fastutilCreatePreallocate(s: CreateState): List<Int> {
        val list = IntArrayList(s.elements.size)
        for (element in s.elements) {
            list.add(element)
        }
        return list
    }

    @Benchmark
    fun fastcollectCreatePreallocate(s: CreateState): List<Int> {
        return buildIntList(s.elements.size) {
            for (element in s.elements) {
                add(element)
            }
        }
    }

    @Benchmark
    fun jvmCreatePreallocate(s: CreateState): List<Int> {
        val list = IntArrayList(s.size)
        for (element in s.elements) {
            list.add(element)
        }
        return list
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
    fun fastutilIterateSlow(s: ReadState): Int {
        var value = 0
        for (v in s.fastutil.iterator()) {
            value += v
        }
        return value
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
    fun jvmIterate(s: ReadState): Int {
        var value = 0
        for (v in s.jvm) {
            value += v
        }
        return value
    }

    @Benchmark
    fun fastutilSearch(s: ReadState): Int {
        var value = 0
        for (element in s.selectedElements) {
            value += s.fastutil.indexOf(element)
            value += s.fastutil.lastIndexOf(element)
        }
        return value
    }

    @Benchmark
    fun fastcollectSearch(s: ReadState): Int {
        var value = 0
        for (element in s.selectedElements) {
            value += s.fastcollect.indexOf(element)
            value += s.fastcollect.lastIndexOf(element)
        }
        return value
    }

    @Benchmark
    fun jvmSearch(s: ReadState): Int {
        var value = 0
        for (element in s.selectedElements) {
            value += s.jvm.indexOf(element)
            value += s.jvm.lastIndexOf(element)
        }
        return value
    }
}
