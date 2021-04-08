package de.universegame.envLoggerServer.envData

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class EnvDataSet(
    val humidity: Double,
    val temperature: Double,
    val pressure: Double,
    val co2: Double,
    val tvoc: Double,
    val heightApproximation: Double,
    val mac: String,
    val time: Long = Date().time
)