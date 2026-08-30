/**
 * Methods for dealing with Long2ByteMaps.
 */
@file:JvmName("Long2ByteMaps")
@file:JvmMultifileClass
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package io.github.sooniln.fastcollect

import java.util.Map
import kotlin.jvm.JvmMultifileClass

public fun  long2ByteMapOf(entry: Map.Entry<Long, Byte>): Long2ByteMap = long2ByteMapOf(entry.key, entry.value)
public fun  long2ByteMapOf(vararg entries: Map.Entry<Long, Byte>): Long2ByteMap = Long2ByteHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }

public fun  mutableLong2ByteMapOf(entry: Map.Entry<Long, Byte>): MutableLong2ByteMap = Long2ByteHashMap(1).apply { set(entry.key, entry.value) }
public fun  mutableLong2ByteMapOf(vararg entries: Map.Entry<Long, Byte>): MutableLong2ByteMap = Long2ByteHashMap(entries.size).apply { entries.forEach { set(it.key, it.value) } }
