/**
 * Methods for dealing with FloatCollections.
 */
@file:JvmName("FloatCollections")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun FloatCollection.any(predicate: FloatPredicate): Boolean = any { predicate.test(it) }
public fun FloatCollection.all(predicate: FloatPredicate): Boolean = all { predicate.test(it) }
public fun FloatCollection.none(predicate: FloatPredicate): Boolean = none { predicate.test(it) }
public fun FloatCollection.find(defaultValue: Float, predicate: FloatPredicate): Float = find(defaultValue) { predicate.test(it) }
