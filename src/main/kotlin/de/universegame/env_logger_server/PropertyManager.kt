package de.universegame.env_logger_server

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File

/**
 * load configuration from file **filename**
 * @return true if file exists, false if file was created
 * **/
fun loadConfig(filename: String): Boolean {
    val file = File(filename).also { file ->
        file.parentFile.mkdirs()
    }
    log("Load config", LoggingTypes.UserRegistration)
    Logger.log("Load from ${file.absolutePath}")
    if (!file.exists()) {
        log("Config does not exists", LoggingTypes.UserRegistration)
        saveConfig(filename)
        return false
    }
    val jsonData = file.readText()
    config = customJson.decodeFromString(jsonData)
    log("Loaded Config successfully", LoggingTypes.UserRegistration)
    return true
}

/**
 * save configuration to file **filename**
 * @return true
 * **/
fun saveConfig(filename: String) {
    log("Created new config file", LoggingTypes.UserRegistration)
    val config: String = customJson.encodeToString(config)
    saveFile(filename, config)
    log("Saved config", LoggingTypes.UserRegistration)

}

fun loadFile(filename: String): String {
    val file = File(filename).also { file ->
        file.parentFile.mkdirs()
    }
    if (!file.exists()) saveFile(filename, "")
    return file.readText()
}

fun saveFile(filename: String, text: String) {
    val file = File(filename).also { file ->
        file.parentFile.mkdirs()
    }

    file.writeText(text)
}