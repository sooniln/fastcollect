/**
 * Methods for dealing with Int2ByteMaps.
 */
@file:JvmName("Int2ByteMaps")

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyInt2ByteMap(): Int2ByteMap = EmptyInt2ByteMap as Int2ByteMap

public fun  int2ByteMapOf(): Int2ByteMap = EmptyInt2ByteMap as Int2ByteMap
public fun  int2ByteMapOf(entry: Pair<Int, Byte>): Int2ByteMap = SingletonInt2ByteMap(entry.first, entry.second)
public fun  int2ByteMapOf(vararg entries: Pair<Int, Byte>): Int2ByteMap = Int2ByteHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableInt2ByteMapOf(): MutableInt2ByteMap = Int2ByteHashMap()
public fun  mutableInt2ByteMapOf(entry: Pair<Int, Byte>): MutableInt2ByteMap = Int2ByteHashMap(1).apply { set(entry.first, entry.second) }
public fun  mutableInt2ByteMapOf(vararg entries: Pair<Int, Byte>): MutableInt2ByteMap = Int2ByteHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
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
public interface Int2ByteMap : Int2ByteTraversable {

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
        foreachKey { k ->
            if (k equalsRaw key) return true
        }
        return false
    }

    public fun containsValue(value: @UnsafeVariance Byte): Boolean {
        foreach { _, v ->
            if (v equalsRaw value) return true
        }
        return false
    }

    public val keys: IntSet
    public val values: ByteCollection

    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun keys(): IntSet = keys
    @Deprecated("For idiomatic Java usage only", level = DeprecationLevel.HIDDEN)
    public fun values(): ByteCollection = values

    public operator fun iterator(): Iterator<Entry>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry {
        public val key: Int
        public val value: Byte

        public operator fun component1(): Int = key
        public operator fun component2(): Byte = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry : Entry {
        final override fun equals(other: Any?): Boolean = other is Entry && other.key equalsRaw key && other.value equalsRaw value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun  Int2ByteMap.asMap(): Map<Int, Byte> = Int2ByteMapWrapper(this)

public fun  Int2ByteMap.Entry.asEntry(): Map.Entry<Int, Byte> = Int2ByteMapEntryWrapper(this)

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
public interface MutableInt2ByteMap : Int2ByteMap, MutableInt2ByteTraversable {

    public fun put(key: Int, value: Byte): Byte

    public fun putIfAbsent(key: Int, value: Byte): Byte {
        val oldValue = get(key)
        if (isDefaultValue(oldValue) && !containsKey(key)) {
            return put(key, value)
        }
        return oldValue
    }

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

    public fun putAll(from: Int2ByteMap) {
        from.foreach { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Int, Byte>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry : Int2ByteMap.Entry {
        override var value: Byte
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry : Int2ByteMap.AbstractEntry(), MutableEntry
}

public fun  MutableInt2ByteMap.asMap(): MutableMap<Int, Byte> = MutableInt2ByteMapWrapper(this)

public fun  MutableInt2ByteMap.MutableEntry.asEntry(): MutableMap.MutableEntry<Int, Byte> = MutableInt2ByteMapEntryWrapper(this)

@OptIn(ExperimentalContracts::class)
public inline fun  MutableInt2ByteMap.merge(key: Int, value: Byte, merge: (oldValue: Byte, value: Byte) -> Byte): Byte {
    contract { callsInPlace(merge, InvocationKind.AT_MOST_ONCE) }

    val oldValue = get(key)
    val absent = isDefaultValue(oldValue) && !containsKey(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    val newValue = if (absent) value else merge(oldValue as Byte, value)
    if (absent || !(newValue equalsRaw oldValue)) {
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

            foreach { key, value ->
                if (!(other.getOrElse(key) { return false } equalsRaw value)) return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        var result = 0
        foreach { key, value ->
            result += key.hashCode() xor value.hashCode()
        }
        return result
    }

    override fun toString(): String = Iterable { iterator() }.joinToString(", ", "{", "}")

    public class SimpleEntry(override val key: Int, override val value: Byte) : Int2ByteMap.AbstractEntry()
}

public abstract class AbstractMutableInt2ByteMap : AbstractInt2ByteMap(), MutableInt2ByteMap


private object EmptyInt2ByteMap : AbstractInt2ByteMap() {


    override fun isDefaultValue(value: Byte): Boolean = true


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Int): Boolean = false

    override fun containsValue(value: Byte): Boolean = false
    override fun get(key: Int): Byte = Byte.MIN_VALUE


    override val keys: IntSet get() = emptyIntSet()

    override val values: ByteCollection get() = emptyByteList()
    override fun iterator(): Iterator<Int2ByteMap.Entry> = emptyList<Int2ByteMap.Entry>().iterator()



    override fun traverser(): Int2ByteTraverser = emptyInt2ByteTraverser()

}

private class SingletonInt2ByteMap(
    private val key: Int,
    private val value: Byte
) : AbstractInt2ByteMap() {
    override fun isDefaultValue(value: Byte): Boolean = value equalsRaw Byte.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Int): Boolean = key equalsRaw this.key
    override fun containsValue(value: Byte): Boolean = value equalsRaw this.value
    override fun get(key: Int): Byte = if (key equalsRaw this.key) value else Byte.MIN_VALUE

    override val keys: IntSet get() = intSetOf(key)

    override val values: ByteCollection get() = byteListOf(value)

    override fun iterator() = object : Iterator<Int2ByteMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Int2ByteMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return SimpleEntry(key, value)
        }
    }

    override fun traverser(): Int2ByteTraverser = object : Int2ByteTraverser {
        private var complete = false
        override val key: Int get() {
            check(complete)
            return this@SingletonInt2ByteMap.key
        }
        override val value: Byte get() {
            check(complete)
            return this@SingletonInt2ByteMap.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class Int2ByteMapWrapper(private val map: Int2ByteMap) : AbstractMap<Int, Byte>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Byte? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Int): Boolean = map.containsKey(key)

    override fun containsValue(value: Byte): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Int, Byte>> = object : AbstractSet<Map.Entry<Int, Byte>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Int, Byte>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun iterator(): Iterator<Map.Entry<Int, Byte>> = object : Iterator<Map.Entry<Int, Byte>> {
            private val it = map.iterator()
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Int, Byte> = it.next().asEntry()
        }
    }
}

private class Int2ByteMapEntryWrapper(
    private val entry: Int2ByteMap.Entry
) : Map.Entry<Int, Byte> {
    override val key: Int get() = entry.key
    override val value: Byte get() = entry.value
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}

private class MutableInt2ByteMapWrapper(private val map: MutableInt2ByteMap) : AbstractMutableMap<Int, Byte>() {
    override val size: Int get() = map.size

    override fun get(key: Int): Byte? {
        val value = map[key]
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
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun add(element: MutableMap.MutableEntry<Int, Byte>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Int, Byte>> = object : MutableIterator<MutableMap.MutableEntry<Int, Byte>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Int, Byte> = it.next().asEntry()
            override fun remove() = it.remove()
        }
    }

    override fun putAll(from: Map<out Int, Byte>): Unit = map.putAll(from)
}

private class MutableInt2ByteMapEntryWrapper(
    private val entry: MutableInt2ByteMap.MutableEntry
) : MutableMap.MutableEntry<Int, Byte> {
    override val key: Int get() = entry.key
    override val value: Byte get() = entry.value
    override fun setValue(newValue: Byte): Byte {
        val oldValue = entry.value
        entry.value = newValue
        return oldValue
    }
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}


public fun interface IntByteConsumer {

    public fun accept(key: Int, value: Byte)
}
