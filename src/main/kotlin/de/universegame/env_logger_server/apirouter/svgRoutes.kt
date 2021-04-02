package de.universegame.env_logger_server.apirouter

import de.universegame.env_logger_server.envHandler
import de.universegame.env_logger_server.svg.EnvDataSVGGenerator
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes

val svgRoutes = routes(
    "/minutes" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(envHandler.secondData))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/hours" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(envHandler.minuteData))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/days" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(envHandler.hourData))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/weeks" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(envHandler.dayData))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/months" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(envHandler.monthData))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/years" bind Method.GET to { request: Request ->
        try {
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(envHandler.yearData))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    }
)