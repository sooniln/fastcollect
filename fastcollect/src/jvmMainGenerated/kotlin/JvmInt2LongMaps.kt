/**
 * Methods for dealing with Int2LongMaps.
 */
@file:JvmName("Int2LongMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  int2LongMapOf(entry: Map.Entry<Int, Long>): Int2LongMap = int2LongMapOf(entry.key, entry.value)
public fun  int2LongMapOf(vararg entries: Map.Entry<Int, Long>): Int2LongMap = Int2LongHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableInt2LongMapOf(entry: Map.Entry<Int, Long>): MutableInt2LongMap = Int2LongHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableInt2LongMapOf(vararg entries: Map.Entry<Int, Long>): MutableInt2LongMap = Int2LongHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
