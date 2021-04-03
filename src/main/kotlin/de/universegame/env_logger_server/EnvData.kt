package de.universegame.env_logger_server

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
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

    /*var maxHum: Double = Double.MIN_VALUE
        private set
    var minHum: Double = Double.MAX_VALUE
        private set*/
    var maxTemp: Double = Double.MIN_VALUE
        private set
    var minTemp: Double = Double.MAX_VALUE
        private set
    var maxPres: Double = Double.MIN_VALUE
        private set
    var minPres: Double = Double.MAX_VALUE
        private set
    var maxCO2: Int = 0
        private set
    var minCO2: Int = 0
        private set
    var maxTVOC: Int = 0
        private set
    var minTVOC: Int = 0
        private set
    var minTime: Long = Date().time
        private set

    fun addEntry(set: EnvDataSet) {
        iotData.addEntry(set)
        secondData.addEntry(set)
        val time = LocalDateTime.now()
        if (time.second != lastUpdatedSecond) {
            lastUpdatedSecond = time.second
            minuteData.addEntry(set)
        }
        if (time.minute != lastUpdatedMinute) {
            lastUpdatedMinute = time.minute
            hourData.addEntry(set)
        }
        if (time.hour != lastUpdatedHour) {
            dayData.addEntry(set)
            monthData.addEntry(set)
            if (time.hour % 6 == 0)
                yearData.addEntry(set)
            lastUpdatedHour = time.hour
            save()
        }
        updateMinMax(set)
    }

    private fun updateMinMax(set: EnvDataSet) {
        //if (set.humidity > maxHum) maxHum = set.humidity
        //if (set.humidity < minHum) minHum = set.humidity

        if (set.temperature > maxTemp) maxTemp = set.temperature
        if (set.temperature < minTemp && set.temperature > -500) minTemp = set.temperature

        if (set.pressure > maxPres) maxPres = set.pressure
        if (set.pressure < minPres && set.pressure >= 0) minPres = set.pressure

        if (set.co2 > maxCO2) maxCO2 = set.co2.toInt()
        if (set.co2 < minCO2 && set.co2 >= 400) minCO2 = set.co2.toInt()

        if (set.tvoc > maxTVOC) maxTVOC = set.tvoc.toInt()
        if (set.tvoc < minTVOC && set.tvoc >= 0) minTVOC = set.tvoc.toInt()

        if (set.time < minTime) minTime = set.time
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

    private fun save() {
        saveFile("./config/data.json", customJson.encodeToString(this))
    }
}