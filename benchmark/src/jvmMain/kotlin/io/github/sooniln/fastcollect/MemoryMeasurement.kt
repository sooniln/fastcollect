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
    val listEclipse = org.eclipse.collections.impl.list.mutable.primitive.IntArrayList()
    val listAndroidX = MutableIntList()
    val listKotlin = ArrayList<Int>()

    val setFastcollect = IntHashSet()
    val setFastutil = IntOpenHashSet()
    val setEclipse = org.eclipse.collections.impl.set.mutable.primitive.IntHashSet()
    val setAndroidX = MutableIntSet()
    val setKotlin = HashSet<Int>()

    val mapFastcollect = Int2IntHashMap()
    val mapFastutil = Int2IntOpenHashMap()
    val mapEclipse = IntIntHashMap()
    val mapAndroidX = MutableIntIntMap()
    val mapKotlin = HashMap<Int, Int>()

    println("collection,size,bytes")
    var prev = 0
    for (size in sizes) {
        for (i in prev until size) {
            listFastcollect.add(i)
            listFastutil.add(i)
            listEclipse.add(i)
            listAndroidX.add(i)
            listKotlin.add(i)
            setFastcollect.add(i)
            setFastutil.add(i)
            setEclipse.add(i)
            setAndroidX.add(i)
            setKotlin.add(i)
            mapFastcollect[i] = i
            mapFastutil[i] = i
            mapEclipse.put(i, i)
            mapAndroidX[i] = i
            mapKotlin[i] = i
        }
        prev = size

        row("listFastCollect", size, listFastcollect)
        row("listFastutil", size, listFastutil)
        row("listEclipse", size, listEclipse)
        row("listAndroidX", size, listAndroidX)
        row("listKotlin", size, listKotlin)
        row("setFastCollect", size, setFastcollect)
        row("setFastutil", size, setFastutil)
        row("setEclipse", size, setEclipse)
        row("setAndroidX", size, setAndroidX)
        row("setKotlin", size, setKotlin)
        row("mapFastCollect", size, mapFastcollect)
        row("mapFastutil", size, mapFastutil)
        row("mapEclipse", size, mapEclipse)
        row("mapAndroidX", size, mapAndroidX)
        row("mapKotlin", size, mapKotlin)
    }
}

private fun row(collection: String, size: Int, obj: Any) = println("$collection,$size,${parseInstance(obj).totalSize()}")

private fun growingSizes(from: Int, to: Int): List<Int> {
    val sizes = mutableListOf<Int>()
    var s = from
    while (s <= to) {
        sizes += s
        val next = maxOf(s + 10, (s * 1.1).toInt())
        s = next
    }
    if (sizes.last() != to) sizes += to
    return sizes
}
