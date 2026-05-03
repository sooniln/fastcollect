package io.github.sooniln.fastcollect

import io.github.sooniln.fastcollect.ints.Int2IntHashMap
import io.github.sooniln.fastcollect.ints.IntArrayDeque
import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import korlibs.datastructure.IntIntMap
import korlibs.datastructure.IntSet
import org.openjdk.jol.info.GraphLayout.parseInstance

/**
 * Measures the total retained memory of distributing a fixed number of integers across N
 * collections, where N grows geometrically from 1 to TOTAL_ELEMENTS. At each step all N
 * collections hold an equal share of the data. Since all N collections are structurally
 * identical, one is measured with JOL and the result is multiplied by N. Output is CSV on stdout.
 *
 * Run via: ./gradlew :benchmark:runSplitMemoryMeasurement > split_memory_results.csv
 */
fun main() {
    println("collection,numCollections,totalBytes")

    val totalElements = 1_000_000

    emptySplitRow("listFastCollect", totalElements) { IntArrayDeque() }
    emptySplitRow("listFastutil", totalElements) { IntArrayList() }
    emptySplitRow("listKDS", totalElements) { korlibs.datastructure.IntArrayList() }
    emptySplitRow("listKotlin", totalElements) { ArrayList<Int>() }

    emptySplitRow("setFastCollect", totalElements) { IntHashSet() }
    emptySplitRow("setFastutil", totalElements) { IntOpenHashSet() }
    emptySplitRow("setKDS", totalElements) { IntSet() }
    emptySplitRow("setKotlin", totalElements) { HashSet<Int>() }

    emptySplitRow("mapFastCollect", totalElements) { Int2IntHashMap() }
    emptySplitRow("mapFastutil", totalElements) { Int2IntOpenHashMap() }
    emptySplitRow("mapKDS", totalElements) { IntIntMap() }
    emptySplitRow("mapKotlin", totalElements) { HashMap<Int, Int>() }

    for (numCollections in growingSplitCounts(totalElements)) {
        val size = totalElements / numCollections

        splitRow("listFastCollect", numCollections, IntArrayDeque(size).apply { repeat(size) { add(it + 1) } })
        splitRow("listFastutil", numCollections, IntArrayList(size).apply { repeat(size) { add(it + 1) } })
        splitRow("listKDS", numCollections, korlibs.datastructure.IntArrayList(size).apply { repeat(size) { add(it + 1) } })
        splitRow("listKotlin", numCollections, ArrayList<Int>(size).apply { repeat(size) { add(it + 1) } })

        splitRow("setFastCollect", numCollections, IntHashSet(size).apply { repeat(size) { add(it + 1) } })
        splitRow("setFastutil", numCollections, IntOpenHashSet(size).apply { repeat(size) { add(it + 1) } })
        splitRow("setKDS", numCollections, IntSet().apply { repeat(size) { add(it + 1) } })
        splitRow("setKotlin", numCollections, HashSet<Int>(size).apply { repeat(size) { add(it + 1) } })

        splitRow("mapFastCollect", numCollections, Int2IntHashMap(size).apply { repeat(size) { set(it + 1, it + 1) } })
        splitRow("mapFastutil", numCollections, Int2IntOpenHashMap(size).apply { repeat(size) {set(it + 1, it + 1) } })
        splitRow("mapKDS", numCollections, IntIntMap(size).apply { repeat(size) { set(it + 1, it + 1) } })
        splitRow("mapKotlin", numCollections, HashMap<Int, Int>(size).apply { repeat(size) { set(it + 1, it + 1) } })
    }
}

private fun emptySplitRow(collection: String, numCollections: Int, constructor: () -> Any) {
    val array = Array(numCollections) { constructor() }
    println("empty$collection,$numCollections,${parseInstance(*array).totalSize()}")
}

private fun splitRow(collection: String, numCollections: Int, obj: Any) {
    println("$collection,$numCollections,${parseInstance(obj).totalSize() * numCollections}")
}

// Returns all divisors of TOTAL_ELEMENTS in ascending order, so every split is exact.
private fun growingSplitCounts(totalElements: Int): List<Int> {
    val divisors = mutableListOf<Int>()
    var i = 1
    while (i * i <= totalElements) {
        if (totalElements % i == 0) {
            divisors += i
            if (i != totalElements / i) divisors += totalElements / i
        }
        i++
    }
    return divisors.sortedDescending()
}
