package de.universegame.envLoggerServer

/**
 * Simple map function to map from one valuespace to another
 * @param value value from space you want to transform from
 * @param fromLow min value from space you want to transform from
 * @param fromHigh max value from space you want to transform from
 * @param toLow min value from space you want to transform to
 * @param toHigh max value from space you want to transform to
 * @return returns mapped value
 * */
fun map(value: Double, fromLow: Double, fromHigh: Double, toLow: Double, toHigh: Double): Double {
    return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow)
}

/**
 * Simple map function to map from one valuespace to another
 * @param value value from space you want to transform from
 * @param fromLow min value from space you want to transform from
 * @param fromHigh max value from space you want to transform from
 * @param toLow min value from space you want to transform to
 * @param toHigh max value from space you want to transform to
 * @return returns mapped value
 * */
fun map(value: Long, fromLow: Long, fromHigh: Long, toLow: Double, toHigh: Double): Double {
    return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow)
}