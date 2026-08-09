/**
 * Methods for dealing with primitive Collections.
 */
@file:JvmName("Collections")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

/**
 * A collection of Booleans which inherits from [Collection].
 */
public interface BooleanCollection : Collection<Boolean> {
    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun iterator(): BooleanIterator

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
    public fun foreach(action: BooleanConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            action.accept(it.next())
        }
    }

    override fun contains(element: Boolean): Boolean {
        for (e in this) {
            if (e equalsBoxed element) return true
        }
        return false
    }

    public fun containsAll(elements: BooleanCollection): Boolean {
        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    override fun containsAll(elements: Collection<Boolean>): Boolean {
        if (elements is BooleanCollection) {
            return containsAll(elements)
        }

        for (e in elements) {
            if (!contains(e)) {
                return false
            }
        }
        return true
    }

    public fun toBooleanArray(): BooleanArray {
        val array = BooleanArray(size)
        var index = 0
        for (element in this) {
            array[index++] = element
        }
        return array
    }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun BooleanCollection.any(predicate: (Boolean) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    for (element in this) {
        if (predicate(element)) return true
    }
    return false
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun BooleanCollection.all(predicate: (Boolean) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any { !predicate(it) }
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun BooleanCollection.none(predicate: (Boolean) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return !any(predicate)
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun <R> BooleanCollection.fold(initial: R, operation: (accumulated: R, Boolean) -> R): R {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    var accumulated = initial
    for (element in this) {
        accumulated = operation(accumulated, element)
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun BooleanCollection.reduce(operation: (accumulated: Boolean, Boolean) -> Boolean): Boolean {
    contract { callsInPlace(operation, InvocationKind.UNKNOWN) }

    val it = iterator()
    var accumulated = it.nextBoolean()
    while (it.hasNext()) {
        accumulated = operation(accumulated, it.nextBoolean())
    }
    return accumulated
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun BooleanCollection.sumOf(selector: (Boolean) -> Int): Int {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0
    while (it.hasNext()) {
        sum += selector(it.nextBoolean())
    }
    return sum
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun BooleanCollection.sumOf(selector: (Boolean) -> Long): Long {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0L
    while (it.hasNext()) {
        sum += selector(it.nextBoolean())
    }
    return sum
}

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun BooleanCollection.sumOf(selector: (Boolean) -> Double): Double {
    contract { callsInPlace(selector, InvocationKind.UNKNOWN) }

    val it = iterator()
    var sum = 0.0
    while (it.hasNext()) {
        sum += selector(it.nextBoolean())
    }
    return sum
}

/**
 * A mutable collection of Booleans which inherits from [MutableCollection].
 */
public interface MutableBooleanCollection : BooleanCollection, MutableCollection<Boolean> {
    override fun iterator(): MutableBooleanIterator

    override fun add(element: Boolean): Boolean
    override fun remove(element: Boolean): Boolean

    override fun clear() {
        val it = iterator()
        while (it.hasNext()) {
            it.nextBoolean()
            it.remove()
        }
    }

    public fun addAll(elements: BooleanCollection): Boolean {
        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    override fun addAll(elements: Collection<Boolean>): Boolean {
        if (elements is BooleanCollection) {
            return addAll(elements)
        }

        var modified = false
        for (e in elements) {
            modified = add(e) or modified
        }
        return modified
    }

    public fun removeAll(elements: BooleanCollection): Boolean = filterInPlace { elements.contains(it) }
    override fun removeAll(elements: Collection<Boolean>): Boolean {
        return if (elements is BooleanCollection) removeAll(elements) else filterInPlace { elements.contains(it) }
    }

    public fun retainAll(elements: BooleanCollection): Boolean = filterInPlace { !elements.contains(it) }
    override fun retainAll(elements: Collection<Boolean>): Boolean {
        return if (elements is BooleanCollection) retainAll(elements) else filterInPlace { !elements.contains(it) }
    }

    public operator fun plusAssign(element: Boolean) {
        add(element)
    }
    public operator fun plusAssign(elements: BooleanCollection) {
        addAll(elements)
    }
    public operator fun plusAssign(elements: Collection<Boolean>) {
        addAll(elements)
    }

    public operator fun minusAssign(element: Boolean) {
        remove(element)
    }
    public operator fun minusAssign(elements: BooleanCollection) {
        removeAll(elements)
    }
    public operator fun minusAssign(elements: Collection<Boolean>) {
        removeAll(elements)
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableBooleanCollection.removeAll(predicate: (Boolean) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (predicate(it.nextBoolean())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

@OptIn(ExperimentalContracts::class)
public inline fun MutableBooleanCollection.retainAll(predicate: (Boolean) -> Boolean): Boolean {
    contract { callsInPlace(predicate, InvocationKind.UNKNOWN) }
    return removeAll { !predicate(it) }
}

@OptIn(ExperimentalContracts::class)
internal inline fun MutableBooleanCollection.filterInPlace(removePredicate: (Boolean) -> Boolean): Boolean {
    contract { callsInPlace(removePredicate, InvocationKind.UNKNOWN) }

    var modified = false
    val it = iterator()
    while (it.hasNext()) {
        if (removePredicate(it.nextBoolean())) {
            it.remove()
            modified = true
        }
    }
    return modified
}

public abstract class AbstractBooleanCollection : BooleanCollection {
    override fun toString(): String {
        return joinToString(", ", "[", "]")
    }
}



public fun interface BooleanConsumer {
    public fun accept(value: Boolean)
}
