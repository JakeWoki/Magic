@file:Suppress("NOTHING_TO_INLINE")

package com.lin.magic

/**
 * Use to implement an unimplemented method.
 */
inline fun unimplemented(): Nothing {
    throw NotImplementedError("Not implemented")
}
