package de.universegame.env_logger_server.apirouter

import de.universegame.env_logger_server.EnvData
import de.universegame.env_logger_server.envHandler
import de.universegame.env_logger_server.http4kJsonConfig.auto
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.routes

val jsonRoutes = routes(
    "/minutes" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).with(Body.auto<EnvData>().toLens() of envHandler.minuteData)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/hours" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).with(Body.auto<EnvData>().toLens() of envHandler.hourData)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/all" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).with(Body.auto<EnvData>().toLens() of envHandler.secondData)
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    }
)