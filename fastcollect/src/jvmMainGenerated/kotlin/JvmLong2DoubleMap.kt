/**
 * Methods for dealing with Long2DoubleMaps.
 */
@file:JvmName("Long2DoubleMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  long2DoubleMapOf(entry: Map.Entry<Long, Double>): Long2DoubleMap = long2DoubleMapOf(entry.key, entry.value)
public fun  long2DoubleMapOf(vararg entries: Map.Entry<Long, Double>): Long2DoubleMap = Long2DoubleHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableLong2DoubleMapOf(entry: Map.Entry<Long, Double>): MutableLong2DoubleMap = Long2DoubleHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableLong2DoubleMapOf(vararg entries: Map.Entry<Long, Double>): MutableLong2DoubleMap = Long2DoubleHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
