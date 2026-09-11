/**
 * Methods for dealing with Int2IntMaps.
 */
@file:JvmName("Int2IntMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  int2IntMapOf(entry: Map.Entry<Int, Int>): Int2IntMap = int2IntMapOf(entry.key, entry.value)
public fun  int2IntMapOf(vararg entries: Map.Entry<Int, Int>): Int2IntMap = Int2IntHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableInt2IntMapOf(entry: Map.Entry<Int, Int>): MutableInt2IntMap = Int2IntHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableInt2IntMapOf(vararg entries: Map.Entry<Int, Int>): MutableInt2IntMap = Int2IntHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
