package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.longs.Long2LongHashMap
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
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * A JVM specific benchmark which measures the performance of various map libraries.
 */
@Fork(1)
@Warmup(iterations = 5, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class LongMapBenchmark {

    companion object {
        val seed = System.currentTimeMillis()
    }

    @State(Scope.Benchmark)
    open class BaseState {

        @Param("3000", "12000", "48000", "192000", "768000", "3072000", "12288000")
        var size: Int = 3000

        lateinit var map: Long2LongHashMap

        @Setup(Level.Trial)
        open fun setup() {
            map = Long2LongHashMap()
        }
    }

    @State(Scope.Benchmark)
    open class RandomState : BaseState() {

        lateinit var keys: LongArray

        @Setup(Level.Trial)
        override fun setup() {
            super.setup()

            keys = LongArray(size)
            KeyGenerators.generateRandomKeys(keys, seed = seed)

            var value = 0L
            for (key in keys) {
                map.put(key, value++)
            }
        }
    }

    @State(Scope.Benchmark)
    open class FullState : BaseState() {

        @Param("random", "lowBits", "even", "partition", "highBits")
        var order: String = "random"

        var idx = 0
        lateinit var inKeys: LongArray
        lateinit var outKeys: LongArray

        @Setup(Level.Trial)
        override fun setup() {
            super.setup()

            inKeys = LongArray(size)
            outKeys = LongArray(size)
            KeyGenerators.generateKeys(order, inKeys, outKeys, seed = seed)

            inKeys.forEachIndexed { i, key -> map.put(key, i.toLong()) }
        }

        inline fun <T> nextInKey(crossinline action: FullState.(Long) -> T): T {
            val t = action(inKeys[idx])
            if (++idx == inKeys.size) {
                idx = 0
            }
            return t
        }

        inline fun <T> nextOutKey(crossinline action: FullState.(Long) -> T): T {
            val t = action(outKeys[idx])
            if (++idx == outKeys.size) {
                idx = 0
            }
            return t
        }

        inline fun <T> nextInOutKeys(action: FullState.(Long, Long) -> T): T {
            val t = action(inKeys[idx], outKeys[idx])
            if (++idx == inKeys.size) {
                idx = 0
            }
            return t
        }

        @Suppress("NOTHING_TO_INLINE")
        inline fun swapInOut() {
            val t = inKeys[idx]
            inKeys[idx] = outKeys[idx]
            outKeys[idx] = t
        }
    }

    @State(Scope.Benchmark)
    open class EmptyState : FullState() {
        @Setup(Level.Trial)
        override fun setup() {
            super.setup()
            map.clear()
        }

        inline fun <T> nextMissInKey(crossinline action: FullState.(Long) -> T): T {
            val t = action(inKeys[idx])
            if (++idx == inKeys.size) {
                idx = 0
                map.clear()
            }
            return t
        }
    }

    @Benchmark
    fun naiveCopy(state: RandomState): Long2LongHashMap {
        val copy = Long2LongHashMap()
        for ((key, value) in state.map) { copy.put(key, value) }
        return copy
    }

    @Benchmark
    fun preAllocatedCopy(state: RandomState) = Long2LongHashMap(state.map)

    @Benchmark
    fun getHit(state: FullState) = state.nextInKey { key -> map.get(key) }

    @Benchmark
    fun getMiss(state: FullState) = state.nextOutKey { key -> map.get(key) }

    @Benchmark
    fun putHit(state: FullState) = state.nextInKey { key -> map.put(key, key) }

    @Benchmark
    fun putMiss(state: EmptyState) = state.nextMissInKey { key -> map.put(key, key) }

    @Benchmark
    fun removeAndPutMiss(state: FullState) = state.nextInOutKeys { inKey, outKey ->
        map.remove(inKey)
        map.put(outKey, 1)
        swapInOut()
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun iterate(state: FullState, bh: Blackhole) {
        for ((key, value) in state.map) {
            bh.consume(key); bh.consume(value)
        }
    }
}
