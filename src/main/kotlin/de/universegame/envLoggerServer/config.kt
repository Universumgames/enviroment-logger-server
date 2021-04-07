package de.universegame.envLoggerServer

import kotlinx.serialization.Serializable
import org.http4k.core.Status
import java.time.format.DateTimeFormatter

var config = Config()

var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(config.datetimePattern)

@Serializable
data class Config(
    val datetimePattern: String = "yyyy-MM-dd HH:mm:ss.SSS",

    val serverConfig: ServerConfig = ServerConfig(),
    val httpResponses: HTTPResponsesConfig = HTTPResponsesConfig(),
) {
}

@Serializable
data class ServerConfig(
    val serverPrefix: String = "http",
    val serverAddress: String = "localhost",
    val serverPort: Int = 8085
)

@Serializable
data class HTTPResponsesConfig(
    @Serializable
    val inDatabaseNotFound: Int = Status.PRECONDITION_FAILED.code,
    val missingQuery: Int = Status.BAD_REQUEST.code
)

fun Int.toStatus(): Status {
    return Status(this, "cast")
}
