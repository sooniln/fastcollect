package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.Int2IntHashMap
import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import korlibs.datastructure.IntIntMap
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
 * A JVM specific benchmark which measures the performance of various map libraries.
 */
@Fork(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class MapBenchmark {

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class ReadState {
        private val rnd = Random(123)

        @Param("16", "100", "10000", "1000000")
        var size: Int = 0

        lateinit var kds: IntIntMap
        lateinit var fastutil: Int2IntOpenHashMap
        lateinit var fastcollect: Int2IntHashMap
        lateinit var jvm: HashMap<Int, Int>

        var index = 0
        lateinit var inKeys: IntArray
        lateinit var outKeys: IntArray

        @Setup(Level.Trial)
        fun setupTrial() {
            kds = IntIntMap(size)
            fastutil = Int2IntOpenHashMap(size)
            fastcollect = Int2IntHashMap(size)
            jvm = HashMap(size)

            inKeys = IntArray(size)
            outKeys = IntArray(size)

            repeat(size) { i ->
                val key = rnd.nextInt()
                val value = rnd.nextInt()
                kds[key] = value
                fastutil[key] = value
                fastcollect[key] = value
                jvm[key] = value
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

        inline fun nextInElement() = inKeys[nextIndex()]
        inline fun nextOutElement() = outKeys[nextIndex()]
    }

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class PutHitState {
        private val rnd = Random(123)

        @Param("16", "100", "10000", "1000000")
        var size: Int = 0

        lateinit var kds: IntIntMap
        lateinit var fastutil: Int2IntOpenHashMap
        lateinit var fastcollect: Int2IntHashMap
        lateinit var jvm: HashMap<Int, Int>

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
            kds = IntIntMap(size)
            fastutil = Int2IntOpenHashMap(size)
            fastcollect = Int2IntHashMap(size)
            jvm = HashMap(size)

            for (key in inKeys) {
                val value = rnd.nextInt()
                kds[key] = value
                fastutil[key] = value
                fastcollect[key] = value
                jvm[key] = value
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
    fun fastcollectGetHit(s: ReadState) = s.fastcollect.lookup(s.nextInElement())

    @Benchmark
    fun fastcollectGetMiss(s: ReadState) = s.fastcollect.lookup(s.nextOutElement())

    @Benchmark
    fun fastcollectPutHit(s: PutHitState) { s.fastcollect[s.nextInKey()] = 1 }

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
    fun kdsGetHit(s: ReadState) = s.kds[s.nextInElement()]

    @Benchmark
    fun kdsGetMiss(s: ReadState) = s.kds[s.nextOutElement()]

    @Benchmark
    fun kdsPutHit(s: PutHitState) { s.kds[s.nextInKey()] = 1 }

    @Benchmark
    fun kdsIterateKeys(s: ReadState): Int {
        var c = 0
        for (e in s.kds.keys) {
            c += e
        }
        return c
    }

    @Benchmark
    fun kdsIterateValues(s: ReadState): Int {
        var c = 0
        for (e in s.kds.values) {
            c += e
        }
        return c
    }

    @Benchmark
    fun kdsIterate(s: ReadState): Int {
        var c = 0
        for (e in s.kds.entries.iterator()) {
            c += e.key + e.value
        }
        return c
    }

    @Benchmark
    fun fastutilGetHit(s: ReadState) = s.fastutil.get(s.nextInElement())

    @Benchmark
    fun fastutilGetMiss(s: ReadState): Int = s.fastutil.get(s.nextOutElement())

    @Benchmark
    fun fastutilPutHit(s: PutHitState) { s.fastutil[s.nextInKey()] = 1}

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
    fun jvmGetHit(s: ReadState) = s.jvm[s.nextInElement()]

    @Benchmark
    fun jvmGetMiss(s: ReadState) = s.jvm[s.nextOutElement()]

    @Benchmark
    fun jvmPutHit(s: PutHitState) { s.jvm[s.nextInKey()] = 1}

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

    @Benchmark
    fun insertAndReinsertTimings() {
        val n = 500000

        val insertStart = System.nanoTime()
        val one = IntHashSet(n)
        for (i in 1..n) {
            one.add(i)
        }
        val insertEnd = System.nanoTime()

        val reinsertStart = System.nanoTime()
        val two = IntHashSet(n/2)
        for (e in one) {
            two.add(e)
        }
        val reinsertEnd = System.nanoTime()

        val insertMs = (insertEnd - insertStart) / 1000000.0
        val reinsertMs = (reinsertEnd - reinsertStart) / 1000000.0
        System.out.printf("insert %,d entries: %.2f ms, reinsert: %.2f ms%n", n, insertMs, reinsertMs)
    }
}
