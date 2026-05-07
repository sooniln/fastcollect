package io.github.sooniln.fastcollect

import androidx.collection.MutableIntIntMap
import androidx.collection.MutableIntList
import androidx.collection.MutableIntSet
import io.github.sooniln.fastcollect.ints.Int2IntHashMap
import io.github.sooniln.fastcollect.ints.IntArrayDeque
import io.github.sooniln.fastcollect.ints.IntHashSet
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import org.eclipse.collections.impl.map.mutable.primitive.IntIntHashMap
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
    emptySplitRow("listEclipse", totalElements) { org.eclipse.collections.impl.list.mutable.primitive.IntArrayList() }
    emptySplitRow("listAndroidX", totalElements) { MutableIntList() }
    emptySplitRow("listKotlin", totalElements) { ArrayList<Int>() }

    emptySplitRow("setFastCollect", totalElements) { IntHashSet() }
    emptySplitRow("setFastutil", totalElements) { IntOpenHashSet() }
    emptySplitRow("setEclipse", totalElements) { org.eclipse.collections.impl.set.mutable.primitive.IntHashSet() }
    emptySplitRow("setAndroidX", totalElements) { MutableIntSet() }
    emptySplitRow("setKotlin", totalElements) { HashSet<Int>() }

    emptySplitRow("mapFastCollect", totalElements) { Int2IntHashMap() }
    emptySplitRow("mapFastutil", totalElements) { Int2IntOpenHashMap() }
    emptySplitRow("mapEclipse", totalElements) { IntIntHashMap() }
    emptySplitRow("mapAndroidX", totalElements) { MutableIntIntMap() }
    emptySplitRow("mapKotlin", totalElements) { HashMap<Int, Int>() }

    for (numCollections in growingSplitCounts(totalElements)) {
        val size = totalElements / numCollections

        splitRow("listFastCollect", numCollections, IntArrayDeque(size).apply { repeat(size) { add(it + 1) } })
        splitRow("listFastutil", numCollections, IntArrayList(size).apply { repeat(size) { add(it + 1) } })
        splitRow("listEclipse", numCollections, org.eclipse.collections.impl.list.mutable.primitive.IntArrayList(size).apply { repeat(size) { add(it + 1) } })
        splitRow("listAndroidX", numCollections, MutableIntList(size).apply { repeat(size) { add(it + 1) } })
        splitRow("listKotlin", numCollections, ArrayList<Int>(size).apply { repeat(size) { add(it + 1) } })

        splitRow("setFastCollect", numCollections, IntHashSet(size).apply { repeat(size) { add(it + 1) } })
        splitRow("setFastutil", numCollections, IntOpenHashSet(size).apply { repeat(size) { add(it + 1) } })
        splitRow("setEclipse", numCollections, org.eclipse.collections.impl.set.mutable.primitive.IntHashSet(size).apply { repeat(size) { add(it + 1) } })
        splitRow("setAndroidX", numCollections, MutableIntSet().apply { repeat(size) { add(it + 1) } })
        splitRow("setKotlin", numCollections, HashSet<Int>(size).apply { repeat(size) { add(it + 1) } })

        splitRow("mapFastCollect", numCollections, Int2IntHashMap(size).apply { repeat(size) { set(it + 1, it + 1) } })
        splitRow("mapFastutil", numCollections, Int2IntOpenHashMap(size).apply { repeat(size) { set(it + 1, it + 1) } })
        splitRow("mapEclipse", numCollections, IntIntHashMap(size).apply { repeat(size) { put(it + 1, it + 1) } })
        splitRow("mapAndroidX", numCollections, MutableIntIntMap(size).apply { repeat(size) { set(it + 1, it + 1) } })
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
