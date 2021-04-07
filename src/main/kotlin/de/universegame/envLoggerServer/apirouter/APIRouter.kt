package de.universegame.env_logger_server.apirouter

import de.universegame.env_logger_server.LoggingTypes
import de.universegame.env_logger_server.log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.PathMethod
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind

//Pattern String handle org.http4k.core.Method with Security by Handler

val test1: (request: Request, securityData: SecurityData) -> Response =
    { _: Request, _: SecurityData ->
        Response(Status.OK).body("test1")
    }

val test2: (request: Request, securityData: SecurityData) -> Response =
    { _: Request, _: SecurityData? ->
        Response(Status.OK).body("test2")
    }

fun Request.get(variable: String): String?{
    var returnVar : String? = this.query(variable.toLowerCase())
    if(returnVar == null && this.bodyString().isNotEmpty())
        returnVar = try {
            this.bodyString().decodeJson()[variable.toLowerCase()]
        }catch(e: Exception){
            log(e.stackTraceToString(), LoggingTypes.Error)
            null
        }
    return returnVar
}

private fun String.decodeJson():Map<String, String>{
    return Json.decodeFromString(this) as Map<String, String>
}

/**
 * Level of Authentication
 * */
enum class SecurityLevel {
    NONE,
    APITOKEN,
    DEVICE,
    USER,
    ADMIN
}

data class SecurityHandler(
    val secLevel: SecurityLevel,
    val path: PathMethod
)

data class SecurityData(
        val uuid: String
)

data class HTTPMethodEndpoint(
    var path: PathMethod
)

infix fun String.handle(method: Method): HTTPMethodEndpoint {
    return HTTPMethodEndpoint(this bind method)
}

infix fun HTTPMethodEndpoint.with(s: SecurityLevel): SecurityHandler {
    return SecurityHandler(s, path)
}

/**
 * @param handler handler method with http4k.*.Request and SecurityData? (SecurityData can be null or it's contents, depending on the routing setting and the calling method)
 * */
infix fun SecurityHandler.by(handler: (Request, SecurityData) -> Response): RoutingHttpHandler {
    return path to {
        authFilter(it, this, handler)
    }
}

fun authFilter(request: Request, s: SecurityHandler, handler: (Request, SecurityData) -> Response): Response {
    /*val apiToken: String? = request.get("apitoken")
    val deviceSecret: String? = request.get("devicesecret")
    val sessionToken: String? = request.get("sessiontoken")
    val cookie: Cookie? = request.cookie("cmmJWT")
    var apiLevel = false
    var deviceLevel = false
    var userLevel = false
    val adminLevel: Boolean
    //loading database data from passed data
    if (s.secLevel!= SecurityLevel.NONE && apiToken == null && deviceSecret == null && sessionToken == null && cookie == null)
        return Response(Status.BAD_REQUEST)
    val user = CMMUser()
    val device = CMMDevice()
    if (apiToken != null) {
        user.loadFromAPIToken(apiToken)
        if (!user.isFullyLoaded()) return Response(Status.UNAUTHORIZED)
        apiLevel = true
    }
    if (deviceSecret != null) {
        device.loadFromDeviceSecret(deviceSecret)
        user.loadFromUUID(device.userUUID)
        if (!device.isFullyLoaded()) return Response(Status.UNAUTHORIZED)
        apiLevel = true
        deviceLevel = true
    }
    if (sessionToken != null) {
        user.loadFromSessionToken(sessionToken)
        if (!user.isFullyLoaded()) return Response(Status.UNAUTHORIZED)
        apiLevel = true
        deviceLevel = true
        userLevel = true
    }
    if (cookie != null) {
        user.loadFromCookie(cookie)
        if (!user.isFullyLoaded()) return Response(Status.UNAUTHORIZED)
        apiLevel = true
        deviceLevel = true
        userLevel = true
    }
    adminLevel = user.admin

    log("Handle uri: " + request.uri + " with " + s.secLevel.toString(), LoggingTypes.Debug)
    try {
        when (s.secLevel) {
            SecurityLevel.NONE -> return handler(request, SecurityData(null, null))
            SecurityLevel.APITOKEN -> {
                if (apiLevel)
                    return handler(
                        request,
                        SecurityData(
                            if (user.isFullyLoaded()) user else null,
                            if (device.isFullyLoaded()) device else null
                        )
                    )
                else Response(Status.UNAUTHORIZED)
            }
            SecurityLevel.DEVICE -> {
                if (deviceLevel)
                    return handler(
                        request,
                        SecurityData(
                            if (user.isFullyLoaded()) user else null,
                            if (device.isFullyLoaded()) device else null
                        )
                    )
                else Response(Status.UNAUTHORIZED)
            }
            SecurityLevel.USER -> {
                if (userLevel)
                    return handler(
                        request,
                        SecurityData(
                            if (user.isFullyLoaded()) user else null,
                            if (device.isFullyLoaded()) device else null
                        )
                    )
                else Response(Status.UNAUTHORIZED)
            }
            SecurityLevel.ADMIN -> {
                return if (adminLevel)
                    handler(request, SecurityData(user, null))
                else
                    Response(Status.UNAUTHORIZED)
            }
        }
    } catch (e: Exception) {
        log(e.stackTraceToString(), LoggingTypes.Error)
    }*/
    return Response(Status.NOT_FOUND)
}