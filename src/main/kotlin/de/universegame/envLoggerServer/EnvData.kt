package de.universegame.envLoggerServer

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
enum class ENVDataPrecision {
    LATESTDATAONLY,
    LAST6MINUTES,
    LAST6HOURS_3SEC_PRECISION,
    LASTDAY_30SEC_PRECISION,
    LAST6DAYS_1_MIN_PRECISION,
    LAST6WEEKS_1_HOUR_PRECISION,
    LAST6MONTHS_1_HOUR_PRECISION,
    LAST6YEARS_6_HOUR_PRECISION
}

fun getPrecisionByShortName(short: String): ENVDataPrecision{
    for(prec in ENVDataPrecision.values()){
        if(prec.name.toLowerCase().contains(short.toLowerCase()))
            return prec
    }
    return ENVDataPrecision.LATESTDATAONLY
}

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
    override val precision: ENVDataPrecision,
    override var listSize: Int = 0,
    val maxSize: Int = 500
) : IEnvData {

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

@Serializable
data class EnvHandler(
    var iotData: EnvData = EnvData(maxSize = 1, precision = ENVDataPrecision.LATESTDATAONLY),
    @Transient
    /**stores data of last ~6 minutes*/
    var last6Minutes: EnvData = EnvData(maxSize = 60 * 10 * 6, precision = ENVDataPrecision.LAST6MINUTES),
    @Transient
    /**stores data of last 6 hours with 3 second precision*/
    var last6Hours: EnvData = EnvData(maxSize = 60 * 60 * 6 / 3, precision = ENVDataPrecision.LAST6HOURS_3SEC_PRECISION),
    @Transient
    /**stores data of last day with 30 seconds precision*/
    var lastDay: EnvData = EnvData(maxSize = 60 * 60 * 24 / 30, precision = ENVDataPrecision.LASTDAY_30SEC_PRECISION),
    @Transient
    /**stores data of last 6 days with 1 minute precision*/
    var last6Days: EnvData = EnvData(maxSize = 60 * 24 * 6, precision = ENVDataPrecision.LAST6DAYS_1_MIN_PRECISION),
    @Transient
    /**stores data of last 6 weeks with 1 hour precision*/
    var last6Weeks: EnvData = EnvData(maxSize = 24 * 7 * 6, precision = ENVDataPrecision.LAST6WEEKS_1_HOUR_PRECISION),
    @Transient
    /**stores data of last 6 months with 1 hour precision*/
    var last6Months: EnvData = EnvData(maxSize = 24 * 30 * 6, precision = ENVDataPrecision.LAST6MONTHS_1_HOUR_PRECISION),
    @Transient
    /**stores data of last 6 years with 6 hour precision*/
    var last6Years: EnvData = EnvData(maxSize = 4 * 365 * 6, precision = ENVDataPrecision.LAST6YEARS_6_HOUR_PRECISION)
) {

    @Transient
    private var lastUpdatedSecond = 0

    @Transient
    private var lastUpdatedMinute = 0

    @Transient
    private var lastUpdatedHour = 0

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

    fun addEntry(set: EnvDataSet) {
        iotData.addEntry(set)
        last6Minutes.addEntry(set)
        val time = LocalDateTime.now()
        if (time.second != lastUpdatedSecond) {
            lastUpdatedSecond = time.second
            if (time.second % 3 == 0)
                last6Hours.addEntry(set)
            if(time.second % 30 == 0)
                lastDay.addEntry(set)
        }
        if (time.minute != lastUpdatedMinute) {
            lastUpdatedMinute = time.minute
            last6Days.addEntry(set)
            if (time.minute % 20 == 0)
                save()
        }
        if (time.hour != lastUpdatedHour) {
            last6Weeks.addEntry(set)
            last6Months.addEntry(set)
            if (time.hour % 6 == 0) {
                last6Years.addEntry(set)
                createBackup()
            }
            lastUpdatedHour = time.hour

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
        saveEnvDataToFiles("./data", customJson, onlyIfEmpty = false)
    }

    fun saveEnvDataToFiles(directory: String, json: Json, onlyIfEmpty: Boolean = false) {
        val dir = if (directory.endsWith("/") || directory.endsWith("\\")) directory else "$directory/"
        saveFile(dir + "handler.json", json.encodeToString(this), onlyIfEmpty)
        saveFile(dir + "last6Minutes.json", json.encodeToString(last6Minutes), onlyIfEmpty)
        saveFile(dir + "last6Hours.json", json.encodeToString(last6Hours), onlyIfEmpty)
        saveFile(dir + "lastDay.json", json.encodeToString(lastDay), onlyIfEmpty)
        saveFile(dir + "last6Days.json", json.encodeToString(last6Days), onlyIfEmpty)
        saveFile(dir + "last6Weeks.json", json.encodeToString(last6Weeks), onlyIfEmpty)
        saveFile(dir + "last6Months.json", json.encodeToString(last6Months), onlyIfEmpty)
        saveFile(dir + "last6Years.json", json.encodeToString(last6Years), onlyIfEmpty)
        log("Handler saved into $directory")
    }

    fun loadEnvDataFromFiles(directory: String, json: Json) {
        val dir = if (directory.endsWith("/") || directory.endsWith("\\")) directory else "$directory/"
        if (loadFile(dir + "last6Minutes.json").isEmpty() ||
            loadFile(dir + "last6Hours.json").isEmpty() ||
            loadFile(dir + "lastDay.json").isEmpty() ||
            loadFile(dir + "last6Days.json").isEmpty() ||
            loadFile(dir + "last6Weeks.json").isEmpty() ||
            loadFile(dir + "last6Months.json").isEmpty() ||
            loadFile(dir + "last6Years.json").isEmpty()
        )
            saveEnvDataToFiles(directory, json, true)
        else {
            last6Minutes = json.decodeFromString(loadFile(dir + "last6Minutes.json"))
            last6Hours = json.decodeFromString(loadFile(dir + "last6Hours.json"))
            lastDay = json.decodeFromString(loadFile(dir + "lastDay.json"))
            last6Days = json.decodeFromString(loadFile(dir + "last6Days.json"))
            last6Weeks = json.decodeFromString(loadFile(dir + "last6Weeks.json"))
            last6Months = json.decodeFromString(loadFile(dir + "last6Months.json"))
            last6Years = json.decodeFromString(loadFile(dir + "last6Years.json"))
        }
        log("core Handler loaded")
    }

    fun copy(selected: ENVDataPrecision): EnvHandler {
        val copy = EnvHandler()

        when (selected) {
            ENVDataPrecision.LATESTDATAONLY -> copy.iotData = iotData
            ENVDataPrecision.LAST6MINUTES -> copy.last6Minutes = last6Minutes
            ENVDataPrecision.LAST6HOURS_3SEC_PRECISION -> copy.last6Hours = last6Hours
            ENVDataPrecision.LASTDAY_30SEC_PRECISION -> copy.lastDay = lastDay
            ENVDataPrecision.LAST6DAYS_1_MIN_PRECISION -> copy.last6Days = last6Days
            ENVDataPrecision.LAST6WEEKS_1_HOUR_PRECISION -> copy.last6Weeks = last6Weeks
            ENVDataPrecision.LAST6MONTHS_1_HOUR_PRECISION -> copy.last6Months = last6Months
            ENVDataPrecision.LAST6YEARS_6_HOUR_PRECISION -> copy.last6Years = last6Years
        }

        copy.maxTemp = this.maxTemp
        copy.minTemp = this.minTemp
        copy.maxPres = this.maxPres
        copy.minPres = this.minPres
        copy.maxCO2 = this.maxCO2
        copy.minCO2 = this.minCO2
        copy.maxTVOC = this.maxTVOC
        copy.minTVOC = this.minTVOC
        return copy
    }

    fun getPrecision(precision: ENVDataPrecision): EnvData {
        return when (precision) {
            ENVDataPrecision.LATESTDATAONLY -> iotData
            ENVDataPrecision.LAST6MINUTES -> last6Minutes
            ENVDataPrecision.LAST6HOURS_3SEC_PRECISION -> last6Hours
            ENVDataPrecision.LASTDAY_30SEC_PRECISION -> lastDay
            ENVDataPrecision.LAST6DAYS_1_MIN_PRECISION -> last6Days
            ENVDataPrecision.LAST6WEEKS_1_HOUR_PRECISION -> last6Weeks
            ENVDataPrecision.LAST6MONTHS_1_HOUR_PRECISION -> last6Months
            ENVDataPrecision.LAST6YEARS_6_HOUR_PRECISION -> last6Years
        }
    }
}

fun createBackup(dataDir: String = "./data", backupDir: String = "./dataBackup") {
    if (copyDirectory(dataDir, backupDir))
        log("Backup created")
    else log("Backup was already created this hour, skipping...")
}

fun loadEnvHandlerFromFiles(directory: String, json: Json): EnvHandler {
    val dir = if (directory.endsWith("/") || directory.endsWith("\\")) directory else "$directory/"
    val handlerJson = loadFile(dir + "handler.json")
    val handler: EnvHandler
    if (handlerJson.isEmpty()) {
        handler = EnvHandler()
        handler.saveEnvDataToFiles(dir, json)
    } else
        handler = json.decodeFromString(handlerJson)
    log("raw Handler loaded")
    handler.loadEnvDataFromFiles(directory, json)
    log("Handler fully loaded")
    return handler
}