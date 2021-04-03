package de.universegame.env_logger_server

import de.universegame.env_logger_server.apirouter.router
import de.universegame.env_logger_server.svg.EnvDataSVGGenerator
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.http4k.format.ConfigurableKotlinxSerialization
import org.http4k.server.Netty
import org.http4k.server.asServer

val customJson = Json {
    encodeDefaults = true
    prettyPrint = true
    ignoreUnknownKeys = true
}

object http4kJsonConfig : ConfigurableKotlinxSerialization({
    encodeDefaults = true
    prettyPrint = true
    ignoreUnknownKeys = true
})

fun <T> MutableList<T>.prepend(element: T) {
    if (size > 0)
        add(0, element)
    else add(element)
}

var envHandler: EnvHandler = EnvHandler()

fun main() {
    Logger.init(LoggingTypes.All)
    log("Init Routes")
    val handler = router
    log("Loading config")
    val configSetUp = loadConfig("./config/config.json")
    //storing new variables of class
    saveConfig("./config/config.json")

    var data = loadFile("./config/data.json")
    if (data.isEmpty()) {
        data = customJson.encodeToString(envHandler)
        saveFile("./config/data.json", data)
    } else
        envHandler = customJson.decodeFromString(data)

    saveFile(
        "./config/template.svg",
        EnvDataSVGGenerator.genSVG(envHandler.secondData, envHandler, debug = true).trimIndent().trimStart(' ')
    )

    if (configSetUp) {
        log("Load DB")
        //initializeDB()

        log("Starting server")
        val server = handler.asServer(Netty(config.serverConfig.serverPort)).start()
        log("To stop the server, type 'stop'")

        while (readLine() != "stop");
        log("Stopping server")
        server.stop()
        log("Stopped Server")
        Logger.stop()
    } else {
        log("Started server without existing config")
        log("Created config, not initialized database")
        log("Please set up the config file for proper functionality")
    }
}