/**
 * Methods for dealing with Int2FloatMaps.
 */
@file:JvmName("Int2FloatMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  int2FloatMapOf(entry: Map.Entry<Int, Float>): Int2FloatMap = int2FloatMapOf(entry.key, entry.value)
public fun  int2FloatMapOf(vararg entries: Map.Entry<Int, Float>): Int2FloatMap = Int2FloatHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableInt2FloatMapOf(entry: Map.Entry<Int, Float>): MutableInt2FloatMap = Int2FloatHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableInt2FloatMapOf(vararg entries: Map.Entry<Int, Float>): MutableInt2FloatMap = Int2FloatHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
