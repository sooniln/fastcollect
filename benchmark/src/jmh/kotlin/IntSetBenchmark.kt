package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
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
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class IntSetBenchmark {

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

        @Param("5", "31", /*"1000", "10000", "1000000"*/)
        var size: Int = 0

        lateinit var fastutil: IntOpenHashSet
        lateinit var fastcollect: IntHashSet
        lateinit var jvm: HashSet<Int>

        lateinit var inElements: IntArray
        lateinit var outElements: IntArray

        @Setup(Level.Trial)
        fun setup() {
            fastutil = IntOpenHashSet(size)
            fastcollect = IntHashSet(size)
            jvm = HashSet(size)

            inElements = IntArray(size)
            outElements = IntArray(size)

            repeat(size) { i ->
                val value = rnd.nextInt()
                fastutil.add(value)
                fastcollect.add(value)
                jvm.add(value)
                inElements[i] = value
            }

            var i = 0
            while (i < size) {
                val r = rnd.nextInt(size * 10)
                if (!fastcollect.contains(r)) {
                    outElements[i++] = r
                }
            }
        }
    }

    @Benchmark
    fun fastutilGetHit(s: ReadState): Int {
        var c = 0
        for (i in s.inElements) {
            if (s.fastutil.contains(i)) ++c
        }
        return c
    }

    @Benchmark
    fun fastcollectGetHit(s: ReadState): Int {
        var c = 0
        for (i in s.inElements) {
            if (s.fastcollect.contains(i)) ++c
        }
        return c
    }

    @Benchmark
    fun jvmGetHit(s: ReadState): Int {
        var c = 0
        for (i in s.inElements) {
            if (s.jvm.contains(i)) ++c
        }
        return c
    }

    @Benchmark
    fun fastutilGetMiss(s: ReadState): Int {
        var c = 0
        for (i in s.outElements) {
            if (s.fastutil.contains(i)) ++c
        }
        return c
    }

    @Benchmark
    fun fastcollectGetMiss(s: ReadState): Int {
        var c = 0
        for (i in s.outElements) {
            if (s.fastcollect.contains(i)) ++c
        }
        return c
    }

    @Benchmark
    fun jvmGetMiss(s: ReadState): Int {
        var c = 0
        for (i in s.outElements) {
            if (s.jvm.contains(i)) ++c
        }
        return c
    }

    @Benchmark
    fun fastutilIterate(s: ReadState): Int {
        var c = 0
        val it = s.fastutil.iterator()
        while (it.hasNext()) {
            c += it.nextInt()
        }
        return c
    }

    @Benchmark
    fun fastutilIterateSlow(s: ReadState): Int {
        var c = 0
        for (i in s.fastutil.iterator()) {
            c += i
        }
        return c
    }

    @Benchmark
    fun fastcollectIterate(s: ReadState): Int {
        var c = 0
        for (i in s.fastcollect) {
            c += i
        }
        return c
    }

    @Benchmark
    fun jvmIterate(s: ReadState): Int {
        var c = 0
        for (i in s.jvm) {
            c += i
        }
        return c
    }
}
