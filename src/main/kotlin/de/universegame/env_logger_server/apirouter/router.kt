package de.universegame.env_logger_server.apirouter

import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.routes

val router = routes(
    "/json" bind jsonRoutes,
    "/svg" bind svgRoutes,
    "/env/iot/{mac}/" bind Method.POST to iotRoute
)