package de.universegame.env_logger_server.apirouter

import de.universegame.env_logger_server.CMMInfoJackson.auto
import de.universegame.env_logger_server.EnvDataSet
import de.universegame.env_logger_server.IoTEnvData
import de.universegame.env_logger_server.envHandler
import org.http4k.core.*
import org.http4k.routing.path

val iotRoute = { request: Request ->
    try {
        val hum = request.get("humidity") ?: ""
        val temp = request.get("temperature") ?: ""
        val pres = request.get("pressure") ?: ""
        val co2 = request.get("co2") ?: ""
        val tvoc = request.get("tvoc") ?: ""
        val heightAprox = request.get("heightapprox") ?: ""
        val mac = request.path("mac") ?: ""

        envHandler.addEntry(
            EnvDataSet(
                hum.toDouble(),
                temp.toDouble(),
                pres.toDouble(),
                co2.toDouble(),
                tvoc.toDouble(),
                heightAprox.toDouble(),
                mac
            )
        )
        Response.invoke(Status.OK).with(Body.auto<IoTEnvData>().toLens() of envHandler.iotData)
    } catch (e: Exception) {
        e.printStackTrace()
        Response.invoke(Status.INTERNAL_SERVER_ERROR)
    }
}