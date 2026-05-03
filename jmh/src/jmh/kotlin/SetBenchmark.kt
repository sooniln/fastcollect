package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntArrayDeque
import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.IntArrayList
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

    companion object {
        private fun generateRandomInOutKeys(rnd: Random, inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            val inSet = IntHashSet(inKeys.size)
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

        private fun generateSequentialInOutKeys(inKeys: IntArray, outKeys: IntArray) {
            check(inKeys.size == outKeys.size)

            repeat(inKeys.size) { inKeys[it] = it + 1 }
            repeat(outKeys.size) { outKeys[it] = inKeys[it] + inKeys.size }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class BaseState {
        protected val rnd = Random(123)

        @Param("16", "100", "10000", "1000000")
        var size: Int = 0

        var index = 0
        lateinit var inKeys: IntArray
        lateinit var outKeys: IntArray

        @Setup(Level.Trial)
        open fun setupTrial() {
            inKeys = IntArray(size)
            outKeys = IntArray(size)
        }

        @Setup(Level.Iteration)
        open fun setupIteration() {
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

        inline fun nextInKey() = inKeys[nextIndex()]
        inline fun nextOutKey() = outKeys[nextIndex()]
    }

    @State(Scope.Benchmark)
    open class KdsReadState : BaseState() {
        lateinit var kds: IntSet

        @Param("true", "false")
        var sequential: Boolean = false

        @Setup(Level.Trial)
        override fun setupTrial() {
            super.setupTrial()
            if (sequential) {
                generateSequentialInOutKeys(inKeys, outKeys)
            } else {
                generateRandomInOutKeys(rnd, inKeys, outKeys)
            }
            kds = IntSet().apply { for (key in inKeys) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class FastutilReadState : BaseState() {
        lateinit var fastutil: IntOpenHashSet

        @Param("true", "false")
        var sequential: Boolean = false

        @Setup(Level.Trial)
        override fun setupTrial() {
            super.setupTrial()
            if (sequential) {
                generateSequentialInOutKeys(inKeys, outKeys)
            } else {
                generateRandomInOutKeys(rnd, inKeys, outKeys)
            }
            fastutil = IntOpenHashSet(size).apply { for (key in inKeys) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class FastCollectReadState : BaseState() {
        lateinit var fastcollect: IntHashSet

        @Param("true", "false")
        var sequential: Boolean = false

        @Setup(Level.Trial)
        override fun setupTrial() {
            super.setupTrial()
            if (sequential) {
                generateSequentialInOutKeys(inKeys, outKeys)
            } else {
                generateRandomInOutKeys(rnd, inKeys, outKeys)
            }
            fastcollect = IntHashSet(size).apply { for (key in inKeys) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class KotlinReadState : BaseState() {
        lateinit var kotlin: HashSet<Int>

        @Param("true", "false")
        var sequential: Boolean = false

        @Setup(Level.Trial)
        override fun setupTrial() {
            super.setupTrial()
            if (sequential) {
                generateSequentialInOutKeys(inKeys, outKeys)
            } else {
                generateRandomInOutKeys(rnd, inKeys, outKeys)
            }
            kotlin = HashSet<Int>(size).apply { for (key in inKeys) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class KdsAddState {
        lateinit var kds: IntSet

        @Setup(Level.Iteration)
        fun setupIteration() {
            kds = IntSet()
        }
    }

    @State(Scope.Benchmark)
    open class FastutilAddState {
        lateinit var fastutil: IntOpenHashSet

        @Setup(Level.Iteration)
        fun setupIteration() {
            fastutil = IntOpenHashSet()
            System.gc()
            Thread.sleep(100)
        }
    }

    @State(Scope.Benchmark)
    open class FastCollectAddState {
        lateinit var fastcollect: IntHashSet

        @Setup(Level.Iteration)
        fun setupIteration() {
            fastcollect = IntHashSet()
            System.gc()
            Thread.sleep(100)
        }
    }

    @State(Scope.Benchmark)
    open class KotlinAddState {
        lateinit var kotlin: HashSet<Int>

        @Setup(Level.Iteration)
        fun setupIteration() {
            kotlin = HashSet()
            System.gc()
            Thread.sleep(100)
        }
    }

    @State(Scope.Benchmark)
    open class KdsPutHitState : BaseState() {

        lateinit var kds: IntSet

        @Setup(Level.Iteration)
        override fun setupIteration() {
            super.setupIteration()
            kds = IntSet().apply { for (key in inKeys) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class FastutilPutHitState : BaseState() {

        lateinit var fastutil: IntOpenHashSet

        @Setup(Level.Iteration)
        override fun setupIteration() {
            super.setupIteration()
            fastutil = IntOpenHashSet(size).apply { for (key in inKeys) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class FastCollectPutHitState : BaseState() {

        lateinit var fastcollect: IntHashSet

        @Setup(Level.Iteration)
        override fun setupIteration() {
            super.setupIteration()
            fastcollect = IntHashSet(size).apply { for (key in inKeys) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class KotlinPutHitState : BaseState() {

        lateinit var kotlin: HashSet<Int>

        @Setup(Level.Iteration)
        override fun setupIteration() {
            super.setupIteration()
            kotlin = HashSet<Int>(size).apply { for (key in inKeys) add(key) }
        }
    }

    @Warmup(iterations = 10, batchSize = 1000000)
    @Measurement(iterations = 10, batchSize = 1000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastcollectAdd(s: FastCollectAddState): IntHashSet {
        s.fastcollect.add(1)
        return s.fastcollect
    }

    @Benchmark
    fun fastcollectGetHit(s: FastCollectReadState) = s.fastcollect.contains(s.nextInKey())

    @Benchmark
    fun fastcollectGetMiss(s: FastCollectReadState) = s.fastcollect.contains(s.nextOutKey())

    @Benchmark
    fun fastcollectPutHit(s: FastCollectPutHitState) = s.fastcollect.add(s.nextInKey())

    @Benchmark
    fun fastcollectIterate(s: FastCollectReadState): Int {
        var c = 0
        for (i in s.fastcollect) {
            c += i
        }
        return c
    }

    @Warmup(iterations = 10, batchSize = 1000000)
    @Measurement(iterations = 10, batchSize = 1000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kdsAdd(s: KdsAddState): IntSet {
        s.kds.add(1)
        return s.kds
    }

    @Benchmark
    fun kdsGetHit(s: KdsReadState) = s.kds.contains(s.nextInKey())

    @Benchmark
    fun kdsGetMiss(s: KdsReadState) = s.kds.contains(s.nextOutKey())

    @Benchmark
    fun kdsPutHit(s: KdsPutHitState) = s.kds.add(s.nextInKey())

    @Benchmark
    fun kdsIterate(s: KdsReadState): Int {
        var c = 0
        val it = s.kds.iterator()
        while (it.hasNext()) {
            c += it.next()
        }
        return c
    }

    @Warmup(iterations = 10, batchSize = 1000000)
    @Measurement(iterations = 10, batchSize = 1000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastutilAdd(s: FastutilAddState): IntOpenHashSet {
        s.fastutil.add(1)
        return s.fastutil
    }

    @Benchmark
    fun fastutilGetHit(s: FastutilReadState) = s.fastutil.contains(s.nextInKey())

    @Benchmark
    fun fastutilGetMiss(s: FastutilReadState) = s.fastutil.contains(s.nextOutKey())

    @Benchmark
    fun fastutilPutHit(s: FastutilPutHitState) = s.fastutil.add(s.nextInKey())

    @Benchmark
    fun fastutilIterate(s: FastutilReadState): Int {
        var c = 0
        val it = s.fastutil.iterator()
        while (it.hasNext()) {
            c += it.nextInt()
        }
        return c
    }

    @Warmup(iterations = 10, batchSize = 1000000)
    @Measurement(iterations = 10, batchSize = 1000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kotlinAdd(s: KotlinAddState): HashSet<Int> {
        s.kotlin.add(1)
        return s.kotlin
    }

    @Benchmark
    fun kotlinGetHit(s: KotlinReadState) = s.kotlin.contains(s.nextInKey())

    @Benchmark
    fun kotlinGetMiss(s: KotlinReadState) = s.kotlin.contains(s.nextOutKey())

    @Benchmark
    fun kotlinPutHit(s: KotlinPutHitState) = s.kotlin.add(s.nextInKey())

    @Benchmark
    fun kotlinIterate(s: KotlinReadState): Int {
        var c = 0
        for (i in s.kotlin) {
            c += i
        }
        return c
    }
}
