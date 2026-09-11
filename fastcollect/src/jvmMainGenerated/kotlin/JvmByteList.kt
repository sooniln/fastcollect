/**
 * Methods for dealing with ByteLists.
 */
@file:JvmName("ByteLists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.Random
import kotlin.jvm.JvmMultifileClass
import kotlin.random.asKotlinRandom

public fun MutableByteList.shuffle(random: Random) { shuffle(random.asKotlinRandom()) }
