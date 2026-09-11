/**
 * Methods for dealing with ByteCollections.
 */
@file:JvmName("ByteCollections")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import kotlin.jvm.JvmMultifileClass

public fun ByteCollection.any(predicate: BytePredicate): Boolean = any { predicate.test(it) }
public fun ByteCollection.all(predicate: BytePredicate): Boolean = all { predicate.test(it) }
public fun ByteCollection.none(predicate: BytePredicate): Boolean = none { predicate.test(it) }
public fun ByteCollection.find(defaultValue: Byte, predicate: BytePredicate): Byte = find(defaultValue) { predicate.test(it) }
