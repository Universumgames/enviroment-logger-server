package de.universegame.envLoggerServer.apirouter

import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes

/**
 * parent route object to handle all api routes
 * */
val router = routes(
    "/data" bind dataRoutes,
    "/env/iot/{mac}/" bind Method.POST to iotRoute,
    "/favicon.ico" bind Method.GET to { Response(Status.OK) }
)