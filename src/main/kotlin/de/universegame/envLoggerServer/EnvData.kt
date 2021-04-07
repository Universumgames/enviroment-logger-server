package de.universegame.env_logger_server

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
enum class ENVDataPrecison {
    LATESTDATAONLY,
    LAST6MINUTES,
    LAST6HOURS_1SEC_PRECISION,
    LAST6DAYS_1_MIN_PRECISION,
    LAST6WEEKS_1_HOUR_PRECISION,
    LAST6MONTHS_1_HOUR_PRECISION,
    LAST6YEARS_6_HOUR_PRECISION
}

enum class EnvDataSelect {
    SECOND,
    MINUTE,
    HOUR,
    DAY,
    MONTH,
    YEAR
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
    @Transient
    /**stores data of last ~6 minutes*/
    var secondData: EnvData = EnvData(maxSize = 60 * 10 * 6, precision = ENVDataPrecison.LAST6MINUTES),
    @Transient
    /**stores data of last 6 hours with 1 second precision*/
    var minuteData: EnvData = EnvData(maxSize = 60 * 60 * 6, precision = ENVDataPrecison.LAST6HOURS_1SEC_PRECISION),
    @Transient
    /**stores data of last 6 days with 1 minute precision*/
    var hourData: EnvData = EnvData(maxSize = 60 * 24 * 6, precision = ENVDataPrecison.LAST6DAYS_1_MIN_PRECISION),
    @Transient
    /**stores data of last 6 weeks with 1 hour precision*/
    var dayData: EnvData = EnvData(maxSize = 24 * 7 * 6, precision = ENVDataPrecison.LAST6WEEKS_1_HOUR_PRECISION),
    @Transient
    /**stores data of last 6 months with 1 hour precision*/
    var monthData: EnvData = EnvData(maxSize = 24 * 30 * 6, precision = ENVDataPrecison.LAST6MONTHS_1_HOUR_PRECISION),
    @Transient
    /**stores data of last 6 years with 6 hour precision*/
    var yearData: EnvData = EnvData(maxSize = 4 * 365 * 6, precision = ENVDataPrecison.LAST6YEARS_6_HOUR_PRECISION)
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
            if (time.minute % 20 == 0)
                save()
        }
        if (time.hour != lastUpdatedHour) {
            dayData.addEntry(set)
            monthData.addEntry(set)
            if (time.hour % 6 == 0) {
                yearData.addEntry(set)
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
        saveFile(dir + "seconds.json", json.encodeToString(secondData), onlyIfEmpty)
        saveFile(dir + "minutes.json", json.encodeToString(minuteData), onlyIfEmpty)
        saveFile(dir + "hours.json", json.encodeToString(hourData), onlyIfEmpty)
        saveFile(dir + "days.json", json.encodeToString(dayData), onlyIfEmpty)
        saveFile(dir + "months.json", json.encodeToString(monthData), onlyIfEmpty)
        saveFile(dir + "years.json", json.encodeToString(yearData), onlyIfEmpty)
        log("Handler saved into $directory")
    }

    fun loadEnvDataFromFiles(directory: String, json: Json) {
        val dir = if (directory.endsWith("/") || directory.endsWith("\\")) directory else "$directory/"
        if (loadFile(dir + "seconds.json").isEmpty() ||
            loadFile(dir + "minutes.json").isEmpty() ||
            loadFile(dir + "hours.json").isEmpty() ||
            loadFile(dir + "days.json").isEmpty() ||
            loadFile(dir + "months.json").isEmpty() ||
            loadFile(dir + "years.json").isEmpty()
        )
            saveEnvDataToFiles(directory, json, true)
        else {
            secondData = json.decodeFromString(loadFile(dir + "seconds.json"))
            minuteData = json.decodeFromString(loadFile(dir + "minutes.json"))
            hourData = json.decodeFromString(loadFile(dir + "hours.json"))
            dayData = json.decodeFromString(loadFile(dir + "days.json"))
            monthData = json.decodeFromString(loadFile(dir + "months.json"))
            yearData = json.decodeFromString(loadFile(dir + "years.json"))
        }
        log("core Handler loaded")
    }

    fun copy(selected: EnvDataSelect): EnvHandler {
        val copy = EnvHandler()

        when (selected) {
            EnvDataSelect.SECOND -> copy.secondData = this.secondData.copy()
            EnvDataSelect.MINUTE -> copy.minuteData = this.minuteData.copy()
            EnvDataSelect.HOUR -> copy.hourData = this.hourData.copy()
            EnvDataSelect.DAY -> copy.dayData = this.dayData.copy()
            EnvDataSelect.MONTH -> copy.monthData = this.monthData.copy()
            EnvDataSelect.YEAR -> copy.yearData = this.yearData.copy()
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