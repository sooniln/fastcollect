package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.Int2IntHashMap
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
@Warmup(iterations = 3, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = BenchmarkTimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
open class MapBenchmark {

    private val rnd = Random(123)

    @Param("16", "100", "10000", "1000000")
    var size: Int = 0

    lateinit var fastcollect: Int2IntHashMap
    lateinit var kotlin: HashMap<Int, Int>

    lateinit var inKeys: IntArray
    lateinit var outKeys: IntArray

    @Setup
    fun setup() {
        fastcollect = Int2IntHashMap(size)
        kotlin = HashMap(size)

        inKeys = IntArray(size)
        outKeys = IntArray(size)

        repeat(size) { i ->
            val key = rnd.nextInt()
            val value = rnd.nextInt()
            fastcollect[key] = value
            kotlin[key] = value
            inKeys[i] = key
        }

        var i = 0
        while (i < size) {
            val r = rnd.nextInt(size * 10)
            if (!fastcollect.containsKey(r)) {
                outKeys[i++] = r
            }
        }
    }

    @Benchmark
    fun fastcollectGetHit(): Int {
        var value = 0
        for (e in inKeys) {
            value += fastcollect.lookup(e)
        }
        return value
    }

    @Benchmark
    fun fastcollectGetMiss(): Int {
        var value = 0
        for (e in outKeys) {
            value += fastcollect.lookup(e)
        }
        return value
    }

    @Benchmark
    fun fastcollectIterateKeys(): Int {
        var c = 0
        for (e in fastcollect.keys) {
            c += e
        }
        return c
    }

    @Benchmark
    fun fastcollectIterateValues(): Int {
        var c = 0
        for (e in fastcollect.values) {
            c += e
        }
        return c
    }

    @Benchmark
    fun fastcollectIterate(): Int {
        var c = 0
        for (e in fastcollect.iterator()) {
            c += e.key() + e.value()
        }
        return c
    }

    @Benchmark
    fun fastcollectIterateFast(): Int {
        var c = 0
        for (e in fastcollect.fastIterator()) {
            c += e.key() + e.value()
        }
        return c
    }

    @Benchmark
    fun kotlinGetHit(): Int {
        var value = 0
        for (e in inKeys) {
            value += kotlin.getValue(e)
        }
        return value
    }

    @Benchmark
    fun kotlinGetMiss(): Int {
        var value = 0
        for (e in outKeys) {
            val v = kotlin[e]
            if (v != null) {
                value += v
            }
        }
        return value
    }

    @Benchmark
    fun kotlinIterateKeys(): Int {
        var c = 0
        for (e in kotlin.keys) {
            c += e
        }
        return c
    }

    @Benchmark
    fun kotlinIterateValues(): Int {
        var c = 0
        for (e in kotlin.values) {
            c += e
        }
        return c
    }

    @Benchmark
    fun kotlinIterate(): Int {
        var c = 0
        for (e in kotlin) {
            c += e.key + e.value
        }
        return c
    }
}
