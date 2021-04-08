package de.universegame.envLoggerServer.envData

import de.universegame.envLoggerServer.prepend
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.abs

interface IEnvData {
    val valueMap: MutableMap<String, MutableList<EnvDataSet>>
    val uuids: MutableList<String>
    var listSize: Int
    val precision: ENVDataPrecision


    fun addEntry(set: EnvDataSet) {
        if (valueMap[set.mac] == null) valueMap[set.mac] = mutableListOf()
        if (!uuids.contains(set.mac)) {
            uuids.add(set.mac)
            listSize++
        }
        valueMap[set.mac]?.prepend(set)
    }
}

@Serializable
data class EnvData(
    override val valueMap: MutableMap<String, MutableList<EnvDataSet>> = mutableMapOf(),
    override val uuids: MutableList<String> = mutableListOf(),
    override val precision: ENVDataPrecision,
    override var listSize: Int = 0
) : IEnvData {

    private val maxSize: Int
        get() {
            return precision.toListSize()
        }

    override fun addEntry(set: EnvDataSet) {
        if (!dataSetValid(set))
            return
        super.addEntry(set)
        for (i in 0..10) {
            if (valueMap[set.mac]?.size ?: 0 > maxSize)
                valueMap[set.mac]?.removeLast()
            if (valueMap[set.mac]?.size ?: 0 <= maxSize)
                break
        }
    }

    @Transient
    var lastHums: MutableMap<String, Double> = mutableMapOf()

    @Transient
    var lastTemps: MutableMap<String, Double> = mutableMapOf()

    @Transient
    var lastPress: MutableMap<String, Double> = mutableMapOf()

    /*@Transient
    var lastCO2: Int = -1

    @Transient
    var lastTVOC: Int = -1*/

    private fun dataSetValid(set: EnvDataSet): Boolean {
        val lastHum = lastHums[set.mac] ?: set.humidity
        val lastTemp = lastTemps[set.mac] ?: set.temperature
        val lastPres = lastPress[set.mac] ?: set.pressure
        val returnVal = (abs(lastHum - set.humidity) < lastHum * 0.2) &&
                (abs(lastTemp - set.temperature) < lastTemp * 0.2) &&
                (abs(lastPres - set.pressure) < lastPres * 0.1)

        lastHums[set.mac]?.getCloserTo(set.humidity, 0.5)
        lastTemps[set.mac]?.getCloserTo(set.temperature, .5)
        lastPress[set.mac]?.getCloserTo(set.pressure, .1)
        return returnVal
    }

    private fun Double.getCloserTo(number: Double, fraction: Double) {
        this.minus((this - number) * fraction)
    }
}

