package io.github.sooniln.fastcollect

import kotlin.random.Random

object KeyGenerators{

    fun generateKeys(order: String, inKeys: IntArray, outKeys: IntArray = IntArray(0), seed: Long = System.currentTimeMillis()) {
        when(order) {
            "random" -> generateRandomKeys(inKeys, outKeys, seed)
            "sequential" -> generateSequentialKeys(inKeys, outKeys)
            "even" -> generateEvenKeys(inKeys, outKeys)
            "partition" -> generatePartitionKeys(inKeys, outKeys)
            "highBits" -> generateHighBitsKeys(inKeys, outKeys)
            else -> throw IllegalArgumentException("Unexpected order: $order")
        }
    }

    fun generateKeys(order: String, inKeys: LongArray, outKeys: LongArray = LongArray(0), seed: Long = System.currentTimeMillis()) {
        when(order) {
            "random" -> generateRandomKeys(inKeys, outKeys, seed)
            "sequential" -> generateSequentialKeys(inKeys, outKeys)
            "even" -> generateEvenKeys(inKeys, outKeys)
            "partition" -> generatePartitionKeys(inKeys, outKeys)
            "highBits" -> generateHighBitsKeys(inKeys, outKeys)
            else -> throw IllegalArgumentException("Unexpected order: $order")
        }
    }

    fun generateRandomKeys(inKeys: IntArray, outKeys: IntArray = IntArray(0), seed: Long = System.currentTimeMillis()) {
        check(inKeys.size + outKeys.size > 0)

        val rnd = Random(seed)

        val inSet = HashSet<Int>(inKeys.size)
        while (inSet.size < inKeys.size) {
            val key = rnd.nextInt()
            if (!inSet.contains(key)) {
                inKeys[inSet.size] = key
                inSet.add(key)
            }
        }

        var i = 0
        while (i < inKeys.size) {
            val key = rnd.nextInt()
            if (!inSet.contains(key)) {
                outKeys[i++] = key
            }
        }
    }

    fun generateRandomKeys(inKeys: LongArray, outKeys: LongArray = LongArray(0), seed: Long = System.currentTimeMillis()) {
        check(inKeys.size + outKeys.size > 0)

        val rnd = Random(seed)

        val inSet = HashSet<Long>(inKeys.size)
        while (inSet.size < inKeys.size) {
            val key = rnd.nextLong()
            if (!inSet.contains(key)) {
                inKeys[inSet.size] = key
                inSet.add(key)
            }
        }

        var i = 0
        while (i < inKeys.size) {
            val key = rnd.nextLong()
            if (!inSet.contains(key)) {
                outKeys[i++] = key
            }
        }
    }

    private fun generateSequentialKeys(inKeys: IntArray, outKeys: IntArray) {
        check(inKeys.size + outKeys.size > 0)

        repeat(inKeys.size) { inKeys[it] = it + 1 }
        repeat(outKeys.size) { outKeys[it] = inKeys[it] + inKeys.size + 1 }
        inKeys.shuffle()
        outKeys.shuffle()
    }

    private fun generateSequentialKeys(inKeys: LongArray, outKeys: LongArray) {
        check(inKeys.size + outKeys.size > 0)

        repeat(inKeys.size) { inKeys[it] = it.toLong() + 1 }
        repeat(outKeys.size) { outKeys[it] = inKeys[it] + inKeys.size + 1 }
    }

    private fun generateEvenKeys(inKeys: IntArray, outKeys: IntArray) {
        check(inKeys.size + outKeys.size > 0)

        repeat(inKeys.size) { inKeys[it] = 2*it + 2 }
        repeat(outKeys.size) { outKeys[it] = 2 * it + 1 }
    }

    private fun generateEvenKeys(inKeys: LongArray, outKeys: LongArray) {
        check(inKeys.size + outKeys.size > 0)

        repeat(inKeys.size) { inKeys[it] = 2*it.toLong() + 2 }
        repeat(outKeys.size) { outKeys[it] = 2 * it.toLong() + 1 }
    }

    private fun generatePartitionKeys(inKeys: IntArray, outKeys: IntArray) {
        check(inKeys.size % 2 == 0)
        check(inKeys.size + outKeys.size > 0)

        repeat(inKeys.size/2) { inKeys[it] = 2*it }
        repeat(inKeys.size/2) { inKeys[it + inKeys.size/2] = 2*it + 1 }
        repeat(outKeys.size) { outKeys[it] = Int.MAX_VALUE - it }
    }

    private fun generatePartitionKeys(inKeys: LongArray, outKeys: LongArray) {
        check(inKeys.size % 2 == 0)
        check(inKeys.size + outKeys.size > 0)

        repeat(inKeys.size/2) { inKeys[it] = 2*it.toLong() }
        repeat(inKeys.size/2) { inKeys[it + inKeys.size/2] = 2*it.toLong() + 1 }
        repeat(outKeys.size) { outKeys[it] = Long.MAX_VALUE - it }
    }

    private fun generateHighBitsKeys(inKeys: IntArray, outKeys: IntArray) {
        check(inKeys.size == outKeys.size)

        repeat(inKeys.size) { inKeys[it] = Integer.reverse(it + 1) }
        repeat(outKeys.size) { outKeys[it] = Integer.reverse(it + inKeys.size + 2) }
    }

    private fun generateHighBitsKeys(inKeys: LongArray, outKeys: LongArray) {
        check(inKeys.size == outKeys.size)

        repeat(inKeys.size) { inKeys[it] = java.lang.Long.reverse(it.toLong() + 1) }
        repeat(outKeys.size) { outKeys[it] = java.lang.Long.reverse(it.toLong() + inKeys.size + 2) }
    }
}
