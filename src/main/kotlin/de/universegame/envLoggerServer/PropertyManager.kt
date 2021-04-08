package de.universegame.envLoggerServer

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.IOException

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

/**
 * Load text from file, if file dos not exists it will be created with no content
 * @param filename the filename that should be read from
 * @return content of file
 * */
fun loadFile(filename: String): String {
    val file = File(filename).also { file ->
        file.parentFile.mkdirs()
    }
    if (!file.exists()) saveFile(filename, "")
    return file.readText()
}

/**
 * save text to file
 * @param filename filename text should be written to
 * @param text the text that should be saved
 * @param onlyIfEmpty defines whether to overwrite possibly existing data
 * */
fun saveFile(filename: String, text: String, onlyIfEmpty: Boolean = true) {
    val file = File(filename).also { file ->
        file.parentFile.mkdirs()
    }
    if (!file.exists() || file.readText().isEmpty() || !onlyIfEmpty)
        file.writeText(text)
}

/**
 * Copy directory recursively
 * @param inputDir directory to copy from
 * @param outputDir directory to copy to
 * */
fun copyDirectory(inputDir: String, outputDir: String): Boolean {
    val IF = File(inputDir).also { file ->
        file.parentFile.mkdirs()
        file.mkdirs()
    }
    val OF = File(outputDir).also { file ->
        file.parentFile.mkdirs()
        file.mkdirs()
    }
    log(outputDir)
    var returnVal: Boolean = true
    IF.copyRecursively(OF, false, onError = { file: File, ioException: IOException ->
        returnVal = false
        OnErrorAction.SKIP
    })
    return returnVal
}