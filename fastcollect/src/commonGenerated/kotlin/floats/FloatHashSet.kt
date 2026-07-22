package io.github.sooniln.fastcollect.floats

import io.github.sooniln.fastcollect.ints.IntHashSet
import kotlin.jvm.JvmInline
import kotlin.math.max

public typealias FloatHashSet = InlineFloatHashSet

@Suppress("OVERRIDE_BY_INLINE")
@JvmInline
public value class InlineFloatHashSet internal constructor(@PublishedApi internal val set: IntHashSet) : MutableFloatSet {

    public constructor(capacity: Int = 0) : this(IntHashSet(capacity))
    public constructor(elements: FloatCollection) : this() { addAll(elements) }
    public constructor(elements: Collection<Float>) : this() { addAll(elements) }

    override val size: Int
        inline get() = set.size

    public fun ensureCapacity(capacity: Int) { set.ensureCapacity(capacity) }
    public fun trimToSize() { set.trimToSize() }

    override fun contains(element: Float): Boolean = set.contains(element.toBits())
    override fun add(element: Float): Boolean = set.add(element.toBits())
    override fun remove(element: Float): Boolean = set.remove(element.toBits())
    override fun clear() { set.clear() }

    override fun addAll(elements: FloatCollection): Boolean {
        if (elements is InlineFloatHashSet) {
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
    override fun addAll(elements: Collection<Float>): Boolean {
        if (elements is FloatCollection) {
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

    override fun iterator(): InlineMutableFloatIterator = InlineMutableFloatIterator(set.iterator())

    override fun fastForEach(action: (Float) -> Unit) {
        set.fastForEach { element -> action(Float.fromBits(element)) }
    }
}
