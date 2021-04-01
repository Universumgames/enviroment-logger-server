package de.universegame.env_logger_server

import de.universegame.env_logger_server.CMMInfoJackson.auto
import de.universegame.env_logger_server.apirouter.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.*
import org.http4k.format.ConfigurableKotlinxSerialization
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Netty
import org.http4k.server.asServer
import java.util.*

val customJson = Json {
    encodeDefaults = true
    prettyPrint = true
    ignoreUnknownKeys = true
}

object CMMInfoJackson : ConfigurableKotlinxSerialization({
    encodeDefaults = true
    prettyPrint = true
    ignoreUnknownKeys = true
})

@Serializable
data class DataSet(
    val humidity: Double,
    val temperature: Double,
    val pressure: Double,
    val co2: Double,
    val tvoc: Double,
    val heightApproximation: Double,
    val time: Long
)

@Serializable
data class Data(
    val valueMap: MutableMap<String, MutableList<DataSet>> = mutableMapOf(),
    val uuids: MutableList<String> = mutableListOf(),
    var listSize: Int = 0
)

fun <T> MutableList<T>.prepend(element: T) {
    if (size > 0)
        add(0, element)
    else add(element)
}

val data: Data = Data()

fun main() {
    Logger.init(LoggingTypes.All)
    log("Init Routes")
    val handler = routes(
        "/env/{mac}/" bind Method.POST to { request: Request ->
            try {
                val hum = request.get("humidity") ?: ""
                val temp = request.get("temperature") ?: ""
                val pres = request.get("pressure") ?: ""
                val co2 = request.get("co2") ?: ""
                val tvoc = request.get("tvoc") ?: ""
                val heightAprox = request.get("heightapprox") ?: ""
                val mac = request.path("mac") ?: ""
                if (data.valueMap[mac] == null)
                    data.valueMap[mac] = mutableListOf()
                if (!data.uuids.contains(mac)) {
                    data.uuids.add(mac)
                    data.listSize++
                }
                data.valueMap[mac]?.prepend(
                    DataSet(
                        hum.toDouble(),
                        temp.toDouble(),
                        pres.toDouble(),
                        co2.toDouble(),
                        tvoc.toDouble(),
                        heightAprox.toDouble(),
                        Date().time
                    )
                )
                if (data.valueMap[mac]?.size!! > 60)
                    data.valueMap[mac]?.removeLast()
                //println(customJson.encodeToJsonElement(valueMap))
                val dataClone = data.copy()
                dataClone.valueMap.forEach{
                    if(it.value.size >0){
                        val first = it.value[0].copy()
                        it.value.clear()
                        it.value.add(first)
                    }

                }
                Response.invoke(Status.OK).with(Body.auto<Data>().toLens() of dataClone)
            } catch (e: Exception) {
                e.printStackTrace()
                Response.invoke(Status.INTERNAL_SERVER_ERROR)
            }
        }
    )
    log("Loading config")
    val configSetUp = loadConfig("./config/config.json")
    //storing new variables of class
    saveConfig("./config/config.json")

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