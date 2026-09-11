/**
 * Methods for dealing with IntLists.
 */
@file:JvmName("IntLists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.Random
import kotlin.jvm.JvmMultifileClass
import kotlin.random.asKotlinRandom

public fun MutableIntList.shuffle(random: Random) { shuffle(random.asKotlinRandom()) }
