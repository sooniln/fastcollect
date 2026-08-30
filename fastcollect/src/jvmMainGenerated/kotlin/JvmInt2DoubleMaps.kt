/**
 * Methods for dealing with Int2DoubleMaps.
 */
@file:JvmName("Int2DoubleMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  int2DoubleMapOf(entry: Map.Entry<Int, Double>): Int2DoubleMap = int2DoubleMapOf(entry.key, entry.value)
public fun  int2DoubleMapOf(vararg entries: Map.Entry<Int, Double>): Int2DoubleMap = Int2DoubleHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableInt2DoubleMapOf(entry: Map.Entry<Int, Double>): MutableInt2DoubleMap = Int2DoubleHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableInt2DoubleMapOf(vararg entries: Map.Entry<Int, Double>): MutableInt2DoubleMap = Int2DoubleHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
