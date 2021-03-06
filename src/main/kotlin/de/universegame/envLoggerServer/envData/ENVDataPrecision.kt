package de.universegame.envLoggerServer.envData

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
enum class ENVDataPrecision {
    LATESTDATAONLY,
    LAST6MINUTES_1SEC_PRECISION,
    LAST6HOURS_3SEC_PRECISION,
    LASTDAY_30SEC_PRECISION,
    LAST6DAYS_1_MIN_PRECISION,
    LAST6WEEKS_1_HOUR_PRECISION,
    LAST6MONTHS_1_HOUR_PRECISION,
    LAST6YEARS_6_HOUR_PRECISION
}

fun ENVDataPrecision.toListSize(): Int {
    return when (this) {
        ENVDataPrecision.LATESTDATAONLY -> 1
        ENVDataPrecision.LAST6MINUTES_1SEC_PRECISION -> 60 * 6 * 10
        ENVDataPrecision.LAST6HOURS_3SEC_PRECISION -> 60 * 60 * 6 / 3
        ENVDataPrecision.LASTDAY_30SEC_PRECISION -> 60 * 60 * 24 / 30
        ENVDataPrecision.LAST6DAYS_1_MIN_PRECISION -> 60 * 24 * 6
        ENVDataPrecision.LAST6WEEKS_1_HOUR_PRECISION -> 24 * 7 * 6
        ENVDataPrecision.LAST6MONTHS_1_HOUR_PRECISION -> 24 * 30 * 6
        ENVDataPrecision.LAST6YEARS_6_HOUR_PRECISION -> 4 * 365 * 6
    }
}

fun ENVDataPrecision.toOldestAllowedTime(): Long {
    return when (this) {
        ENVDataPrecision.LATESTDATAONLY -> 1
        ENVDataPrecision.LAST6MINUTES_1SEC_PRECISION -> 60 * 60
        ENVDataPrecision.LAST6HOURS_3SEC_PRECISION -> 6 * 60 * 60
        ENVDataPrecision.LASTDAY_30SEC_PRECISION -> 24 * 60 * 60
        ENVDataPrecision.LAST6DAYS_1_MIN_PRECISION -> 6 * 24 * 60 * 60
        ENVDataPrecision.LAST6WEEKS_1_HOUR_PRECISION -> 6 * 7 * 24 * 60 * 60
        ENVDataPrecision.LAST6MONTHS_1_HOUR_PRECISION -> 6 * 5 * 7 * 24 * 60 * 60
        ENVDataPrecision.LAST6YEARS_6_HOUR_PRECISION -> 355 * 24 * 60 * 60
    }.toLong() * 1000
}

fun getPrecisionByShortName(short: String): ENVDataPrecision {
    for (prec in ENVDataPrecision.values()) {
        if (prec.name.toLowerCase().contains(short.toLowerCase()))
            return prec
    }
    return ENVDataPrecision.LATESTDATAONLY
}