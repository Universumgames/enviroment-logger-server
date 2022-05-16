package de.universegame.envLoggerServer.envData

import de.universegame.envLoggerServer.prepend
import kotlinx.serialization.Serializable
import java.util.*

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
        super.addEntry(set)
        val oldestAllowdTime = Date().time - precision.toOldestAllowedTime()
        valueMap[set.mac]?.removeAll {
            it.time < oldestAllowdTime
        }
        for (i in 0..10) {
            if ((valueMap[set.mac]?.size ?: 0) > maxSize)
                valueMap[set.mac]?.removeLast()
            if ((valueMap[set.mac]?.size ?: 0) <= maxSize)
                break
        }
    }

    var maxTemp: Double = Double.MIN_VALUE
        private set

    var minTemp: Double = Double.MAX_VALUE
        private set

    var maxPres: Double = Double.MIN_VALUE
        private set

    var minPres: Double = Double.MAX_VALUE
        private set

    var maxCO2: Int = Int.MIN_VALUE
        private set

    var minCO2: Int = Int.MAX_VALUE
        private set

    var maxTVOC: Int = Int.MIN_VALUE
        private set

    var minTVOC: Int = Int.MAX_VALUE
        private set

    fun calcMinMax(){
        for(keyValue in valueMap){
            for(set in keyValue.value){
                if (set.temperature > maxTemp) maxTemp = set.temperature
                if (set.temperature < minTemp && set.temperature > -500) minTemp = set.temperature

                if (set.pressure > maxPres) maxPres = set.pressure
                if (set.pressure < minPres && set.pressure >= 0) minPres = set.pressure

                if (set.co2 > maxCO2) maxCO2 = set.co2.toInt()
                if (set.co2 < minCO2 && set.co2 >= 400) minCO2 = set.co2.toInt()

                if (set.tvoc > maxTVOC) maxTVOC = set.tvoc.toInt()
                if (set.tvoc < minTVOC && set.tvoc >= 0) minTVOC = set.tvoc.toInt()
            }
        }
    }
}

