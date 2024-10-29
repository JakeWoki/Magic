@file:JvmName("IntUtils")

package com.lin.magic.adblock.util.integer

/**
 * Returns the lower 16 bits of the [Int].
 */
fun Int.lowerHalf(): Int {
    val half = (Int.SIZE_BITS / 2)
    return (this shl half) ushr half
}

/**
 * Returns the upper 16 bits of the [Int].
 */
fun Int.upperHalf(): Int {
    val half = (Int.SIZE_BITS / 2)
    return (this ushr half) shl half
}
