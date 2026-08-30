/**
 * Methods for dealing with Long2LongMaps.
 */
@file:JvmName("Long2LongMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  long2LongMapOf(entry: Map.Entry<Long, Long>): Long2LongMap = long2LongMapOf(entry.key, entry.value)
public fun  long2LongMapOf(vararg entries: Map.Entry<Long, Long>): Long2LongMap = Long2LongHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableLong2LongMapOf(entry: Map.Entry<Long, Long>): MutableLong2LongMap = Long2LongHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableLong2LongMapOf(vararg entries: Map.Entry<Long, Long>): MutableLong2LongMap = Long2LongHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
