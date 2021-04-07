package de.universegame.env_logger_server


fun map(value: Double, fromLow: Double, fromHigh: Double, toLow: Double, toHigh: Double): Double {
    return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow)
}

fun map(value: Long, fromLow: Long, fromHigh: Long, toLow: Double, toHigh: Double): Double {
    return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow)
}