package de.universegame.env_logger_server.apirouter

import de.universegame.env_logger_server.EnvDataSelect
import de.universegame.env_logger_server.EnvHandler
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
            val clone: EnvHandler
            synchronized(envHandler) {
                clone = envHandler.copy(EnvDataSelect.SECOND)
            }
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(clone.secondData, clone))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/hours" bind Method.GET to { request: Request ->
        try {
            val clone: EnvHandler
            synchronized(envHandler) {
                clone = envHandler.copy(EnvDataSelect.MINUTE)
            }
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(clone.minuteData, clone))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/days" bind Method.GET to { request: Request ->
        try {
            val clone: EnvHandler
            synchronized(envHandler) {
                clone = envHandler.copy(EnvDataSelect.HOUR)
            }
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(clone.hourData, clone))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/weeks" bind Method.GET to { request: Request ->
        try {
            val clone: EnvHandler
            synchronized(envHandler) {
                clone = envHandler.copy(EnvDataSelect.DAY)
            }
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(clone.dayData, clone))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/months" bind Method.GET to { request: Request ->
        try {
            val clone: EnvHandler
            synchronized(envHandler) {
                clone = envHandler.copy(EnvDataSelect.MONTH)
            }
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(clone.monthData, clone))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    },
    "/years" bind Method.GET to { request: Request ->
        try {
            val clone: EnvHandler
            synchronized(envHandler) {
                clone = envHandler.copy(EnvDataSelect.YEAR)
            }
            Response(Status.OK).body(EnvDataSVGGenerator.genSVG(clone.yearData, clone))
        } catch (e: Exception) {
            e.printStackTrace()
            Response.invoke(Status.INTERNAL_SERVER_ERROR)
        }
    }
)