/**
 * Methods for dealing with Long2FloatMaps.
 */
@file:JvmName("Long2FloatMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  long2FloatMapOf(entry: Map.Entry<Long, Float>): Long2FloatMap = long2FloatMapOf(entry.key, entry.value)
public fun  long2FloatMapOf(vararg entries: Map.Entry<Long, Float>): Long2FloatMap = Long2FloatHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableLong2FloatMapOf(entry: Map.Entry<Long, Float>): MutableLong2FloatMap = Long2FloatHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableLong2FloatMapOf(vararg entries: Map.Entry<Long, Float>): MutableLong2FloatMap = Long2FloatHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
