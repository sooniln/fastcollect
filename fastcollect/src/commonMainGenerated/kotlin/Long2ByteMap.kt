/**
 * Methods for dealing with Long2ByteMaps.
 */
@file:JvmName("Long2ByteMaps")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName
import kotlin.jvm.JvmSynthetic

@Suppress("UNCHECKED_CAST")
public fun  emptyLong2ByteMap(): Long2ByteMap = EmptyLong2ByteMap as Long2ByteMap

@Suppress("UNCHECKED_CAST")
public fun  long2ByteMapOf(): Long2ByteMap = EmptyLong2ByteMap as Long2ByteMap
public fun  long2ByteMapOf(key: Long, value: Byte): Long2ByteMap = SingletonLong2ByteMap(key, value)
@JvmSynthetic
public fun  long2ByteMapOf(entry: Pair<Long, Byte>): Long2ByteMap = SingletonLong2ByteMap(entry.first, entry.second)
@JvmSynthetic
public fun  long2ByteMapOf(vararg entries: Pair<Long, Byte>): Long2ByteMap = Long2ByteHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

public fun  mutableLong2ByteMapOf(): MutableLong2ByteMap = Long2ByteHashMap()
@JvmSynthetic
public fun  mutableLong2ByteMapOf(entry: Pair<Long, Byte>): MutableLong2ByteMap = Long2ByteHashMap(1).apply { set(entry.first, entry.second) }
@JvmSynthetic
public fun  mutableLong2ByteMapOf(vararg entries: Pair<Long, Byte>): MutableLong2ByteMap = Long2ByteHashMap(entries.size).apply { entries.forEach { set(it.first, it.second) } }

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  buildLong2ByteMap(expectedSize: Int = 0, builderAction: MutableLong2ByteMap.() -> Unit): Long2ByteMap {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val map = Long2ByteHashMap(expectedSize)
    map.builderAction()
    return map
}

/**
 * A map of Longs to Bytes.
 *
 * A Long2ByteMap returns some default value (how this default value is chosen is implementation dependent) to indicate
 * that a key is not present in the map. Returned values from APIs may be checked with [isDefaultValue] and if this
 * returns true it indicates that the key was not present (or it was present but associated with that value - it may be
 * necessary to disambiguate). For ease of use, prefer to use APIs such as [getOrElse]/[getOrDefault] to handle these
 * cases more easily.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
public interface Long2ByteMap : Long2ByteTraversable {

    @get:JvmName("size")
    public val size: Int

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

    public operator fun get(key: Long): Byte

    /** Returns the value for the given key or throws [NoSuchElementException] if the key is not present. */
    public fun getValue(key: Long): Byte = getOrElse(key) { throw NoSuchElementException() }

    public fun getOrDefault(key: Long, defaultValue: @UnsafeVariance Byte): Byte = getOrElse(key) { defaultValue }

    public fun containsKey(key: Long): Boolean {
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

    @get:JvmName("keys")
    public val keys: LongSet

    @get:JvmName("values")
    public val values: ByteCollection

    public operator fun iterator(): Iterator<Entry>

    /** Prefer to always implement this interface via [AbstractEntry] for correct behavior. */
    public interface Entry {
        public val key: Long
        public val value: Byte

        public operator fun component1(): Long = key
        public operator fun component2(): Byte = value
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractEntry : Entry {
        final override fun equals(other: Any?): Boolean = other is Entry && other.key equalsRaw key && other.value equalsRaw value
        final override fun hashCode(): Int = key.hashCode() xor value.hashCode()
        final override fun toString(): String = "$key=$value"
    }
}

public fun  Long2ByteMap.isNotEmpty(): Boolean = size != 0

public fun  Long2ByteMap.asMap(): Map<Long, Byte> = Long2ByteMapWrapper(this)

public fun  Long2ByteMap.Entry.asEntry(): Map.Entry<Long, Byte> = Long2ByteMapEntryWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  Long2ByteMap.getOrElse(key: Long, defaultValue: () -> Byte): Byte {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    val value = get(key)
    @Suppress("UNCHECKED_CAST", "USELESS_CAST")
    return if (isDefaultValue(value) && !containsKey(key)) defaultValue() else value as Byte
}

/**
 * A mutable map of Longs to Bytes.
 */
public interface MutableLong2ByteMap : Long2ByteMap, MutableLong2ByteTraversable {

    public fun put(key: Long, value: Byte): Byte

    public fun putIfAbsent(key: Long, value: Byte): Byte {
        val oldValue = get(key)
        if (isDefaultValue(oldValue) && !containsKey(key)) {
            return put(key, value)
        }
        return oldValue
    }

    public operator fun set(key: Long, value: Byte) {
        put(key, value)
    }

    /** Replaces the old value for the given key, or throw [NoSuchElementException] if the key is not present. */
    public fun replace(key: Long, value: Byte): Byte = replaceOrSet(key, value) { throw NoSuchElementException() }

    /** Removes the given key and returns its value, or the default value if the key is not present. */
    public fun remove(key: Long): Byte

    /** Removes the given key or throws [NoSuchElementException] if the key is not present. */
    public fun removeKey(key: Long): Byte = removeOrElse(key) { throw NoSuchElementException() }

    /** Removes the given key if it is associated with the given value, return true if the key was removed. */
    public fun remove(key: Long, value: Byte): Boolean

    public fun clear()

    public fun putAll(from: Long2ByteMap) {
        from.foreach { key, value ->
            set(key, value)
        }
    }

    public fun putAll(from: Map<out Long, Byte>) {
        for ((key, value) in from) {
            set(key, value)
        }
    }

    override fun iterator(): MutableIterator<MutableEntry>

    /** Prefer to always implement this interface via [AbstractMutableEntry] for correct behavior. */
    public interface MutableEntry : Long2ByteMap.Entry {
        override var value: Byte
    }

    /** An implementation of [Entry] with correct equals/hashCode/toString. */
    public abstract class AbstractMutableEntry : Long2ByteMap.AbstractEntry(), MutableEntry
}

public fun  MutableLong2ByteMap.asMap(): MutableMap<Long, Byte> = MutableLong2ByteMapWrapper(this)

public fun  MutableLong2ByteMap.MutableEntry.asEntry(): MutableMap.MutableEntry<Long, Byte> = MutableLong2ByteMapEntryWrapper(this)

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2ByteMap.merge(key: Long, value: Byte, merge: (oldValue: Byte, value: Byte) -> Byte): Byte {
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

@JvmSynthetic
@OptIn(ExperimentalContracts::class)
public inline fun  MutableLong2ByteMap.getOrPut(key: Long, defaultValue: () -> Byte): Byte {
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
public inline fun  MutableLong2ByteMap.replaceOrSet(key: Long, value: Byte, oldValue: () -> Byte): Byte {
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
public inline fun  MutableLong2ByteMap.removeOrElse(key: Long, defaultValue: () -> Byte): Byte {
    contract { callsInPlace(defaultValue, InvocationKind.AT_MOST_ONCE) }

    return if (containsKey(key)) {
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        remove(key) as Byte
    } else {
        defaultValue()
    }
}

public abstract class AbstractLong2ByteMap : Long2ByteMap {

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other is Long2ByteMap) {
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

    override fun toString(): String = joinToString(", ", "{", "}")

    public class SimpleEntry(override val key: Long, override val value: Byte) : Long2ByteMap.AbstractEntry()
}

public abstract class AbstractMutableLong2ByteMap : AbstractLong2ByteMap(), MutableLong2ByteMap


private object EmptyLong2ByteMap : AbstractLong2ByteMap() {


    override val size: Int get() = 0
    override fun isEmpty(): Boolean = true

    override fun containsKey(key: Long): Boolean = false
    override val keys: LongSet get() = emptyLongSet()


    override fun isDefaultValue(value: Byte): Boolean = true
    override fun containsValue(value: Byte): Boolean = false
    override fun get(key: Long): Byte = Byte.MIN_VALUE
    override val values: ByteCollection get() = emptyByteList()
    override fun iterator(): Iterator<Long2ByteMap.Entry> = emptyList<Long2ByteMap.Entry>().iterator()
    override fun traverser(): Long2ByteTraverser = emptyLong2ByteTraverser()

}

private class SingletonLong2ByteMap(
    private val key: Long,
    private val value: Byte
) : AbstractLong2ByteMap() {
    override fun isDefaultValue(value: Byte): Boolean = value equalsRaw Byte.MIN_VALUE

    override val size: Int get() = 1
    override fun isEmpty(): Boolean = false

    override fun containsKey(key: Long): Boolean = key equalsRaw this.key
    override fun containsValue(value: Byte): Boolean = value equalsRaw this.value
    override fun get(key: Long): Byte = if (key equalsRaw this.key) value else Byte.MIN_VALUE

    override val keys: LongSet get() = longSetOf(key)

    override val values: ByteCollection get() = byteListOf(value)

    override fun iterator() = object : Iterator<Long2ByteMap.Entry> {
        private var complete: Boolean = false

        override fun hasNext() = !complete
        override fun next(): Long2ByteMap.Entry {
            if (complete) throw NoSuchElementException()
            complete = true
            return SimpleEntry(key, value)
        }
    }

    override fun traverser(): Long2ByteTraverser = object : Long2ByteTraverser {
        private var complete = false
        override val key: Long get() {
            check(complete)
            return this@SingletonLong2ByteMap.key
        }
        override val value: Byte get() {
            check(complete)
            return this@SingletonLong2ByteMap.value
        }
        override fun forward(): Boolean {
            if (complete) return false
            complete = true
            return true
        }
    }
}

private class Long2ByteMapWrapper(private val map: Long2ByteMap) : AbstractMap<Long, Byte>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Byte? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Byte): Boolean = map.containsValue(value)

    override val entries: Set<Map.Entry<Long, Byte>> = object : AbstractSet<Map.Entry<Long, Byte>>() {
        override val size: Int get() = map.size

        override fun contains(element: Map.Entry<Long, Byte>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun iterator(): Iterator<Map.Entry<Long, Byte>> = object : Iterator<Map.Entry<Long, Byte>> {
            private val it = map.iterator()
            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): Map.Entry<Long, Byte> = it.next().asEntry()
        }
    }
}

private class Long2ByteMapEntryWrapper(
    private val entry: Long2ByteMap.Entry
) : Map.Entry<Long, Byte> {
    override val key: Long get() = entry.key
    override val value: Byte get() = entry.value
    override fun equals(other: Any?): Boolean = other is Map.Entry<*, *> && other.key == key && other.value == value
    override fun hashCode(): Int = key.hashCode() xor value.hashCode()
    override fun toString(): String = "$key=$value"
}

private class MutableLong2ByteMapWrapper(private val map: MutableLong2ByteMap) : AbstractMutableMap<Long, Byte>() {
    override val size: Int get() = map.size

    override fun get(key: Long): Byte? {
        val value = map[key]
        return if (map.isDefaultValue(value) && !map.containsKey(key)) null else value
    }

    override fun containsKey(key: Long): Boolean = map.containsKey(key)

    override fun containsValue(value: Byte): Boolean = map.containsValue(value)

    override fun remove(key: Long): Byte? {
        return if (map.containsKey(key)) map.remove(key) else null
    }

    override fun put(key: Long, value: Byte): Byte? {
        val containsKey = map.containsKey(key)
        val oldValue = map.put(key, value)
        return if (containsKey) oldValue else null
    }

    override fun clear(): Unit = map.clear()

    override val entries: MutableSet<MutableMap.MutableEntry<Long, Byte>> = object : AbstractMutableSet<MutableMap.MutableEntry<Long, Byte>>() {
        override val size: Int get() = map.size

        override fun contains(element: MutableMap.MutableEntry<Long, Byte>): Boolean {
            val value = map[element.key]
            return (!map.isDefaultValue(value) || containsKey(element.key)) && value equalsRaw element.value
        }

        override fun add(element: MutableMap.MutableEntry<Long, Byte>): Boolean = throw UnsupportedOperationException()

        override fun iterator(): MutableIterator<MutableMap.MutableEntry<Long, Byte>> = object : MutableIterator<MutableMap.MutableEntry<Long, Byte>> {
            private val it = map.iterator()

            override fun hasNext(): Boolean = it.hasNext()
            override fun next(): MutableMap.MutableEntry<Long, Byte> = it.next().asEntry()
            override fun remove() = it.remove()
        }
    }

    override fun putAll(from: Map<out Long, Byte>): Unit = map.putAll(from)
}

private class MutableLong2ByteMapEntryWrapper(
    private val entry: MutableLong2ByteMap.MutableEntry
) : MutableMap.MutableEntry<Long, Byte> {
    override val key: Long get() = entry.key
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


public fun interface LongByteConsumer {

    public fun accept(key: Long, value: Byte)
}
