package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.Int2IntHashMap
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

/**
 * A JVM specific benchmark which measures the performance of various map libraries.
 */
@Fork(1)
@Warmup(iterations = 10, time = 150, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 300, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class Map32Benchmark {

    companion object {
        private val seed = System.currentTimeMillis()
    }

    @State(Scope.Benchmark)
    open class BaseState {
        val map: Int2IntHashMap = Int2IntHashMap()
    }

    @State(Scope.Benchmark)
    open class RandomState : BaseState() {

        @Param("3000", "12000", "48000", "192000", "768000", "3072000", "12288000")
        open var size: Int = 3000

        lateinit var keys: IntArray

        @Setup(Level.Trial)
        fun setup() {
            keys = IntArray(size)
            KeyGenerators.generateRandomKeys(keys, seed = seed)

            var value = 0
            for (key in keys) {
                map[key] = value++
            }
        }
    }

    @State(Scope.Benchmark)
    open class FullState : BaseState() {

        @Param("3000", /*"12000", "48000", "192000",*/ "768000", /*"3072000", "12288000"*/)
        open var size: Int = 3000

        @Param("random", "sequential", /*"even", "partition", "highBits"*/)
        var order: String = "random"

        var idx = 0
        lateinit var inKeys: IntArray
        lateinit var outKeys: IntArray

        @Setup(Level.Trial)
        open fun setup() {
            inKeys = IntArray(size)
            outKeys = IntArray(size)
            KeyGenerators.generateKeys(order, inKeys, outKeys, seed = seed)

            inKeys.forEachIndexed { i, key -> map[key] = i }
        }

        inline fun <T> nextInKey(crossinline action: FullState.(Int) -> T): T {
            val t = action(inKeys[idx])
            if (++idx == inKeys.size) {
                idx = 0
            }
            return t
        }

        inline fun <T> nextOutKey(crossinline action: FullState.(Int) -> T): T {
            val t = action(outKeys[idx])
            if (++idx == outKeys.size) {
                idx = 0
            }
            return t
        }

        inline fun <T> nextInOutKeys(action: FullState.(Int, Int) -> T): T {
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

        inline fun <T> nextMissInKey(crossinline action: FullState.(Int) -> T): T {
            val t = action(inKeys[idx])
            if (++idx == inKeys.size) {
                idx = 0
                map.clear()
            }
            return t
        }
    }

    /*@Benchmark
    fun naiveCopy(state: RandomState): Int2IntHashMap {
        val copy = Int2IntHashMap()
        for (entry in state.map) {
            copy[entry.key] = entry.value
        }
        return copy
    }

    @Benchmark
    fun preAllocatedCopy(state: RandomState) = Int2IntHashMap(state.map)*/

    @Benchmark
    fun getHit(state: FullState) = state.nextInKey { key -> map[key] }

    @Benchmark
    fun getMiss(state: FullState) = state.nextOutKey { key -> map[key] }

    @Benchmark
    fun putHit(state: FullState) = state.nextInKey { key -> map[key] = key }

    @Benchmark
    fun putMiss(state: EmptyState) = state.nextMissInKey { key -> map[key] = key }
/*
    @Benchmark
    fun removeAndPutMiss(state: FullState) = state.nextInOutKeys { inKey, outKey ->
        map.remove(inKey)
        map[outKey] = 1
        swapInOut()
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun iterate(state: FullState, bh: Blackhole) {
        for (entry in state.map) {
            bh.consume(entry.key)
            bh.consume(entry.value)
        }
    }*/
}
