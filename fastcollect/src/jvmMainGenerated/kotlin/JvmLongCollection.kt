/**
 * Methods for dealing with LongCollections.
 */
@file:JvmName("LongCollections")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun LongCollection.any(predicate: LongPredicate): Boolean = any { predicate.test(it) }
public fun LongCollection.all(predicate: LongPredicate): Boolean = all { predicate.test(it) }
public fun LongCollection.none(predicate: LongPredicate): Boolean = none { predicate.test(it) }
public fun LongCollection.find(defaultValue: Long, predicate: LongPredicate): Long = find(defaultValue) { predicate.test(it) }
