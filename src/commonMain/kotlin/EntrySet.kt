package io.github.sooniln.fastcollect

public interface EntrySet<out T : Map.Entry<*, *>> : Set<T>, FastIterable<T>

public interface MutableEntrySet<out T : MutableMap.MutableEntry<*, *>> : EntrySet<T>, MutableFastIterable<T>
