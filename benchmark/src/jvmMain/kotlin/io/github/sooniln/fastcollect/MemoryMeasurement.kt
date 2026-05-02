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
 * Measures retained memory (full object graph via JOL) for each collection type at geometrically
 * increasing sizes. Output is CSV on stdout; redirect to a file and graph bytes vs size.
 *
 * Each collection is created once and grown incrementally between measurement points.
 *
 * Run via: ./gradlew :benchmark:runMemoryMeasurement > memory_results.csv
 */
fun main() {
    val sizes = growingSizes(from = 0, to = 1_000_000)

    val listFastcollect = IntArrayDeque()
    val listFastutil = IntArrayList()
    val listKds = korlibs.datastructure.IntArrayList()
    val listJvm = ArrayList<Int>()

    val setFastcollect = IntHashSet()
    val setFastutil = IntOpenHashSet()
    val setKds = IntSet()
    val setJvm = HashSet<Int>()

    val mapFastcollect = Int2IntHashMap()
    val mapFastutil = Int2IntOpenHashMap()
    val mapKds = IntIntMap()
    val mapJvm = HashMap<Int, Int>()

    println("collection,size,bytes")
    var prev = 0
    for (size in sizes) {
        for (i in prev until size) {
            listFastcollect.add(i)
            listFastutil.add(i)
            listKds.add(i)
            listJvm.add(i)
            setFastcollect.add(i)
            setFastutil.add(i)
            setKds.add(i)
            setJvm.add(i)
            mapFastcollect[i] = i
            mapFastutil[i] = i
            mapKds[i] = i
            mapJvm[i] = i
        }
        prev = size

        row("listFastCollect", size, listFastcollect)
        row("listFastutil", size, listFastutil)
        row("listKds", size, listKds)
        row("listJvm", size, listJvm)
        row("setFastCollect", size, setFastcollect)
        row("setFastutil", size, setFastutil)
        row("setKds", size, setKds)
        row("setJvm", size, setJvm)
        row("mapFastCollect", size, mapFastcollect)
        row("mapFastutil", size, mapFastutil)
        row("mapKds", size, mapKds)
        row("mapJvm", size, mapJvm)
    }
}

private fun row(collection: String, size: Int, obj: Any) = println("$collection,$size,${parseInstance(obj).totalSize()}")

// Generates ~8 sizes per decade on a log scale: 1, 2, 3, 4, 6, 8, 12, 16, ...
private fun growingSizes(from: Int, to: Int): List<Int> {
    val sizes = mutableListOf<Int>()
    var s = from
    while (s <= to) {
        sizes += s
        val next = maxOf(s + 1, (s * 1.25).toInt())
        s = next
    }
    if (sizes.last() != to) sizes += to
    return sizes
}
