package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.longs.LongHashSet
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
import org.openjdk.jmh.annotations.Timeout
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

/**
 * A JVM specific benchmark which measures the performance of various set libraries.
 */
@Fork(1)
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class LongSetBenchmark {

    companion object {
        const val seed = 1001L
    }

    @State(Scope.Benchmark)
    open class BaseState {

        @Param("12", "14", "16", "18", "20", "22", "24")
        var pow2: Int = 12

        @Param(".50", ".75")
        var loadFactor: Float = .75f

        var size: Int = 0
        lateinit var set: LongHashSet

        @Setup(Level.Trial)
        open fun setup() {
            size = ((1 shl pow2) * loadFactor).toInt() - 2
            set = LongHashSet()
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

            for (key in keys) {
                set.add(key)
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

            for (key in inKeys) {
                set.add(key)
            }
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
            set.clear()
        }

        inline fun <T> nextMissInKey(crossinline action: FullState.(Long) -> T): T {
            val t = action(inKeys[idx])
            if (++idx == inKeys.size) {
                idx = 0
                set.clear()
            }
            return t
        }
    }

    @Benchmark
    fun naiveCopy(state: RandomState): LongHashSet {
        val copy = LongHashSet()
        state.set.forEach { key ->
            if (Thread.interrupted()) throw InterruptedException()
            copy.add(key)
        }
        return copy
    }

    @Benchmark
    fun preAllocatedCopy(state: RandomState) = LongHashSet(state.set)

    @Benchmark
    fun getHit(state: FullState) = state.nextInKey { key -> set.contains(key) }

    @Benchmark
    fun getMiss(state: FullState) = state.nextOutKey { key -> set.contains(key) }

    @Benchmark
    fun putHit(state: FullState) = state.nextInKey { key -> set.add(key) }

    @Benchmark
    fun putMiss(state: EmptyState) = state.nextMissInKey { key -> set.add(key) }

    @Benchmark
    fun removeAndPutMiss(state: FullState) = state.nextInOutKeys { inKey, outKey ->
        set.remove(inKey)
        set.add(outKey)
        swapInOut()
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun iterate(state: RandomState, bh: Blackhole) {
        for (key in state.set) {
            if (Thread.interrupted()) throw InterruptedException()
            bh.consume(key)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun forEach(state: RandomState, bh: Blackhole) {
        state.set.forEach { key ->
            if (Thread.interrupted()) throw InterruptedException()
            bh.consume(key)
        }
    }
}
