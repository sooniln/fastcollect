/**
 * Methods for dealing with Int2ByteMaps.
 */
@file:JvmName("Int2ByteMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmName

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2ByteMap(): Int2ByteMap = EmptyInt2ByteMap as Int2ByteMap

@Suppress("UNCHECKED_CAST")
public fun  int2ByteMapOf(): Int2ByteMap = EmptyInt2ByteMap as Int2ByteMap
public fun  int2ByteMapOf(entry: Pair<Int, Byte>): Int2ByteMap = SingletonInt2ByteMap(entry.first, entry.second)
public fun  int2ByteMapOf(vararg entries: Pair<Int, Byte>): Int2ByteMap = Int2ByteHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2ByteMapOf(): MutableInt2ByteMap = Int2ByteHashMap()
public fun  mutableInt2ByteMapOf(entry: Pair<Int, Byte>): MutableInt2ByteMap = Int2ByteHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2ByteMapOf(vararg entries: Pair<Int, Byte>): MutableInt2ByteMap = Int2ByteHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun  buildInt2ByteMap(expectedSize: Int = 0, builderAction: MutableInt2ByteMap.() -> Unit): Int2ByteMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Int2ByteHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Ints to Bytes.
 *
 * A Int2ByteMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */

public interface Int2ByteMap {


    public val size: Int

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun size(): Int = size

    public fun isEmpty(): Boolean {
        return size == 0
    }

    /**
     * Returns true if the given value is current the default value of the map (i.e., the value returned from retrieval
     * operations when a key is not present). Note that maps are not required to have an unchanging default value
     * (though this is the most common implementation). A map may change its default value during the invocation of any
     * mutable public API method. A map may not change its default value outside of the invocation of any mutable public
     * API method. For this reason clients should not store or make other assumptions about the default value.
     */
    public fun isDefaultValue(value: @UnsafeVariance Byte): Boolean

    public operator fun get(key: Int): Byte

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Int): Byte = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Int, defaultValue: @UnsafeVariance Byte): Byte = getOrElse(key) { defaultValue }

    public fun containsKey(key: Int): Boolean {
        for (k in keys) {
            if (k equalsBoxed key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Byte): Boolean {
        for (v in values) {
            if (v equalsBoxed value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: ByteCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): ByteCollection = values


    public interface Entry {

        public val key: Int
        public val value: Byte

        public operator fun component1(): Int = key
        public operator fun component2(): Byte = value
    }

    /** Returns a [FastIterator] over the map entries. */
    public operator fun iterator(): FastIterator<Entry>

    /**
     * A method for iteration guaranteed to be as fast or faster than [iterator].
     */
 
    public fun foreach(action: IntByteConsumer) {
 
        val it = iterator()
        while (it.hasNext()) {
            val entry = it.next()
            action.accept(entry.key, entry.value)
        }
    }

    /**
     * A method for iteration over keys guaranteed to be as fast or faster than [iterator].
     */
    public fun foreachKey(action: IntConsumer) {
        val it = iterator()
        while (it.hasNext()) {
            val entry = it.next()
            action.accept(entry.key)
        }
    }
}

public fun  Int2ByteMap.asMap(): Map<Int, Byte> = Int2ByteMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  Int2ByteMap.getOrElse(key: Int, defaultValue: () -> Byte): Byte {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Byte
}

/**
 * A mutable map of Ints to Bytes.
 */
public interface MutableInt2ByteMap : Int2ByteMap {

    public fun put(key: Int, value: Byte): Byte

    public operator fun set(key: Int, value: Byte) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Int, value: Byte): Byte = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Int): Byte

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Int): Byte = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Int, value: Byte): Boolean

    public fun clear()

    override val keys: MutableIntSet
    override val values: MutableByteCollection

    public fun putAll(from: Int2ByteMap) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public fun putAll(from: Map<out Int, Byte>) {
        for (entry in from) {
            set(entry.key, entry.value)
        }
    }

    public interface MutableEntry : Int2ByteMap.Entry {
        override var value: Byte
    }

    override fun iterator(): MutableFastIterator<MutableEntry>
}

public fun  MutableInt2ByteMap.asMutableMap(): MutableMap<Int, Byte> = MutableInt2ByteMapWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ByteMap.merge(key: Int, value: Byte, merge: (oldValue: Byte, value: Byte) -> Byte): Byte {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Byte, value)
    if (absent || !(newValue equalsBoxed oldValue)) {
        set(key, newValue)
    }
    return newValue
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ByteMap.getOrPut(key: Int, defaultValue: () -> Byte): Byte {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    var value = get(key)
    if (isDefaultValue(value) && !containsKey(key)) {
        value = defaultValue()
        set(key, value)
        return value
    } else {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return value as Byte
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ByteMap.replaceOrSet(key: Int, value: Byte, oldValue: () -> Byte): Byte {
    contract { callsInPlace(oldValue, InvocationKind.AT_MOST_ONCE) }

    if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        return put(key, value) as Byte
    } else {
        val returnValue = oldValue()
        set(key, value)
        return returnValue
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ByteMap.removeOrElse(key: Int, defaultValue: () -> Byte): Byte {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Byte
    } else {
        defaultValue()
    }
}

public abstract class AbstractInt2ByteMap : Int2ByteMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Int2ByteMap) {
            if (other.size != size) return false

            for (entry in this) {
                if (!(other[entry.key] equalsBoxed entry.value)) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        for (entry in this) {
            result += entry.key.hashCode() xor entry.value.hashCode()
        }
        return result
    }

    override fun toString(): String = Iterable { iterator() }.joinToString(", ", "{", "}")

    public class SimpleEntry(override val key: Int, override val value: Byte) : Int2ByteMap.Entry
}

public abstract class AbstractMutableInt2ByteMap : AbstractInt2ByteMap(), MutableInt2ByteMap


private object EmptyInt2ByteMap : Int2ByteMap {


    override fun isDefaultValue(value: Byte): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Byte): Boolean = false
    override fun get(key: Int): Byte = Byte.MIN_VALUE


    override val keys: IntSet get() = emptyIntSet()

    override val values: ByteCollection get() = emptyByteList()
    override fun iterator() = emptyFastIterator<Int2ByteMap.Entry>()

}

private class SingletonInt2ByteMap(private val key: Int, private val value: Byte) : Int2ByteMap {
    override fun isDefaultValue(value: Byte): Boolean = value equalsBoxed Byte.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsBoxed this.key
    override fun containsValue(value: Byte): Boolean = value equalsBoxed this.value
    override fun get(key: Int): Byte = if (key equalsBoxed this.key) value else Byte.MIN_VALUE

    override val keys: IntSet by lazy { intSetOf(key) }

    override val values: ByteCollection by lazy { byteListOf(value) }

    override fun iterator() = object : FastIterator<Int2ByteMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2ByteMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return AbstractInt2ByteMap.SimpleEntry(key, value)
        }
    }
}

private class Int2ByteMapWrapper(private val map: Int2ByteMap) : AbstractMap<Int, Byte>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Byte? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Byte): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Byte>> = object : AbstractSet<Map.Entry<Int, Byte>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Byte>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Byte>> = object : Iterator<Map.Entry<Int, Byte>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Byte> {
                val entry = it.next()
                return object : Map.Entry<Int, Byte> {
                    override val key = entry.key
                    override val value = entry.value
                }
            }
        }
    }
}

private class MutableInt2ByteMapWrapper(private val map: MutableInt2ByteMap) : AbstractMutableMap<Int, Byte>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Byte? {
        val value = map.get(key)
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Byte): Boolean = map.containsValue(value)

    override fun remove(key: Int): Byte? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Int, value: Byte): Byte? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Byte>> = object : AbstractMutableSet<MutableMap.MutableEntry<Int, Byte>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Int, Byte>): Boolean {
            val value = map[element.key]
            if (map.isDefaultValue(value) && !containsKey(element.key)) return false
            return value equalsBoxed element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Byte>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Byte>> = object : MutableIterator<MutableMap.MutableEntry<Int, Byte>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Byte> = it.next().let { e -> Entry(e.key, e.value) }
            override fun remove() = it.remove()
        }

        private inner class Entry(override val key: Int, value: Byte) : MutableMap.MutableEntry<Int, Byte> {
            override var value = value
                private set

            override fun setValue(newValue: Byte): Byte {
                val oldValue = value
                if (!(map.put(key, newValue) equalsBoxed oldValue)) throw ConcurrentModificationException()
                value = newValue
                return oldValue
            }

            override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
            override fun hashCode(): Int = key.hashCode() xor value.hashCode()
            override fun toString(): String = "$key=$value"
        }
    }

    override fun putAll(from: Map<out Int, Byte>): Unit = map.putAll(from)
}


public fun interface IntByteConsumer {

    public fun accept(key: Int, value: Byte)
}
