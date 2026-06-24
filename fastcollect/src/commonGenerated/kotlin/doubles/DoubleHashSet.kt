package io.github.sooniln.fastcollect.doubles

import io.github.sooniln.fastcollect.longs.LongHashSet
import kotlin.jvm.JvmInline
import kotlin.math.max

public typealias DoubleHashSet = InlineDoubleHashSet

@Suppress("OVERRIDE_BY_INLINE")
@JvmInline
public value class InlineDoubleHashSet internal constructor(@PublishedApi internal val set: LongHashSet) : MutableDoubleSet {

    public constructor(capacity: Int = 0) : this(LongHashSet(capacity))
    public constructor(elements: DoubleCollection) : this() { addAll(elements) }
    public constructor(elements: Collection<Double>) : this() { addAll(elements) }

    override val size: Int
        inline get() = set.size

    public fun ensureCapacity(capacity: Int) { set.ensureCapacity(capacity) }
    public fun trimToSize() { set.trimToSize() }

    override fun contains(element: Double): Boolean = set.contains(element.toBits())
    override fun add(element: Double): Boolean = set.add(element.toBits())
    override fun remove(element: Double): Boolean = set.remove(element.toBits())
    override fun clear() { set.clear() }

    override fun addAll(elements: DoubleCollection): Boolean {
        if (elements is InlineDoubleHashSet) {
            return set.addAll(elements.set)
        } else {
            set.ensureCapacity(max(set.size + (elements.size / 2), elements.size))
            var modified = false
            for (element in elements) {
                modified = set.add(element.toBits()) or modified
            }
            return modified
        }
    }
    override fun addAll(elements: Collection<Double>): Boolean {
        if (elements is DoubleCollection) {
            return addAll(elements)
        } else {
            set.ensureCapacity(max(set.size + (elements.size / 2), elements.size))
            var modified = false
            for (element in elements) {
                modified = set.add(element.toBits()) or modified
            }
            return modified
        }
    }

    override fun iterator(): InlineMutableDoubleIterator = InlineMutableDoubleIterator(set.iterator())
}
