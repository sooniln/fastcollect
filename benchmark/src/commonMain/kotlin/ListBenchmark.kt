package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntArrayDeque
import korlibs.datastructure.IntArrayList
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import kotlin.math.min
import kotlin.random.Random

/**
 * A generalized benchmark which does not measure the performance of any particular APIs, but measures generalized
 * performance for comparison across frameworks on specific platforms.
 */
@Warmup(iterations = 5, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 10, time = 2, timeUnit = BenchmarkTimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
open class ListBenchmark {

    private val rnd = Random(1234)

    @Param("10", "100", "1000", "10000", "100000", "1000000")
    var size: Int = 0

    lateinit var kds: IntArrayList
    lateinit var fastcollect: IntArrayDeque
    lateinit var kotlin: ArrayList<Int>

    lateinit var inElements: IntArray
    lateinit var inElementsSampled: IntArray

    @Setup
    fun setup() {
        kds = IntArrayList(size)
        fastcollect = IntArrayDeque(size)
        kotlin = ArrayList(size)

        inElements = IntArray(size)

        repeat(size) { i ->
            val element = rnd.nextInt()
            kds.add(element)
            fastcollect.add(element)
            kotlin.add(element)
            inElements[i] = element
        }

        inElements.shuffle(rnd)
        inElementsSampled = inElements.copyOf(min(inElements.size, 1000))
    }

    @Benchmark
    fun fastcollectAdd(): IntArrayDeque {
        val list = IntArrayDeque()
        for (e in inElements) {
            list.add(e)
        }
        return list
    }

    @Benchmark
    fun fastcollectIterate(): Int {
        var value = 0
        for (v in fastcollect) {
            value += v
        }
        return value
    }

    @Benchmark
    fun fastcollectSearch(): Int {
        var value = 0
        for (e in inElementsSampled) {
            value += fastcollect.indexOf(e)
        }
        return value
    }

    @Benchmark
    fun kdsAdd(): IntArrayList {
        val list = IntArrayList()
        for (e in inElements) {
            list.add(e)
        }
        return list
    }

    @Benchmark
    fun kdsIterate(): Int {
        var value = 0
        for (v in kds) {
            value += v
        }
        return value
    }

    @Benchmark
    fun kdsSearch(): Int {
        var value = 0
        for (e in inElementsSampled) {
            value += kds.indexOf(e)
        }
        return value
    }

    @Benchmark
    fun kotlinAdd(): ArrayList<Int> {
        val list = ArrayList<Int>()
        for (e in inElements) {
            list.add(e)
        }
        return list
    }

    @Benchmark
    fun kotlinIterate(): Int {
        var value = 0
        for (v in kotlin) {
            value += v
        }
        return value
    }

    @Benchmark
    fun kotlinSearch(): Int {
        var value = 0
        for (e in inElementsSampled) {
            value += kotlin.indexOf(e)
        }
        return value
    }
}
