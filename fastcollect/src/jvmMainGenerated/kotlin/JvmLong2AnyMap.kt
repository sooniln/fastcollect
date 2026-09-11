/**
 * Methods for dealing with Long2AnyMaps.
 */
@file:JvmName("Long2AnyMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun <V> long2AnyMapOf(entry: Map.Entry<Long, V>): Long2AnyMap<V> = long2AnyMapOf(entry.key, entry.value)
public fun <V> long2AnyMapOf(vararg entries: Map.Entry<Long, V>): Long2AnyMap<V> = Long2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun <V> mutableLong2AnyMapOf(entry: Map.Entry<Long, V>): MutableLong2AnyMap<V> = Long2AnyHashMap<V>(1).apply { set(entry.key, entry.value) }
public fun <V> mutableLong2AnyMapOf(vararg entries: Map.Entry<Long, V>): MutableLong2AnyMap<V> = Long2AnyHashMap<V>(entries.size).apply { entries.forEach { set(it.key, it.value) } }
