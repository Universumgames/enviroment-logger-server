package de.universegame.env_logger_server.apirouter

import de.universegame.env_logger_server.EnvData
import de.universegame.env_logger_server.EnvDataSet
import de.universegame.env_logger_server.envHandler
import de.universegame.env_logger_server.http4kJsonConfig.auto
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


        synchronized(envHandler) {
            envHandler.addEntry(
                EnvDataSet(
                    if (hum.contains("nan")) 0.0 else hum.toDouble(),
                    if (temp.contains("nan")) 0.0 else temp.toDouble(),
                    if (pres.contains("nan")) 0.0 else pres.toDouble(),
                    if (co2.contains("nan")) 0.0 else co2.toDouble(),
                    if (tvoc.contains("nan")) 0.0 else tvoc.toDouble(),
                    if (heightAprox.contains("inf")) 0.0 else heightAprox.toDouble(),
                    mac
                )
            )
            Response.invoke(Status.OK).with(Body.auto<EnvData>().toLens().of(envHandler.iotData))
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Response.invoke(Status.INTERNAL_SERVER_ERROR)
    }
}