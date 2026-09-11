/**
 * Methods for dealing with DoubleCollections.
 */
@file:JvmName("DoubleCollections")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun DoubleCollection.any(predicate: DoublePredicate): Boolean = any { predicate.test(it) }
public fun DoubleCollection.all(predicate: DoublePredicate): Boolean = all { predicate.test(it) }
public fun DoubleCollection.none(predicate: DoublePredicate): Boolean = none { predicate.test(it) }
public fun DoubleCollection.find(defaultValue: Double, predicate: DoublePredicate): Double = find(defaultValue) { predicate.test(it) }
