package de.universegame.envLoggerServer.envData

import de.universegame.envLoggerServer.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.math.abs

@Serializable
data class EnvHandler(
    var iotData: EnvData = EnvData(precision = ENVDataPrecision.LATESTDATAONLY),
    @Transient
    /**stores data of last ~6 minutes*/
    var last6Minutes: EnvData = EnvData(precision = ENVDataPrecision.LAST6MINUTES_1SEC_PRECISION),
    @Transient
    /**stores data of last 6 hours with 3 second precision*/
    var last6Hours: EnvData = EnvData(precision = ENVDataPrecision.LAST6HOURS_3SEC_PRECISION),
    @Transient
    /**stores data of last day with 30 seconds precision*/
    var lastDay: EnvData = EnvData(precision = ENVDataPrecision.LASTDAY_30SEC_PRECISION),
    @Transient
    /**stores data of last 6 days with 1 minute precision*/
    var last6Days: EnvData = EnvData(precision = ENVDataPrecision.LAST6DAYS_1_MIN_PRECISION),
    @Transient
    /**stores data of last 6 weeks with 1 hour precision*/
    var last6Weeks: EnvData = EnvData(precision = ENVDataPrecision.LAST6WEEKS_1_HOUR_PRECISION),
    @Transient
    /**stores data of last 6 months with 1 hour precision*/
    var last6Months: EnvData = EnvData(precision = ENVDataPrecision.LAST6MONTHS_1_HOUR_PRECISION),
    @Transient
    /**stores data of last 6 years with 6 hour precision*/
    var last6Years: EnvData = EnvData(precision = ENVDataPrecision.LAST6YEARS_6_HOUR_PRECISION)
) {

    //last updated time
    @Transient
    private var lastUpdatedSecond = 0

    @Transient
    private var lastUpdatedMinute = 0

    @Transient
    private var lastUpdatedHour = 0

    //min/max values for setting range in svg
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

    /** IoT API endpoint handler
     * @param set add Dataset to structure, depending on the current time, time lat updated and purpose of subsets
     * */
    fun addEntry(set: EnvDataSet) {
        if (!dataSetValid(set))
            return
        iotData.addEntry(set)

        val time = LocalDateTime.now()
        if (time.second != lastUpdatedSecond) {
            last6Minutes.addEntry(set)
            lastUpdatedSecond = time.second
            if (time.second % 3 == 0)
                last6Hours.addEntry(set)
            if (time.second % 30 == 0)
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

    /**
     * internal method to update min/max values
     * */
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

    /** Internal method to save data struct easily to json files
     * */
    private fun save() {
        saveEnvDataToFiles("./data", customJson, onlyIfEmpty = false)
    }

    /**
     * Method to save data into multiple json files
     * @param directory Directory to save the json files to
     * @param json JSON object to configure json serializer
     * @param onlyIfEmpty define if existing files should be overwritten
     * */
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
        Logger.log("Handler saved into $directory")
    }

    /**
     * Method to load internal objects
     * @param directory Directory the data files were saved to
     * @param json JSON object to deserialize
     * */
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
        Logger.log("core Handler loaded")
    }

    /**
     * Simple method to create simple clone of current object based on desired precision
     * @param selected the selected precision
     * */
    fun copy(selected: ENVDataPrecision): EnvHandler {
        val copy = EnvHandler()

        when (selected) {
            ENVDataPrecision.LATESTDATAONLY -> copy.iotData = iotData
            ENVDataPrecision.LAST6MINUTES_1SEC_PRECISION -> copy.last6Minutes = last6Minutes
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

    /**
     * get data struct based on given precision
     * @param precision selected precision
     * @return return selected data struct
     * */
    fun getPrecision(precision: ENVDataPrecision): EnvData {
        return when (precision) {
            ENVDataPrecision.LATESTDATAONLY -> iotData
            ENVDataPrecision.LAST6MINUTES_1SEC_PRECISION -> last6Minutes
            ENVDataPrecision.LAST6HOURS_3SEC_PRECISION -> last6Hours
            ENVDataPrecision.LASTDAY_30SEC_PRECISION -> lastDay
            ENVDataPrecision.LAST6DAYS_1_MIN_PRECISION -> last6Days
            ENVDataPrecision.LAST6WEEKS_1_HOUR_PRECISION -> last6Weeks
            ENVDataPrecision.LAST6MONTHS_1_HOUR_PRECISION -> last6Months
            ENVDataPrecision.LAST6YEARS_6_HOUR_PRECISION -> last6Years
        }
    }

    @Transient
    var lastHum: Double = -1.0

    @Transient
    var lastTemp: Double = -1.0

    @Transient
    var lastPres: Double = -1.0

    /*@Transient
    var lastCO2: Int = -1

    @Transient
    var lastTVOC: Int = -1*/

    fun dataSetValid(set: EnvDataSet): Boolean {
        if (lastHum == -1.0)
            lastHum = set.humidity
        if (lastTemp == -1.0)
            lastTemp = set.temperature
        if (lastPres == -1.0)
            lastPres = set.pressure

        if ((abs(lastHum - set.humidity) < lastHum * 0.1) &&
            (abs(lastTemp - set.temperature) < lastTemp * 0.1) &&
            (abs(lastPres - set.pressure) < lastPres * 0.01)
        ) {
            lastHum += (set.humidity - lastHum) * .5
            lastTemp += (set.temperature - lastTemp) * .5
            lastPres += (set.pressure - lastPres) * .1
            return true
        } else return false
    }
}


/**
 * custom datetime formatter for formatting directory names
 * */
private var dateFormat: DateFormat = SimpleDateFormat("yyyy_MM_dd_hh")

/**
 * Creates a backup by copying all files into another directory
 * @param dataDir the directory the handler data is stored in
 * @param backupDir the directory the backup(folder) should be created in
 * */
fun createBackup(dataDir: String = "./data", backupDir: String = "./dataBackup") {
    if (copyDirectory(
            dataDir,
            (if (backupDir.endsWith("/") || backupDir.endsWith("\\")) backupDir else "$backupDir/") +
                    dateFormat.format(Calendar.getInstance().time)
        )
    )
        Logger.log("Backup created")
    else Logger.log("Backup was already created this hour, skipping...")
}

/**
 * if handler is stored in a directory, you can load it easily with this method
 * @param directory the directory the handler was saved into
 * @param json the JSON object to deserialize the data
 * */
fun loadEnvHandlerFromFiles(directory: String, json: Json): EnvHandler {
    val dir = if (directory.endsWith("/") || directory.endsWith("\\")) directory else "$directory/"
    val handlerJson = loadFile(dir + "handler.json")
    val handler: EnvHandler
    if (handlerJson.isEmpty()) {
        handler = EnvHandler()
        handler.saveEnvDataToFiles(dir, json)
    } else
        handler = json.decodeFromString(handlerJson)
    Logger.log("raw Handler loaded")
    handler.loadEnvDataFromFiles(directory, json)
    Logger.log("Handler fully loaded")
    return handler
}