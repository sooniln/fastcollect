package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntHashSet
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
open class SetBenchmark {

    private val rnd = Random(123)

    @Param("30", "34", "126", "130", "1022", "1026", "16382", "16386", "131070", "131074", "1048574", "1048578")
    var size: Int = 0

    lateinit var fastcollect: IntHashSet
    lateinit var kotlin: HashSet<Int>

    lateinit var inKeys: IntArray
    lateinit var outKeys: IntArray

    @Setup
    fun setup() {
        fastcollect = IntHashSet(size, 0.85f)
        kotlin = HashSet(size)

        inKeys = IntArray(size)
        outKeys = IntArray(size)

        repeat(size) { i ->
            val key = rnd.nextInt()
            fastcollect.add(key)
            kotlin.add(key)
            inKeys[i] = key
        }

        var i = 0
        while (i < size) {
            val r = rnd.nextInt(size * 10)
            if (!fastcollect.contains(r)) {
                outKeys[i++] = r
            }
        }
    }

    @Benchmark
    fun fastcollectGetHit(): Int {
        var value = 0
        for (k in inKeys) {
            if (fastcollect.contains(k)) ++value
        }
        return value
    }

    @Benchmark
    fun fastcollectGetMiss(): Int {
        var value = 0
        for (k in outKeys) {
            if (fastcollect.contains(k)) ++value
        }
        return value
    }

    @Benchmark
    fun fastcollectIterate(): Int {
        var c = 0
        for (i in fastcollect) {
            c += i
        }
        return c
    }

    @Benchmark
    fun kotlinGetHit(): Int {
        var value = 0
        for (k in inKeys) {
            if (kotlin.contains(k)) ++value
        }
        return value
    }

    @Benchmark
    fun kotlinGetMiss(): Int {
        var value = 0
        for (k in outKeys) {
            if (kotlin.contains(k)) ++value
        }
        return value
    }

    @Benchmark
    fun jvmIterate(): Int {
        var c = 0
        for (i in kotlin) {
            c += i
        }
        return c
    }
}
