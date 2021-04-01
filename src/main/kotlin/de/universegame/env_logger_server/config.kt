package de.universegame.env_logger_server

import kotlinx.serialization.Serializable
import org.http4k.core.Status
import java.time.format.DateTimeFormatter

var config = Config()

var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern(config.datetimePattern)

@Serializable
data class Config(
    val datetimePattern: String = "yyyy-MM-dd HH:mm:ss.SSS",

    val secretJWTString: String = "aslkfdgj304rigjsokfj0wo3p45ktjsdlfoigjhwo39nslkfikgkupoidrjg",

    val serverConfig: ServerConfig = ServerConfig(),
    val httpResponses: HTTPResponsesConfig = HTTPResponsesConfig(),
    val dbConfig: DBConfig = DBConfig()
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

@Serializable
data class DBConfig(
    val mysqlUser: String = "env_logger",
    val mysqlPwd: String = "1qwSDCFGz7(iklBP,?WEdfghzu/(9oLp)",
    val mysqlUrl: String = "jdbc:mysql://localhost:3306/cmm?autoReconnect=true&useSSL=false"
)
