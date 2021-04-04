package de.universegame.env_logger_server

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun log(line: String, loggingType: LoggingTypes = LoggingTypes.Undefined) {
    Logger.log(line, loggingType)
}

enum class LoggingTypes {
    All,
    Debug,
    Error,
    Undefined
}

object Logger {
    private var initialized: Boolean = false
    private lateinit var file: File
    private var logDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/mm/yy")

    private var settings: MutableList<LoggingTypes> = mutableListOf()

    /**
     * @param settings Select types of logs you want to see, all others will be shut off
     * @param filename set the filename and path the log should be saved to, leave empty to use current datetime to save it in the *logs* folder
     * */
    fun init(vararg settings: LoggingTypes, filename: String = "") {
        var fileName = filename
        if (fileName == "")
            fileName = "./logs/${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm"))}.log"
        simplifySettings(settings.toMutableList())
        init(fileName)
    }

    private fun init(filename: String) {
        file = File(filename).also { file ->
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
        initialized = true
        log("Create file ${file.absolutePath}")
    }

    private fun simplifySettings(set: MutableList<LoggingTypes>) {
        set.forEach { type: LoggingTypes ->
            when (type) {
                LoggingTypes.All -> {
                    settings.addAll(LoggingTypes.values().filter { it !in settings })
                    return
                }
                LoggingTypes.Debug -> {
                    settings.addAll(LoggingTypes.values().filter { it !in settings })
                    return
                }
                else ->
                    if (type !in settings)
                        settings.add(type)
            }
        }
        settings.add(LoggingTypes.Undefined)
        if(set.isEmpty())
            settings.addAll(LoggingTypes.values().filter { it !in settings })
    }

    private fun getDTString(): String {
        return LocalDateTime.now().format(logDateTimeFormatter)
    }

    fun log(line: String) {
        val text = "${getDTString()}: $line\n"
        if (initialized) {
            file.appendText(text)
        }
        println(text.dropLast(1))
    }

    fun log(line: String, type: LoggingTypes) {
        if (settings.contains(type))
            log("${if (type != LoggingTypes.Undefined) "[${type.name}]" else ""} $line")
    }

    fun stop() {
        println("log location: ${file.absolutePath}")
    }

}