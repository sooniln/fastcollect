/**
 * Methods for dealing with LongLists.
 */
@file:JvmName("LongLists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.Random
import kotlin.jvm.JvmMultifileClass
import kotlin.random.asKotlinRandom

public fun MutableLongList.shuffle(random: Random) { shuffle(random.asKotlinRandom()) }
