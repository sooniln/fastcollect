package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import korlibs.datastructure.IntSet
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

/**
 * A JVM specific benchmark which measures the performance of various set libraries.
 */
@Fork(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class SetBenchmark {

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class ReadState {
        private val rnd = Random(123)

        @Param("16", "100", "10000", "1000000")
        var size: Int = 0

        lateinit var kds: IntSet
        lateinit var fastutil: IntOpenHashSet
        lateinit var fastcollect: IntHashSet
        lateinit var jvm: HashSet<Int>

        var index = 0
        lateinit var inKeys: IntArray
        lateinit var outKeys: IntArray

        @Setup(Level.Trial)
        fun setupTrial() {
            kds = IntSet()
            fastutil = IntOpenHashSet(size)
            fastcollect = IntHashSet(size)
            jvm = HashSet(size)

            inKeys = IntArray(size)
            outKeys = IntArray(size)

            repeat(size) { i ->
                val key = rnd.nextInt()
                kds.add(key)
                fastutil.add(key)
                fastcollect.add(key)
                jvm.add(key)
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

        @Setup(Level.Iteration)
        fun setupIteration() {
            index = 0
        }

        inline fun nextIndex(): Int {
            val i  = index
            if (index == inKeys.size - 1) {
                index = 0
            } else {
                index++
            }
            return i
        }

        inline fun nextInKey() = inKeys[nextIndex()]
        inline fun nextOutKey() = outKeys[nextIndex()]
    }

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class PutHitState {
        private val rnd = Random(123)

        @Param("16", "100", "10000", "1000000")
        var size: Int = 0

        lateinit var kds: IntSet
        lateinit var fastutil: IntOpenHashSet
        lateinit var fastcollect: IntHashSet
        lateinit var jvm: HashSet<Int>

        var index = 0
        lateinit var inKeys: IntArray

        @Setup(Level.Trial)
        fun setupTrial() {
            val inSet = IntHashSet(size)
            inKeys = IntArray(size)

            repeat(size) { i ->
                val key = rnd.nextInt()
                inKeys[i] = key
                inSet.add(key)
            }
        }

        @Setup(Level.Iteration)
        fun setupIteration() {
            kds = IntSet()
            fastutil = IntOpenHashSet(size)
            fastcollect = IntHashSet(size)
            jvm = HashSet(size)

            for (key in inKeys) {
                kds.add(key)
                fastutil.add(key)
                fastcollect.add(key)
                jvm.add(key)
            }

            index = 0
        }

        inline fun nextIndex(): Int {
            val i  = index
            if (index == inKeys.size - 1) {
                index = 0
            } else {
                index++
            }
            return i
        }

        inline fun nextInKey() = inKeys[nextIndex()]
    }

    @Benchmark
    fun fastcollectGetHit(s: ReadState) = s.fastcollect.contains(s.nextInKey())

    @Benchmark
    fun fastcollectGetMiss(s: ReadState) = s.fastcollect.contains(s.nextOutKey())

    @Benchmark
    fun fastcollectPutHit(s: PutHitState) = s.fastcollect.add(s.nextInKey())

    @Benchmark
    fun fastcollectIterate(s: ReadState): Int {
        var c = 0
        for (i in s.fastcollect) {
            c += i
        }
        return c
    }

    @Benchmark
    fun kdsGetHit(s: ReadState) = s.kds.contains(s.nextInKey())

    @Benchmark
    fun kdsGetMiss(s: ReadState) = s.kds.contains(s.nextOutKey())

    @Benchmark
    fun kdsPutHit(s: PutHitState) = s.kds.add(s.nextInKey())

    @Benchmark
    fun kdsIterate(s: ReadState): Int {
        var c = 0
        val it = s.fastutil.iterator()
        while (it.hasNext()) {
            c += it.nextInt()
        }
        return c
    }

    @Benchmark
    fun fastutilGetHit(s: ReadState) = s.fastutil.contains(s.nextInKey())

    @Benchmark
    fun fastutilGetMiss(s: ReadState) = s.fastutil.contains(s.nextOutKey())

    @Benchmark
    fun fastutilPutHit(s: PutHitState) = s.fastutil.add(s.nextInKey())

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
    fun jvmGetHit(s: ReadState) = s.jvm.contains(s.nextInKey())

    @Benchmark
    fun jvmGetMiss(s: ReadState) = s.jvm.contains(s.nextOutKey())

    @Benchmark
    fun jvmPutHit(s: PutHitState) = s.jvm.add(s.nextInKey())

    @Benchmark
    fun jvmIterate(s: ReadState): Int {
        var c = 0
        for (i in s.jvm) {
            c += i
        }
        return c
    }
}
