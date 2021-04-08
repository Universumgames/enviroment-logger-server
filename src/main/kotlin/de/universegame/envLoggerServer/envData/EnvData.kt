package de.universegame.envLoggerServer.envData

import de.universegame.envLoggerServer.prepend
import kotlinx.serialization.Serializable

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
    get() {return precision.toListSize()}

    override fun addEntry(set: EnvDataSet) {
        super.addEntry(set)
        for (i in 0..10) {
            if (valueMap[set.mac]?.size ?: 0 > maxSize)
                valueMap[set.mac]?.removeLast()
            if (valueMap[set.mac]?.size ?: 0 <= maxSize)
                break
        }
    }
}

