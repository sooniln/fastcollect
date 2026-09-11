/**
 * Methods for dealing with FloatLists.
 */
@file:JvmName("FloatLists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.Random
import kotlin.jvm.JvmMultifileClass
import kotlin.random.asKotlinRandom

public fun MutableFloatList.shuffle(random: Random) { shuffle(random.asKotlinRandom()) }
