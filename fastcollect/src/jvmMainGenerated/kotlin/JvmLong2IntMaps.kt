/**
 * Methods for dealing with Long2IntMaps.
 */
@file:JvmName("Long2IntMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  long2IntMapOf(entry: Map.Entry<Long, Int>): Long2IntMap = long2IntMapOf(entry.key, entry.value)
public fun  long2IntMapOf(vararg entries: Map.Entry<Long, Int>): Long2IntMap = Long2IntHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableLong2IntMapOf(entry: Map.Entry<Long, Int>): MutableLong2IntMap = Long2IntHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableLong2IntMapOf(vararg entries: Map.Entry<Long, Int>): MutableLong2IntMap = Long2IntHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
