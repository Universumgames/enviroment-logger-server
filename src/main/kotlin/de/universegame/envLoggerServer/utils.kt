package de.universegame.envLoggerServer

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Helper function to round a double to *decimal* places
 * (0.02356.round(2) -> 0.02)
 * */
fun Double.round(decimal: Int): Double{
    return (this * 10.0.pow(decimal)).roundToInt().toDouble() / 10.0.pow(decimal)
}

fun <T> MutableList<T>.prepend(element: T) {
    if (size > 0)
        add(0, element)
    else add(element)
}