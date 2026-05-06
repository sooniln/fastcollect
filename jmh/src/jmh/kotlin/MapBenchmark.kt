package io.github.sooniln.fastcollect

import io.github.bluuewhale.hashsmith.SwissMap
import io.github.sooniln.fastcollect.ints.Int2IntHashMap
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import korlibs.datastructure.IntIntMap
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.infra.IterationParams
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * A JVM specific benchmark which measures the performance of various map libraries.
 */
@Fork(1, jvmArgsAppend = ["--add-modules", "jdk.incubator.vector"])
@Warmup(iterations = 5, time = 1000, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 1500, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class MapBenchmark {

    companion object {
        private fun generateRandomInOutKeys(rnd: Random, inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            val inSet = IntOpenHashSet(inKeys.size)
            while (inSet.size < inKeys.size) {
                val key = rnd.nextInt()
                if (!inSet.contains(key)) {
                    inKeys[inSet.size] = key
                    inSet.add(key)
                }
            }

            var i = 0
            while (i < inKeys.size) {
                val r = rnd.nextInt()
                if (!inSet.contains(r)) {
                    outKeys[i++] = r
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class ReadState {
        protected val rnd = Random(101)

        //@Param("seqLow", "seqHigh", "random", "evenLow", "evenHigh", "partitionLow", "partitionHigh", "highBits")
        var order: String = "random"

        @Param("3000", "12000", "48000", "192000", "768000", "3072000")
        open var size: Int = 192000

        var index = 0
        lateinit var inKeys: IntArray
        lateinit var outKeys: IntArray

        lateinit var fastcollect: Int2IntHashMap
        lateinit var fastutil: Int2IntOpenHashMap
        lateinit var eclipse: IntIntHashMap
        lateinit var hashsmith: SwissMap<Int, Int>
        lateinit var kds: IntIntMap
        lateinit var kotlin: HashMap<Int, Int>

        private fun generateInOutKeys(size: Int) {
            inKeys = IntArray(size)
            outKeys = IntArray(size)

            generateRandomInOutKeys(rnd, inKeys, outKeys)
        }

        @Setup(Level.Trial)
        fun setupTrial() {
            generateInOutKeys(size)

            fastcollect = Int2IntHashMap(size).apply { for (key in inKeys) set(key, key) }
            fastutil = Int2IntOpenHashMap(size).apply { for (key in inKeys) set(key, key) }
            eclipse = IntIntHashMap(size).apply { for(key in inKeys) put(key, key) }
            hashsmith = SwissMap<Int, Int>(size).apply { for (key in inKeys) set(key, key) }
            kds = IntIntMap(size).apply { for (key in inKeys) set(key, key) }
            kotlin = HashMap<Int, Int>(size).apply { for (key in inKeys) set(key, key) }
        }

        @Setup(Level.Iteration)
        open fun setupIteration(params: IterationParams) {
            if (params.batchSize > inKeys.size) {
                generateInOutKeys(params.batchSize)
            }

            index = 0
        }

        inline fun nextIndex(): Int {
            val i = index
            if (index == inKeys.size - 1) {
                index = 0
            } else {
                index++
            }
            return i
        }

        inline fun nextInKey() = inKeys[index++]
        inline fun nextOutKey() = outKeys[index++]
        inline fun nextWrappingInKey() = inKeys[nextIndex()]
        inline fun nextWrappingOutKey() = outKeys[nextIndex()]
    }

    @State(Scope.Benchmark)
    open class EmptyState: ReadState() {
        @Setup(Level.Iteration)
        override fun setupIteration(params: IterationParams) {
            super.setupIteration(params)
            fastcollect.clear()
            fastutil.clear()
            eclipse.clear()
            kds.clear()
            kotlin.clear()
        }
    }

    @Benchmark
    fun fastcollectGetHit(s: ReadState) = s.fastcollect.lookup(s.nextWrappingInKey())

    @Benchmark
    fun fastcollectGetMiss(s: ReadState) = s.fastcollect.lookup(s.nextWrappingOutKey())

    @Benchmark
    fun fastcollectPutHit(s: ReadState) {
        val k = s.nextWrappingInKey()
        s.fastcollect[k] = k
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastcollectPutMiss(s: EmptyState) {
        val k = s.nextInKey()
        s.fastcollect[k] = k
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectGrow(s: ReadState) = Int2IntHashMap().apply { repeat(s.size) { set(s.inKeys[it], s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectIterateKeys(s: ReadState, bh: Blackhole) {
        for (e in s.fastcollect.keys) {
            bh.consume(e)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectIterateValues(s: ReadState, bh: Blackhole) {
        for (e in s.fastcollect.values) {
            bh.consume(e)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectIterate(s: ReadState, bh: Blackhole) {
        for (e in s.fastcollect) {
            bh.consume(e.key())
            bh.consume(e.value())
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastcollectIterateFast(s: ReadState, bh: Blackhole) {
        for (e in s.fastcollect.fastIterator()) {
            bh.consume(e.key())
            bh.consume(e.value())
        }
    }

    @Benchmark
    fun fastutilGetHit(s: ReadState) = s.fastutil.get(s.nextWrappingInKey())

    @Benchmark
    fun fastutilGetMiss(s: ReadState) = s.fastutil.get(s.nextWrappingOutKey())

    @Benchmark
    fun fastutilPutHit(s: ReadState) {
        val k = s.nextWrappingInKey()
        s.fastutil[k] = k
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastutilPutMiss(s: EmptyState) {
        val k = s.nextInKey()
        s.fastutil[k] = k
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilGrow(s: ReadState) = Int2IntOpenHashMap().apply { repeat(s.size) { set(s.inKeys[it], s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilIterateKeys(s: ReadState, bh: Blackhole) {
        val it = s.fastutil.keys.iterator()
        while (it.hasNext()) {
            bh.consume(it.nextInt())
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilIterateValues(s: ReadState, bh: Blackhole) {
        val it = s.fastutil.values.iterator()
        while (it.hasNext()) {
            bh.consume(it.nextInt())
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilIterate(s: ReadState, bh: Blackhole) {
        for (e in s.fastutil.int2IntEntrySet()) {
            bh.consume(e.intKey)
            bh.consume(e.intValue)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun fastutilIterateFast(s: ReadState, bh: Blackhole) {
        for (e in s.fastutil.int2IntEntrySet().fastIterator()) {
            bh.consume(e.intKey)
            bh.consume(e.intValue)
        }
    }

    @Benchmark
    fun eclipseGetHit(s: ReadState) = s.eclipse.get(s.nextWrappingInKey())

    @Benchmark
    fun eclipseGetMiss(s: ReadState) = s.eclipse.get(s.nextWrappingOutKey())

    @Benchmark
    fun eclipsePutHit(s: ReadState) {
        val k = s.nextWrappingInKey()
        s.eclipse.put(k, k)
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun eclipsePutMiss(s: EmptyState) {
        val k = s.nextInKey()
        s.eclipse.put(k, k)
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun eclipseGrow(s: ReadState) = IntIntHashMap().apply { repeat(s.size) { put(s.inKeys[it], s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun eclipseIterateKeys(s: ReadState, bh: Blackhole) {
        val it = s.eclipse.keySet().intIterator()
        while (it.hasNext()) {
            bh.consume(it.next())
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun eclipseIterateValues(s: ReadState, bh: Blackhole) {
        val it = s.eclipse.values().intIterator()
        while (it.hasNext()) {
            bh.consume(it.next())
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun eclipseIterate(s: ReadState, bh: Blackhole) {
        val it = s.eclipse.keyValuesView().iterator()
        while (it.hasNext()) {
            val pair = it.next()
            bh.consume(pair.one)
            bh.consume(pair.two)
        }
    }

    @Benchmark
    fun hashsmithGetHit(s: ReadState) = s.hashsmith[s.nextWrappingInKey()]

    @Benchmark
    fun hashsmithGetMiss(s: ReadState) = s.hashsmith[s.nextWrappingOutKey()]

    @Benchmark
    fun hashsmithPutHit(s: ReadState) {
        val k = s.nextWrappingInKey()
        s.hashsmith[k] = k
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun hashsmithPutMiss(s: EmptyState) {
        val k = s.nextInKey()
        s.hashsmith[k] = k
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun hashsmithGrow(s: ReadState) = SwissMap<Int, Int>().apply { repeat(s.size) { set(s.inKeys[it], s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun hashsmithIterateKeys(s: ReadState, bh: Blackhole) {
        for (e in s.hashsmith.keys) {
            bh.consume(e)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun hashsmithIterateValues(s: ReadState, bh: Blackhole) {
        for (e in s.hashsmith.values) {
            bh.consume(e)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun hashsmithIterate(s: ReadState, bh: Blackhole) {
        for (e in s.hashsmith) {
            bh.consume(e.key)
            bh.consume(e.value)
        }
    }

    @Benchmark
    fun kdsGetHit(s: ReadState) = s.kds[s.nextWrappingInKey()]

    @Benchmark
    fun kdsGetMiss(s: ReadState) = s.kds[s.nextWrappingOutKey()]

    @Benchmark
    fun kdsPutHit(s: ReadState) {
        val k = s.nextWrappingInKey()
        s.kds[k] = k
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kdsPutMiss(s: EmptyState) {
        val k = s.nextInKey()
        s.kds[k] = k
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kdsGrow(s: ReadState) = IntIntMap().apply { repeat(s.size) { set(s.inKeys[it], s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kdsIterateKeys(s: ReadState, bh: Blackhole) {
        for (e in s.kds.keys) {
            bh.consume(e)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kdsIterateValues(s: ReadState, bh: Blackhole) {
        for (e in s.kds.values) {
            bh.consume(e)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kdsIterate(s: ReadState, bh: Blackhole) {
        for (e in s.kds.entries) {
            bh.consume(e.key)
            bh.consume(e.value)
        }
    }

    @Benchmark
    fun kotlinGetHit(s: ReadState) = s.kotlin[s.nextWrappingInKey()]

    @Benchmark
    fun kotlinGetMiss(s: ReadState) = s.kotlin[s.nextWrappingOutKey()]

    @Benchmark
    fun kotlinPutHit(s: ReadState) {
        val k = s.nextWrappingInKey()
        s.kotlin[k] = k
    }

    @OperationsPerInvocation(5000000)
    @Warmup(iterations = 10, batchSize = 5000000)
    @Measurement(iterations = 20, batchSize = 5000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kotlinPutMiss(s: EmptyState) {
        val k = s.nextInKey()
        s.kotlin[k] = k
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinGrow(s: ReadState) = HashMap<Int, Int>().apply { repeat(s.size) { set(s.inKeys[it], s.inKeys[it]) } }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinIterateKeys(s: ReadState, bh: Blackhole) {
        for (e in s.kotlin.keys) {
            bh.consume(e)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinIterateValues(s: ReadState, bh: Blackhole) {
        for (e in s.kotlin.values) {
            bh.consume(e)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun kotlinIterate(s: ReadState, bh: Blackhole) {
        for (e in s.kotlin.entries) {
            bh.consume(e.key)
            bh.consume(e.value)
        }
    }
}
