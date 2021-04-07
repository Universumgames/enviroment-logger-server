package de.universegame.envLoggerServer

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


/**
 * load configuration from file **filename**
 * @return true if file exists, false if file was created
 * **/
fun loadConfig(filename: String): Boolean {
    val file = File(filename).also { file ->
        file.parentFile.mkdirs()
    }
    log("Load config")
    Logger.log("Load from ${file.absolutePath}")
    if (!file.exists()) {
        log("Config does not exists")
        saveConfig(filename)
        return false
    }
    val jsonData = file.readText()
    config = customJson.decodeFromString(jsonData)
    log("Loaded Config successfully")
    return true
}

/**
 * save configuration to file **filename**
 * @return true
 * **/
fun saveConfig(filename: String) {
    log("Created new config file")
    val config: String = customJson.encodeToString(config)
    saveFile(filename, config)
    log("Saved config")

}

fun loadFile(filename: String): String {
    val file = File(filename).also { file ->
        file.parentFile.mkdirs()
    }
    if (!file.exists()) saveFile(filename, "")
    return file.readText()
}

fun saveFile(filename: String, text: String, onlyIfEmpty: Boolean = true) {
    val file = File(filename).also { file ->
        file.parentFile.mkdirs()
    }
    if (!file.exists() || file.readText().isEmpty() || !onlyIfEmpty)
        file.writeText(text)
}

var dateFormat: DateFormat = SimpleDateFormat("yyyy_MM_dd_hh")

fun copyDirectory(inputDir: String, outputDir: String): Boolean {
    val IF = File(inputDir).also { file ->
        file.parentFile.mkdirs()
        file.mkdirs()
    }
    val outDir =
        (if (outputDir.endsWith("/") || outputDir.endsWith("\\")) outputDir else "$outputDir/") +
                dateFormat.format(Calendar.getInstance().time)
    val OF = File(outDir).also { file ->
        file.parentFile.mkdirs()
        file.mkdirs()
    }
    log(outDir)
    var returnVal: Boolean = true
    IF.copyRecursively(OF, false, onError = { file: File, ioException: IOException ->
        returnVal = false
        OnErrorAction.SKIP
    })
    return returnVal
}