package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.IntArrayDeque
import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.IntArrayList
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.CompilerControl
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
 * A JVM specific benchmark which measures the performance of various list libraries.
 */
@Fork(1)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
open class ListBenchmark {

    companion object {
        private fun generateRandomInElements(rnd: Random, inKeys: IntArray) {
            val inSet = IntHashSet(inKeys.size)
            while (inSet.size < inKeys.size) {
                val key = rnd.nextInt()
                if (!inSet.contains(key)) {
                    inKeys[inSet.size] = key
                    inSet.add(key)
                }
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    @State(Scope.Benchmark)
    open class BaseState {
        protected val rnd = Random(123)

        @Param("16", "100", "10000", "1000000")
        var size: Int = 0

        var index = 0
        lateinit var inElements: IntArray

        @Setup(Level.Trial)
        open fun setupTrial() {
            inElements = IntArray(size)
        }

        @Setup(Level.Iteration)
        open fun setupIteration() {
            index = 0
        }

        inline fun nextIndex(): Int {
            val i = index
            if (index == inElements.size - 1) {
                index = 0
            } else {
                index++
            }
            return i
        }

        inline fun nextInElement() = inElements[nextIndex()]
    }

    @State(Scope.Benchmark)
    open class KdsReadState : BaseState() {
        lateinit var kds: korlibs.datastructure.IntArrayList

        @Setup(Level.Trial)
        override fun setupTrial() {
            super.setupTrial()
            generateRandomInElements(rnd, inElements)
            kds = korlibs.datastructure.IntArrayList().apply { for (key in inElements) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class FastutilReadState : BaseState() {
        lateinit var fastutil: IntArrayList

        @Setup(Level.Trial)
        override fun setupTrial() {
            super.setupTrial()
            generateRandomInElements(rnd, inElements)
            fastutil = IntArrayList(size).apply { for (key in inElements) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class FastCollectReadState : BaseState() {
        lateinit var fastcollect: IntArrayDeque

        @Setup(Level.Trial)
        override fun setupTrial() {
            super.setupTrial()
            generateRandomInElements(rnd, inElements)
            fastcollect = IntArrayDeque(size).apply { for (key in inElements) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class KotlinReadState : BaseState() {
        lateinit var kotlin: ArrayList<Int>

        @Setup(Level.Trial)
        override fun setupTrial() {
            super.setupTrial()
            generateRandomInElements(rnd, inElements)
            kotlin = ArrayList<Int>(size).apply { for (key in inElements) add(key) }
        }
    }

    @State(Scope.Benchmark)
    open class KdsAddState {
        lateinit var kds: korlibs.datastructure.IntArrayList

        @Setup(Level.Iteration)
        fun setupIteration() {
            kds = korlibs.datastructure.IntArrayList()
        }
    }

    @State(Scope.Benchmark)
    open class FastutilAddState {
        lateinit var fastutil: IntArrayList

        @Setup(Level.Iteration)
        fun setupIteration() {
            fastutil = IntArrayList()
            System.gc()
            Thread.sleep(100)
        }
    }

    @State(Scope.Benchmark)
    open class FastCollectAddState {
        lateinit var fastcollect: IntArrayDeque

        @Setup(Level.Iteration)
        fun setupIteration() {
            fastcollect = IntArrayDeque()
            System.gc()
            Thread.sleep(100)
        }
    }

    @State(Scope.Benchmark)
    open class KotlinAddState {
        lateinit var kotlin: ArrayList<Int>

        @Setup(Level.Iteration)
        fun setupIteration() {
            kotlin = ArrayList()
            System.gc()
            Thread.sleep(100)
        }
    }

    @Warmup(iterations = 10, batchSize = 1000000)
    @Measurement(iterations = 10, batchSize = 1000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastcollectAdd(s: FastCollectAddState): IntArrayDeque {
        s.fastcollect.add(1)
        return s.fastcollect
    }

    @Benchmark
    fun fastcollectSearch(s: FastCollectReadState): Int {
        val element = s.nextInElement()
        return s.fastcollect.indexOf(element) + s.fastcollect.lastIndexOf(element)
    }

    @Benchmark
    fun fastcollectIterate(s: FastCollectReadState) {
        for (v in s.fastcollect) {
            sink(v)
        }
    }

    @Warmup(iterations = 10, batchSize = 1000000)
    @Measurement(iterations = 10, batchSize = 1000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kdsAdd(s: KdsAddState): korlibs.datastructure.IntArrayList {
        s.kds.add(1)
        return s.kds
    }

    @Benchmark
    fun kdsSearch(s: KdsReadState): Int {
        val element = s.nextInElement()
        return s.kds.indexOf(element) + s.kds.lastIndexOf(element)
    }

    @Benchmark
    fun kdsIterate(s: KdsReadState) {
        for (v in s.kds) {
            sink(v)
        }
    }

    @Warmup(iterations = 10, batchSize = 1000000)
    @Measurement(iterations = 10, batchSize = 1000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun fastutilAdd(s: FastutilAddState): IntArrayList {
        s.fastutil.add(1)
        return s.fastutil
    }

    @Benchmark
    fun fastutilSearch(s: FastutilReadState): Int {
        val element = s.nextInElement()
        return s.fastutil.indexOf(element) + s.fastutil.lastIndexOf(element)
    }

    @Benchmark
    fun fastutilIterate(s: FastutilReadState) {
        val it = s.fastutil.iterator()
        while (it.hasNext()) {
            sink(it.nextInt())
        }
    }

    @Warmup(iterations = 10, batchSize = 1000000)
    @Measurement(iterations = 10, batchSize = 1000000)
    @BenchmarkMode(Mode.SingleShotTime)
    @Benchmark
    fun kotlinAdd(s: KotlinAddState): ArrayList<Int> {
        s.kotlin.add(1)
        return s.kotlin
    }

    @Benchmark
    fun kotlinSearch(s: KotlinReadState): Int {
        val element = s.nextInElement()
        return s.kotlin.indexOf(element) + s.kotlin.lastIndexOf(element)
    }

    @Benchmark
    fun kotlinIterate(s: KotlinReadState) {
        for (v in s.kotlin) {
            sink(v)
        }
    }

    @CompilerControl(CompilerControl.Mode.DONT_INLINE)
    fun sink(v: Int) {
        // IT IS VERY IMPORTANT TO MATCH THE SIGNATURE TO AVOID AUTOBOXING.
        // The method intentionally does nothing.
    }
}
