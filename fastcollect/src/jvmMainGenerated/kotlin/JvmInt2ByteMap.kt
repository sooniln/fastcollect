/**
 * Methods for dealing with Int2ByteMaps.
 */
@file:JvmName("Int2ByteMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  int2ByteMapOf(entry: Map.Entry<Int, Byte>): Int2ByteMap = int2ByteMapOf(entry.key, entry.value)
public fun  int2ByteMapOf(vararg entries: Map.Entry<Int, Byte>): Int2ByteMap = Int2ByteHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableInt2ByteMapOf(entry: Map.Entry<Int, Byte>): MutableInt2ByteMap = Int2ByteHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableInt2ByteMapOf(vararg entries: Map.Entry<Int, Byte>): MutableInt2ByteMap = Int2ByteHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
