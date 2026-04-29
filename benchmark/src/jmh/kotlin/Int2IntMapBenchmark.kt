package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.Int2IntHashMap
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
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
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class Int2IntMapBenchmark {

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

        @Param(/*"5",*/ "31", "1000", /*"10000",*/ "1000000")
        var size: Int = 0

        lateinit var fastutil: Int2IntOpenHashMap
        lateinit var fastcollect: Int2IntHashMap
        lateinit var jvm: HashMap<Int, Int>

        lateinit var inElements: IntArray
        lateinit var outElements: IntArray

        @Setup(Level.Trial)
        fun setup() {
            fastutil = Int2IntOpenHashMap(size)
            fastcollect = Int2IntHashMap(size)
            jvm = HashMap(size)

            inElements = IntArray(size)
            outElements = IntArray(size)

            repeat(size) { i ->
                val key = rnd.nextInt()
                val value = rnd.nextInt()
                fastutil[key] = value
                fastcollect[key] = value
                jvm[key] = value
                inElements[i] = value
            }

            var i = 0
            while (i < size) {
                val r = rnd.nextInt(size * 10)
                if (!fastcollect.containsKey(r)) {
                    outElements[i++] = r
                }
            }
        }
    }

    @Benchmark
    fun fastutilGetHit(s: ReadState): Int {
        var c = 0
        for (i in s.inElements) {
            c += s.fastutil[i]
        }
        return c
    }

    @Benchmark
    fun fastcollectGetHit(s: ReadState): Int {
        var c = 0
        for (i in s.inElements) {
            c += s.fastcollect.lookup(i)
        }
        return c
    }

    @Benchmark
    fun jvmGetHit(s: ReadState): Int {
        var c = 0
        for (i in s.inElements) {
            c += s.jvm[i]!!
        }
        return c
    }

    @Benchmark
    fun fastutilGetMiss(s: ReadState): Int {
        var c = 0
        for (i in s.outElements) {
            if (s.fastutil.containsKey(i)) ++c
        }
        return c
    }

    @Benchmark
    fun fastcollectGetMiss(s: ReadState): Int {
        var c = 0
        for (i in s.outElements) {
            if (s.fastcollect.containsKey(i)) ++c
        }
        return c
    }

    @Benchmark
    fun jvmGetMiss(s: ReadState): Int {
        var c = 0
        for (i in s.outElements) {
            if (s.jvm.containsKey(i)) ++c
        }
        return c
    }

    @Benchmark
    fun fastutilPutHit(s: ReadState): Int2IntOpenHashMap {
        for (i in s.inElements) {
            s.fastutil[i] = i
        }
        return s.fastutil
    }

    @Benchmark
    fun fastcollectPutHit(s: ReadState): Int2IntHashMap {
        for (i in s.inElements) {
            s.fastcollect[i] = i
        }
        return s.fastcollect
    }

    @Benchmark
    fun jvmPutHit(s: ReadState): Map<Int, Int> {
        for (i in s.inElements) {
            s.jvm[i] = i
        }
        return s.jvm
    }

    @Benchmark
    fun fastutilPutMiss(s: ReadState): Int2IntOpenHashMap {
        for (i in s.outElements) {
            s.fastutil[i] = i
        }
        return s.fastutil
    }

    @Benchmark
    fun fastcollectPutMiss(s: ReadState): Int2IntHashMap {
        for (i in s.outElements) {
            s.fastcollect[i] = i
        }
        return s.fastcollect
    }

    @Benchmark
    fun jvmPutMiss(s: ReadState): Map<Int, Int> {
        for (i in s.outElements) {
            s.jvm[i] = i
        }
        return s.jvm
    }

    @Benchmark
    fun fastutilIterateKeys(s: ReadState): Int {
        var c = 0
        val it = s.fastutil.keys.iterator()
        while (it.hasNext()) {
            c += it.nextInt()
        }
        return c
    }

    @Benchmark
    fun fastutilIterateValues(s: ReadState): Int {
        var c = 0
        val it = s.fastutil.values.iterator()
        while (it.hasNext()) {
            c += it.nextInt()
        }
        return c
    }

    @Benchmark
    fun fastutilIterateSlow(s: ReadState): Int {
        var c = 0
        for (e in s.fastutil) {
            c += e.key + e.value
        }
        return c
    }

    @Benchmark
    fun fastutilIterate(s: ReadState): Int {
        var c = 0
        for (e in s.fastutil.int2IntEntrySet()) {
            c += e.intKey + e.intValue
        }
        return c
    }

    @Benchmark
    fun fastutilIterateFast(s: ReadState): Int {
        var c = 0
        for (e in s.fastutil.int2IntEntrySet().fastIterator()) {
            c += e.intKey + e.intValue
        }
        return c
    }

    @Benchmark
    fun fastcollectIterateKeys(s: ReadState): Int {
        var c = 0
        for (e in s.fastcollect.keys) {
            c += e
        }
        return c
    }

    @Benchmark
    fun fastcollectIterateValues(s: ReadState): Int {
        var c = 0
        for (e in s.fastcollect.values) {
            c += e
        }
        return c
    }

    @Benchmark
    fun fastcollectIterateSlow(s: ReadState): Int {
        var c = 0
        for (e in s.fastcollect.entries) {
            c += e.key + e.value
        }
        return c
    }

    @Benchmark
    fun fastcollectIterate(s: ReadState): Int {
        var c = 0
        for (e in s.fastcollect.iterator()) {
            c += e.key() + e.value()
        }
        return c
    }

    @Benchmark
    fun fastcollectIterateFast(s: ReadState): Int {
        var c = 0
        for (e in s.fastcollect.fastIterator()) {
            c += e.key() + e.value()
        }
        return c
    }

    @Benchmark
    fun jvmIterateKeys(s: ReadState): Int {
        var c = 0
        for (e in s.jvm.keys) {
            c += e
        }
        return c
    }

    @Benchmark
    fun jvmIterateValues(s: ReadState): Int {
        var c = 0
        for (e in s.jvm.values) {
            c += e
        }
        return c
    }

    @Benchmark
    fun jvmIterate(s: ReadState): Int {
        var c = 0
        for (e in s.jvm) {
            c += e.key + e.value
        }
        return c
    }
}
