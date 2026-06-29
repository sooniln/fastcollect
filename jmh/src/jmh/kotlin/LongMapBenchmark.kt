package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.longs.Long2IntHashMap
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
 * A JVM specific benchmark which measures the performance of various map libraries.
 */
@Fork(1, jvmArgs = ["-Xmx6g"])
@Timeout(time = 10, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class LongMapBenchmark {

    companion object {
        const val seed = 1001L
    }

    @State(Scope.Benchmark)
    open class BaseState {

        @Param("514","766","1026","1534","2050","3070","4098","6142","8194","12286","16386","24574","32770","49150","65538","98302","131074","196606","262146","393214","524290","786430","1048578","1572862","2097154","3145726","4194306","6291454","8388610","12582910","16777218","25165822","33554434","50331646","67108866","100663294")
        var size: Int = 514

        lateinit var map: Long2IntHashMap

        @Setup(Level.Trial)
        open fun setup() {
            map = Long2IntHashMap()
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

            map.ensureCapacity(size)
            keys.forEachIndexed { i, key -> map.put(key, i) }
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

            map.ensureCapacity(size)
            inKeys.forEachIndexed { i, key -> map.put(key, i) }
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
    fun naiveCopy(state: RandomState): Long2IntHashMap {
        val copy = Long2IntHashMap()
        for ((key, value) in state.map) {
            if (Thread.interrupted()) throw InterruptedException()
            copy.put(key, value)
        }
        return copy
    }

    @Benchmark
    fun preAllocatedCopy(state: RandomState) = Long2IntHashMap(state.map)

    @Benchmark
    fun getHit(state: FullState) = state.nextInKey { key -> map[key] }

    @Benchmark
    fun getMiss(state: FullState) = state.nextOutKey { key -> map[key] }

    @Benchmark
    fun putHit(state: FullState) = state.nextInKey { key -> map[key] = key.toInt() }

    @Benchmark
    fun putMiss(state: EmptyState) = state.nextMissInKey { key -> map[key] = key.toInt() }

    @Benchmark
    fun removeAndPutMiss(state: FullState) = state.nextInOutKeys { inKey, outKey ->
        map.remove(inKey)
        map[outKey] = outKey.toInt()
        swapInOut()
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun iterate(state: RandomState, bh: Blackhole) {
        for ((key, value) in state.map) {
            if (Thread.interrupted()) throw InterruptedException()
            bh.consume(key)
            bh.consume(value)
        }
    }

    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    @Benchmark
    fun forEach(state: RandomState, bh: Blackhole) {
        state.map.forEach { key, value ->
            if (Thread.interrupted()) throw InterruptedException()
            bh.consume(key)
            bh.consume(value)
        }
    }
}
