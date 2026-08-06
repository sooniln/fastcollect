@file:Suppress("UnusedImport")

package io.github.sooniln.fastcollect.ints


import io.github.sooniln.fastcollect.equalsBoxed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * A collection of Ints which inherits from [Collection].
 */
public interface IntCollection : Collection<Int> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): IntIterator

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
    public fun foreach(action: IntConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            action.accept(it.next())
        }
    }

    override fun contains(element: Int): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }

    public fun containsAll(elements: IntCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Int>): Boolean {
        if (elements is IntCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toIntArray(): IntArray {
        val array = IntArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.any(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.all(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.none(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@OptIn(ExperimentalContracts::class)
public inline fun <R> IntCollection.fold(initial: R, operation: (accumulated: R, Int) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    for (element in this) {
        accumulated = operation(accumulated, element)
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.reduce(operation: (accumulated: Int, Int) -> Int): Int {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = iterator()
    var accumulated = it.nextInt()
    while (it.hasNext()) {
        accumulated = operation(accumulated, it.nextInt())
    }
    return accumulated
}

@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.sumOf(selector: (Int) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0
    while (it.hasNext()) {
        sum += selector(it.nextInt())
    }
    return sum
}

@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.sumOf(selector: (Int) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0L
    while (it.hasNext()) {
        sum += selector(it.nextInt())
    }
    return sum
}

@OptIn(ExperimentalContracts::class)
public inline fun IntCollection.sumOf(selector: (Int) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0.0
    while (it.hasNext()) {
        sum += selector(it.nextInt())
    }
    return sum
}

/**
 * A mutable collection of Ints which inherits from [MutableCollection].
 */
public interface MutableIntCollection : IntCollection, MutableCollection<Int> {
    override fun iterator(): MutableIntIterator

    override fun add(element: Int): Boolean
    override fun remove(element: Int): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextInt()
            it.remove()
        }
    }

    public fun addAll(elements: IntCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Int>): Boolean {
        if (elements is IntCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun removeAll(elements: IntCollection): Boolean = filterInPlace { elements.contains(it) }
    override fun removeAll(elements: Collection<Int>): Boolean {
        return if (elements is IntCollection) removeAll(elements) else filterInPlace { elements.contains(it) }
    }

    public fun retainAll(elements: IntCollection): Boolean = filterInPlace { !elements.contains(it) }
    override fun retainAll(elements: Collection<Int>): Boolean {
        return if (elements is IntCollection) retainAll(elements) else filterInPlace { !elements.contains(it) }
    }

    public operator fun plusAssign(element: Int) {
        add(element)
    }
    public operator fun plusAssign(elements: IntCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Int>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Int) {
        remove(element)
    }
    public operator fun minusAssign(elements: IntCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Int>) {
        removeAll(elements)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableIntCollection.removeAll(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextInt())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableIntCollection.retainAll(predicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableIntCollection.filterInPlace(removePredicate: (Int) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextInt())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractIntCollection : IntCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}



public fun interface IntConsumer {
    public fun accept(value: Int)
}
