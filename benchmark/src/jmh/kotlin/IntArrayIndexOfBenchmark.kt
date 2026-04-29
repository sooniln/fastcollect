package io.github.sooniln.fastcollect

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
open class IntArrayIndexOfBenchmark {

    @State(Scope.Benchmark)
    open class BenchmarkState {

        @Param("64", "1024", "65536")
        var size: Int = 0

        lateinit var array: IntArray

        // Present only at index 0 — tests overhead with immediate hit.
        var hitFirstElement: Int = 0

        // Present only at index size-1 — forces a full traversal before hitting.
        var hitLastElement: Int = 0

        // Not present anywhere — forces a full traversal with no hit.
        var missElement: Int = 0

        @Setup(Level.Trial)
        fun setup() {
            // Populate with distinct values starting at 2, leaving 0 and 1 as sentinels.
            array = IntArray(size) { it + 2 }
            array.shuffle(Random(42))

            hitFirstElement = array[0]

            // 0 is not produced by the fill above, so it can only appear where we place it.
            hitLastElement = 0
            array[size - 1] = hitLastElement

            // 1 is also not produced by the fill above.
            missElement = 1
        }
    }

    // --- hit at first position ---

    @Benchmark
    fun fastIndexOfHitFirst(s: BenchmarkState): Int = s.array.fastIndexOf(s.hitFirstElement)

    @Benchmark
    fun indexOfHitFirst(s: BenchmarkState): Int = s.array.indexOf(s.hitFirstElement)

    // --- hit at last position ---

    @Benchmark
    fun fastIndexOfHitLast(s: BenchmarkState): Int = s.array.fastIndexOf(s.hitLastElement)

    @Benchmark
    fun indexOfHitLast(s: BenchmarkState): Int = s.array.indexOf(s.hitLastElement)

    // --- miss: full traversal, no hit ---

    @Benchmark
    fun fastIndexOfMiss(s: BenchmarkState): Int = s.array.fastIndexOf(s.missElement)

    @Benchmark
    fun indexOfMiss(s: BenchmarkState): Int = s.array.indexOf(s.missElement)
}
