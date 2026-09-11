/**
 * Methods for dealing with IntCollections.
 */
@file:JvmName("IntCollections")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun IntCollection.any(predicate: IntPredicate): Boolean = any { predicate.test(it) }
public fun IntCollection.all(predicate: IntPredicate): Boolean = all { predicate.test(it) }
public fun IntCollection.none(predicate: IntPredicate): Boolean = none { predicate.test(it) }
public fun IntCollection.find(defaultValue: Int, predicate: IntPredicate): Int = find(defaultValue) { predicate.test(it) }
