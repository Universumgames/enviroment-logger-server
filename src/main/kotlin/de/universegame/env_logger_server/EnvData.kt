package de.universegame.env_logger_server

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.LocalDateTime
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

@Serializable
enum class ENVDataPrecison {
    LATESTDATAONLY,
    LAST6MINUTES,
    LAST6HOURS_1SEC_PRECISION,
    LAST6DAYS_1_MIN_PRECISION,
    LAST6WEEKS_1_HOUR_PRECISION,
    LAST6MONTHS_1_HOUR_PRECISION,
    LAST6YEARS_6_HOUR_PRECISION
}

interface IEnvData {
    val valueMap: MutableMap<String, MutableList<EnvDataSet>>
    val uuids: MutableList<String>
    var listSize: Int
    val precision: ENVDataPrecison


    fun addEntry(set: EnvDataSet) {
        if (valueMap[set.mac] == null) valueMap[set.mac] = mutableListOf()
        if (!uuids.contains(set.mac)) {
            uuids.add(set.mac)
            listSize++
        }
        valueMap[set.mac]?.prepend(set)
    }

    fun addEntry(
        humidity: Double,
        temperature: Double,
        pressure: Double,
        co2: Double,
        tvoc: Double,
        heightApproximation: Double,
        mac: String
    ) {
        addEntry(EnvDataSet(humidity, temperature, pressure, co2, tvoc, heightApproximation, mac, Date().time))
    }
}

@Serializable
data class EnvData(
    override val valueMap: MutableMap<String, MutableList<EnvDataSet>> = mutableMapOf(),
    override val uuids: MutableList<String> = mutableListOf(),
    override val precision: ENVDataPrecison,
    override var listSize: Int = 0,
    val maxSize: Int = 500
) : IEnvData {

    @Deprecated("")
    fun toIoT(): EnvData {
        val dataClone = copy()
        dataClone.valueMap.forEach {
            if (it.value.size > 0) {
                val first = it.value[0].copy()
                it.value.clear()
                it.value.add(first)
            }
        }
        return dataClone
    }

    override fun addEntry(set: EnvDataSet) {
        super.addEntry(set)
        if (valueMap[set.mac]?.size ?: 0 > maxSize)
            valueMap[set.mac]?.removeLast()
    }
}

@Serializable
data class EnvHandler(
    var iotData: EnvData = EnvData(maxSize = 1, precision = ENVDataPrecison.LATESTDATAONLY),
    /**stores data of last ~6 minutes*/
    val secondData: EnvData = EnvData(maxSize = 60 * 3 * 6, precision = ENVDataPrecison.LAST6MINUTES),
    /**stores data of last 6 hours with 1 second precision*/
    val minuteData: EnvData = EnvData(maxSize = 60 * 60 * 6, precision = ENVDataPrecison.LAST6HOURS_1SEC_PRECISION),
    /**stores data of last 6 days with 1 minute precision*/
    val hourData: EnvData = EnvData(maxSize = 60 * 24 * 6, precision = ENVDataPrecison.LAST6DAYS_1_MIN_PRECISION),
    /**stores data of last 6 weeks with 1 hour precision*/
    val dayData: EnvData = EnvData(maxSize = 24 * 7 * 6, precision = ENVDataPrecison.LAST6WEEKS_1_HOUR_PRECISION),
    /**stores data of last 6 months with 1 hour precision*/
    val monthData: EnvData = EnvData(maxSize = 24 * 30 * 6, precision = ENVDataPrecison.LAST6MONTHS_1_HOUR_PRECISION),
    /**stores data of last 6 years with 6 hour precision*/
    val yearData: EnvData = EnvData(maxSize = 4 * 365 * 6, precision = ENVDataPrecison.LAST6YEARS_6_HOUR_PRECISION)
) {

    @Transient
    private var lastUpdatedSecond = 0

    @Transient
    private var lastUpdatedMinute = 0

    @Transient
    private var lastUpdatedHour = 0

    fun addEntry(set: EnvDataSet) {
        iotData.addEntry(set)
        secondData.addEntry(set)
        val time = LocalDateTime.now()
        if (time.second != lastUpdatedSecond) {
            lastUpdatedSecond = time.second
            minuteData.addEntry(set)
        }
        if (time.minute != lastUpdatedMinute){
            lastUpdatedMinute = time.minute
            hourData.addEntry(set)
        }
        if(time.hour != lastUpdatedHour){
            dayData.addEntry(set)
            monthData.addEntry(set)
            if(time.hour % 6 == 0)
                yearData.addEntry(set)
            lastUpdatedHour = time.hour
        }
    }

    fun addEntry(
        humidity: Double,
        temperature: Double,
        pressure: Double,
        co2: Double,
        tvoc: Double,
        heightApproximation: Double,
        mac: String
    ) {
        addEntry(EnvDataSet(humidity, temperature, pressure, co2, tvoc, heightApproximation, mac, Date().time))
    }
}