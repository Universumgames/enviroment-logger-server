package de.universegame.envLoggerServer

import de.universegame.envLoggerServer.apirouter.router
import de.universegame.envLoggerServer.svg.EnvDataSVGGenerator
import kotlinx.serialization.json.Json
import org.http4k.format.ConfigurableKotlinxSerialization
import org.http4k.routing.bind
import org.http4k.routing.routes
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
    val handler = routes("/api" bind router)
    log("Loading config")
    val configSetUp = loadConfig("./config/config.json")
    //storing new variables of class
    saveConfig("./config/config.json")

    createBackup()
    envHandler = loadEnvHandlerFromFiles("./data", customJson)

    saveFile(
        "./data/svg/hourData.svg",
        EnvDataSVGGenerator.genSVG(envHandler.last6Days, envHandler, debug = true).trimIndent().trimStart(' ')
    )
    saveFile(
        "./data/svg/dayData.svg",
        EnvDataSVGGenerator.genSVG(envHandler.last6Weeks, envHandler, debug = true).trimIndent().trimStart(' ')
    )
    saveFile(
        "./data/svg/monthData.svg",
        EnvDataSVGGenerator.genSVG(envHandler.last6Months, envHandler, debug = true).trimIndent().trimStart(' ')
    )
    saveFile(
        "./data/svg/yearData.svg",
        EnvDataSVGGenerator.genSVG(envHandler.last6Years, envHandler, debug = true).trimIndent().trimStart(' ')
    )
    log("Generated SVG's")

    if (configSetUp) {
        log("Starting server")
        val server = handler.asServer(Netty(config.serverConfig.serverPort)).start()
        log("To stop the server, type 'stop'")

        while (readLine() != "stop");
        log("Stopping server")
        server.stop()

        envHandler.saveEnvDataToFiles("./data", customJson)
        log("Stopped Server")
        Logger.stop()
    } else {
        log("Started server without existing config")
        log("Created config, not initialized database")
        log("Please set up the config file for proper functionality")
    }
}