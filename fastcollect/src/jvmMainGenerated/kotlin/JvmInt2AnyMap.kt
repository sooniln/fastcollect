/**
 * Methods for dealing with Int2AnyMaps.
 */
@file:JvmName("Int2AnyMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun <V> int2AnyMapOf(entry: Map.Entry<Int, V>): Int2AnyMap<V> = int2AnyMapOf(entry.key, entry.value)
public fun <V> int2AnyMapOf(vararg entries: Map.Entry<Int, V>): Int2AnyMap<V> = Int2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun <V> mutableInt2AnyMapOf(entry: Map.Entry<Int, V>): MutableInt2AnyMap<V> = Int2AnyHashMap<V>(1).apply { set(entry.key, entry.value) }
public fun <V> mutableInt2AnyMapOf(vararg entries: Map.Entry<Int, V>): MutableInt2AnyMap<V> = Int2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.key, it.value) } }
