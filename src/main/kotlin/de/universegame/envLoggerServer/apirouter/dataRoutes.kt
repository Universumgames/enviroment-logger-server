package de.universegame.envLoggerServer.apirouter

import de.universegame.envLoggerServer.envData.EnvData
import de.universegame.envLoggerServer.envData.EnvHandler
import de.universegame.envLoggerServer.envData.getPrecisionByShortName
import de.universegame.envLoggerServer.envHandler
import de.universegame.envLoggerServer.http4kJsonConfig.auto
import de.universegame.envLoggerServer.svg.EnvDataSVGGenerator
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

/**
 * route object to handle all get request for json and svg data
 * */
val dataRoutes = routes(
    "/{type}/{timespan}" bind Method.GET to { request: Request ->
        try {
            val precision = getPrecisionByShortName(request.path("timespan") ?: "")
            val type = request.path("type") ?: ""
            val clone: EnvHandler
            synchronized(envHandler) {
                clone = envHandler.copy(precision)
            }
            if (type.toLowerCase() == "svg")
                Response(Status.OK).body(EnvDataSVGGenerator.getSVGData(precision, clone))
            else
                Response(Status.OK).with(Body.auto<EnvData>().toLens() of clone.getPrecision(precision))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    }
)