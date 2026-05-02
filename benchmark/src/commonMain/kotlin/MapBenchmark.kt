package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.Int2IntHashMap
import korlibs.datastructure.IntIntMap
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
open class MapBenchmark {

    private val rnd = Random(123)

    // size values surrounding backing array size discontinuities (assuming loadFactor of .85)
    @Param("30", "34", "108", "112", "1740", "1744", "13926", "13930", "111411", "111415", "1782580", "1782584")
    var size: Int = 0

    lateinit var kds: IntIntMap
    lateinit var fastcollect: Int2IntHashMap
    lateinit var kotlin: HashMap<Int, Int>

    lateinit var inKeys: IntArray
    lateinit var outKeys: IntArray

    @Setup
    fun setup() {
        kds = IntIntMap(size)
        fastcollect = Int2IntHashMap(size)
        kotlin = HashMap(size)

        inKeys = IntArray(size)
        outKeys = IntArray(size)

        repeat(size) { i ->
            val key = rnd.nextInt()
            val value = rnd.nextInt()
            kds[key] = value
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
    fun fastcollectAdd(): Int2IntHashMap {
        val map = Int2IntHashMap()
        for (e in inKeys) {
            map[e] = 1
        }
        return map
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
    fun kdsAdd(): IntIntMap {
        val map = IntIntMap()
        for (e in inKeys) {
            map[e] = 1
        }
        return map
    }

    @Benchmark
    fun kdsGetHit(): Int {
        var value = 0
        for (e in inKeys) {
            value += kds[e]
        }
        return value
    }

    @Benchmark
    fun kdsGetMiss(): Int {
        var value = 0
        for (e in outKeys) {
            value += kds[e]
        }
        return value
    }

    @Benchmark
    fun kdsIterateKeys(): Int {
        var c = 0
        for (e in kds.keys) {
            c += e
        }
        return c
    }

    @Benchmark
    fun kdsIterateValues(): Int {
        var c = 0
        for (e in kds.values) {
            c += e
        }
        return c
    }

    @Benchmark
    fun kdsIterate(): Int {
        var c = 0
        for (e in kds.entries) {
            c += e.key + e.value
        }
        return c
    }

    @Benchmark
    fun kotlinAdd(): HashMap<Int, Int> {
        val map = HashMap<Int, Int>()
        for (e in inKeys) {
            map[e] = 1
        }
        return map
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
