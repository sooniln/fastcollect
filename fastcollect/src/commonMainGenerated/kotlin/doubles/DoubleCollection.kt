@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.doubles


import io.github.sooniln.fastcollect.longs.MutableLongCollection
import io.github.sooniln.fastcollect.longs.MutableLongIterator

import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * A collection of Doubles which inherits from [Collection].
 */
public interface DoubleCollection : Collection<Double> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): DoubleIterator

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
    public fun foreach(action: DoubleConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            action.accept(it.next())
        }
    }

    override fun contains(element: Double): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }

    public fun containsAll(elements: DoubleCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Double>): Boolean {
        if (elements is DoubleCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toDoubleArray(): DoubleArray {
        val array = DoubleArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.any(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.all(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.none(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> DoubleCollection.fold(initial: R, operation: (accumulated: R, Double) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    for (element in this) {
        accumulated = operation(accumulated, element)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.reduce(operation: (accumulated: Double, Double) -> Double): Double {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = iterator()
    var accumulated = it.nextDouble()
    while (it.hasNext()) {
        accumulated = operation(accumulated, it.nextDouble())
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.sumOf(selector: (Double) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0
    while (it.hasNext()) {
        sum += selector(it.nextDouble())
    }
    return sum
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.sumOf(selector: (Double) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0L
    while (it.hasNext()) {
        sum += selector(it.nextDouble())
    }
    return sum
}

@OptIn(ExperimentalContracts::class)
public inline fun DoubleCollection.sumOf(selector: (Double) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0.0
    while (it.hasNext()) {
        sum += selector(it.nextDouble())
    }
    return sum
}

/**
 * A mutable collection of Doubles which inherits from [MutableCollection].
 */
public interface MutableDoubleCollection : DoubleCollection, MutableCollection<Double> {
    override fun iterator(): MutableDoubleIterator

    override fun add(element: Double): Boolean
    override fun remove(element: Double): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextDouble()
            it.remove()
        }
    }

    public fun addAll(elements: DoubleCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Double>): Boolean {
        if (elements is DoubleCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun removeAll(elements: DoubleCollection): Boolean = filterInPlace { elements.contains(it) }
    override fun removeAll(elements: Collection<Double>): Boolean {
        return if (elements is DoubleCollection) removeAll(elements) else filterInPlace { elements.contains(it) }
    }

    public fun retainAll(elements: DoubleCollection): Boolean = filterInPlace { !elements.contains(it) }
    override fun retainAll(elements: Collection<Double>): Boolean {
        return if (elements is DoubleCollection) retainAll(elements) else filterInPlace { !elements.contains(it) }
    }

    public operator fun plusAssign(element: Double) {
        add(element)
    }
    public operator fun plusAssign(elements: DoubleCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Double>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Double) {
        remove(element)
    }
    public operator fun minusAssign(elements: DoubleCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Double>) {
        removeAll(elements)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableDoubleCollection.removeAll(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextDouble())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableDoubleCollection.retainAll(predicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableDoubleCollection.filterInPlace(removePredicate: (Double) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextDouble())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractDoubleCollection : DoubleCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}



@Suppress("OVERRIDE_BY_INLINE")
@JvmInline
public value class InlineDoubleCollection public constructor(@PublishedApi internal val collection: MutableLongCollection) : MutableDoubleCollection {
    override val size: Int
        inline get() = collection.size
    override fun contains(element: Double): Boolean = collection.contains(element.toBits())
    override fun add(element: Double): Boolean = collection.add(element.toBits())
    override fun remove(element: Double): Boolean = collection.remove(element.toBits())
    override fun iterator(): InlineMutableDoubleIterator = InlineMutableDoubleIterator(collection.iterator())
    override fun foreach(action: DoubleConsumer): Unit = collection.foreach { value -> action.accept(Double.fromBits(value)) }
}

public class InlineMutableDoubleIterator public constructor(@PublishedApi internal val it: MutableLongIterator) : MutableDoubleIterator() {
    override fun hasNext(): Boolean = it.hasNext()
    override fun nextDouble(): Double = Double.fromBits(it.nextLong())
    override fun remove() { it.remove() }
}



public fun interface DoubleConsumer {
    public fun accept(value: Double)
}
