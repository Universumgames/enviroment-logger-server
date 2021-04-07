package de.universegame.envLoggerServer.apirouter

import de.universegame.envLoggerServer.EnvData
import de.universegame.envLoggerServer.EnvHandler
import de.universegame.envLoggerServer.envHandler
import de.universegame.envLoggerServer.getPrecisionByShortName
import de.universegame.envLoggerServer.http4kJsonConfig.auto
import de.universegame.envLoggerServer.svg.EnvDataSVGGenerator
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

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
                Response(Status.OK).body(EnvDataSVGGenerator.genSVG(clone.getPrecision(precision), clone))
            else
                Response(Status.OK).with(Body.auto<EnvData>().toLens() of envHandler.last6Hours.copy())
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    }
)