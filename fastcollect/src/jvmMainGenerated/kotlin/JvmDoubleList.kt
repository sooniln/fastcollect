/**
 * Methods for dealing with DoubleLists.
 */
@file:JvmName("DoubleLists")
@file:JvmMultifileClass

package io.github.sooniln.fastcollect

import java.util.Random
import kotlin.jvm.JvmMultifileClass
import kotlin.random.asKotlinRandom

public fun MutableDoubleList.shuffle(random: Random) { shuffle(random.asKotlinRandom()) }
