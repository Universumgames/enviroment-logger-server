package de.universegame.env_logger_server.svg

import de.universegame.env_logger_server.ENVDataPrecison
import de.universegame.env_logger_server.EnvData
import de.universegame.env_logger_server.EnvHandler
import de.universegame.env_logger_server.map
import java.util.*
import kotlin.math.abs

private data class MinMaxValueSet(
    var maxTemp: Double = Double.MIN_VALUE,
    var minTemp: Double = Double.MAX_VALUE,
    var maxPres: Double = Double.MIN_VALUE,
    var minPres: Double = Double.MAX_VALUE,
    var maxCO2: Int = Int.MIN_VALUE,
    var minCO2: Int = Int.MAX_VALUE,
    var maxTVOC: Int = Int.MIN_VALUE,
    var minTVOC: Int = Int.MAX_VALUE,
    var minTime: Long = 0L
)

object EnvDataSVGGenerator {

    fun genSVG(
        dataSet: EnvData,
        handler: EnvHandler,
        debug: Boolean = false
    ): String {
        val height: Int = 1000
        val width: Int = 1700
        val dims = NamedGrid_Dimensions(120.0, 1590.0, 40.0, 960.0)
        val textData = NamedGrid_TextData(5.0, 120.0, 1700.0)
        val minMaxValueSet: MinMaxValueSet = MinMaxValueSet(
            handler.maxTemp,
            handler.minTemp,
            handler.maxPres,
            handler.minPres,
            handler.maxCO2,
            handler.minCO2,
            handler.maxTVOC,
            handler.minTVOC,
            handler.minTime
        )
        return """
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<svg:svg xmlns:svg="http://www.w3.org/2000/svg" viewBox="0 0 1700 1000" version="1.1" ${if (debug) """style="background-color:green"""" else ""} xmlns="http://www.w3.org/2000/svg">
    <defs>
        <style type="text/css">
            *{
                stroke-linecap:butt;
                stroke-linejoin:round;
                font-family: 'Roboto', sans-serif;
            }
        </style>
    </defs>

    <svg:rect width="$width" height="$height" style="fill:rgb(255,255,255)"/>
    ${
            namedGrid(
                listOf("Temperature", "Humidity", "Pressure", "CO2", "TVOC"),
                genMajColText(dataSet.precision),
                RowValueGenerator.genRowValues(
                    handler.maxTemp,
                    handler.minTemp,
                    handler.maxPres,
                    handler.minPres,
                    handler.maxCO2,
                    handler.minCO2,
                    handler.maxTVOC,
                    handler.minTVOC
                ),
                genMinColText(dataSet.precision), dims, textData, 5, 6
            )
        }
        ${drawValues(dataSet, minMaxValueSet, dims)}
</svg:svg>

""".trimIndent()
    }

    private fun genMajColText(range: ENVDataPrecison): List<String> {
        var list: MutableList<String> = mutableListOf()
        when (range) {
            ENVDataPrecison.LAST6MINUTES -> list =
                mutableListOf("-5 minutes", "-4 minutes", "-3 minutes", "-2 minute", "-1 minute", "this minute")
            ENVDataPrecison.LAST6HOURS_1SEC_PRECISION -> list =
                mutableListOf("-5 hours", "-4 hours", "-3 hours", "-2 hours", "-1 hour", "this hour")
            ENVDataPrecison.LAST6DAYS_1_MIN_PRECISION -> list =
                mutableListOf("-5 days", "-4 days", "-3 days", "-2 days", "yesterday", "today")
            ENVDataPrecison.LAST6WEEKS_1_HOUR_PRECISION -> list =
                mutableListOf("-5 weeks", "-4 weeks", "-3 weeks", "-2 weeks", "last week", "last 7 days")
            ENVDataPrecison.LAST6MONTHS_1_HOUR_PRECISION -> list =
                mutableListOf("-5 months", "-4 months", "-3 months", "-2 months", "last month", "last 30 days")
            ENVDataPrecison.LAST6YEARS_6_HOUR_PRECISION -> list =
                mutableListOf("-5 years", "-4 years", "-3 years", "-2 years", "last year", "last 365 days")
            ENVDataPrecison.LATESTDATAONLY -> list = mutableListOf()
        }
        return list
    }

    private fun genMinColText(range: ENVDataPrecison, cols: Int = 6, subCols: Int = 6): List<String> {
        when (range) {
            ENVDataPrecison.LAST6MINUTES -> return genRecurring("s", 60, subCols, cols)
            ENVDataPrecison.LAST6HOURS_1SEC_PRECISION -> return genRecurring("m", 60, subCols, cols)
            ENVDataPrecison.LAST6DAYS_1_MIN_PRECISION -> return genRecurring("h", 24, subCols, cols)
            ENVDataPrecison.LAST6WEEKS_1_HOUR_PRECISION -> return genRecurring("d", 7, subCols, cols)
            ENVDataPrecison.LAST6MONTHS_1_HOUR_PRECISION -> return genRecurring("w", 4, subCols, cols)
            ENVDataPrecison.LAST6YEARS_6_HOUR_PRECISION -> return genRecurring("M", 12, subCols, cols)
            ENVDataPrecison.LATESTDATAONLY -> return listOf()
        }
    }

    fun genRecurring(sizeName: String, maxValue: Int, subCols: Int = 6, cols: Int = 6): List<String> {
        val list: MutableList<String> = mutableListOf()
        val interval = maxValue / subCols.toDouble()
        for (i in 0..((maxValue / interval) * cols).toInt()) {
            list.add(((i * interval) % maxValue).toInt().toString() + sizeName)
        }
        return list
    }

    private fun drawValues(
        dataSet: EnvData,
        minMax: MinMaxValueSet,
        dims: NamedGrid_Dimensions,
        rowCount: Int = 5,
        colorSet: List<String> = listOf("green", "red", "blue", "violet", "gray")
    ): String {
        var text = """<svg:g id="lines"> """ + "\n"
        var setIndex = 0
        val height = (dims.endYGrid - dims.startYGrid) / rowCount
        val minTime = minMax.minTime
        val maxTime = Date().time
        for (mapEntry in dataSet.valueMap) {
            val color: String = if (colorSet.size - 1 > setIndex) colorSet[setIndex] else ""
            text += """<svg:g id="${mapEntry.key}" style="fill: none; stroke: $color;stroke-width:2">"  """ + "\n"
            var temp = """<polyline id="temp" points=" """ + "\n"
            var hum = """<polyline id="hum" points=" """ + "\n"
            var pres = """<polyline id="pres" points=" """ + "\n"
            var co2 = """<polyline id="co2" points=" """ + "\n"
            var tvoc = """<polyline id="tvoc" points=" """ + "\n"
            for (entry in mapEntry.value) {
                val mapX = map(
                    entry.time,
                    minTime,
                    maxTime,
                    dims.startXGrid,
                    dims.endXGrid
                )

                if(entry.temperature > -500.0) {
                    run { //temp
                        val mapY = map(
                            entry.temperature,
                            minMax.minTemp,
                            minMax.maxTemp,
                            dims.startYGrid + 1 * height,
                            dims.startYGrid + 0 * height,
                        )

                        temp += xyToString(mapY, mapX)
                    }
                }
                if(entry.humidity >= 0.0) {
                    run { //hum
                        val mapY = map(
                            entry.humidity,
                            0.0,
                            100.0,
                            dims.startYGrid + 2 * height,
                            dims.startYGrid + 1 * height,
                        )

                        hum += xyToString(mapY, mapX)
                    }
                }
                if(entry.pressure >= 0) {
                    run { //pres
                        val mapY = map(
                            entry.pressure,
                            minMax.minPres,
                            minMax.maxPres,
                            dims.startYGrid + 3 * height,
                            dims.startYGrid + 2 * height,
                        )

                        pres += xyToString(mapY, mapX)
                    }
                }
                if(entry.co2 >= 0) {
                    run { //co2
                        val mapY = map(
                            entry.co2,
                            minMax.minCO2.toDouble(),
                            minMax.maxCO2.toDouble(),
                            dims.startYGrid + 4 * height,
                            dims.startYGrid + 3 * height,
                        )
                        co2 += xyToString(mapY, mapX)
                    }
                }
                if(entry.tvoc >= 0) {
                    run { //tvoc
                        val mapY = map(
                            entry.tvoc,
                            minMax.minTVOC.toDouble(),
                            minMax.maxTVOC.toDouble(),
                            dims.startYGrid + 5 * height,
                            dims.startYGrid + 4 * height,
                        )
                        tvoc += xyToString(mapY, mapX)
                    }
                }
            }
            temp += """ "/> """ + "\n"
            hum += """ "/> """ + "\n"
            pres += """ "/> """ + "\n"
            co2 += """ "/> """ + "\n"
            tvoc += """ "/> """ + "\n"

            text += temp + hum + pres + co2 + tvoc
            text += """</svg:g>""" + "\n"
            setIndex++
        }
        text += """</svg:g>"""
        return text
    }

    private fun xyToString(x: Double, y: Double): String {
        return "$y,$x "
    }
}


private object RowValueGenerator {

    fun genRowValues(
        maxTemp: Double,
        minTemp: Double,
        maxPres: Double,
        minPres: Double,
        maxCO2: Int,
        minCO2: Int,
        maxTVOC: Int,
        minTVOC: Int,
        separations: Int = 5
    ): List<String> {
        val list: MutableList<String> = mutableListOf()
        val temp = genTempRowValues(maxTemp, minTemp, separations)
        val hum = genHumRowValues(separations)
        val pres = genPresRowValues(maxPres, minPres, separations)
        val co2 = genCO2RowValues(maxCO2, minCO2, separations)
        val tvoc = genTVOCRowValues(maxTVOC, minTVOC, separations)
        list.addAll(temp)
        list.addAll(hum)
        list.addAll(pres)
        list.addAll(co2)
        list.addAll(tvoc)
        val final: MutableList<String> = mutableListOf()
        var index = 0
        var combineNext = false
        var combined = 0
        for (entry in list) {
            if ((index - combined) % separations == 0 && !(index == 0 || index == list.size - 1) && !combineNext) {
                combineNext = true
                index++
                continue
            }
            if (combineNext) {
                final.add(list[index - 1] + "/" + entry)
                combineNext = false
                combined++
                index++
                continue
            }
            final.add(entry)
            index++

        }
        return final
    }

    private fun genTempRowValues(maxTemp: Double, minTemp: Double, separations: Int): List<String> {
        val list: MutableList<String> = mutableListOf()
        val distance = abs((maxTemp - minTemp) / separations)
        var curDistance = maxTemp
        for (i in 0..separations) {
            list.add(curDistance.toInt().toString() + "°C")
            curDistance -= distance
        }
        return list
    }

    private fun genHumRowValues(separations: Int): List<String> {
        val list: MutableList<String> = mutableListOf()
        val distance = 100 / separations.toDouble()
        var curDistance = 100.toDouble()
        for (i in 0..separations) {
            list.add("${curDistance.toInt()}%")
            curDistance -= distance
        }
        return list
    }

    private fun genPresRowValues(maxPres: Double, minPres: Double, separations: Int): List<String> {
        val list: MutableList<String> = mutableListOf()
        val distance = (maxPres - minPres) / separations
        var curDistance = maxPres
        for (i in 0..separations) {
            list.add(curDistance.toInt().toString() + "hPa")
            curDistance -= distance
        }
        return list
    }

    private fun genCO2RowValues(maxCO2: Int, minCO2: Int, separations: Int): List<String> {
        val list: MutableList<String> = mutableListOf()
        val distance = (maxCO2 - minCO2).toDouble() / separations
        var curDistance = maxCO2.toDouble()
        for (i in 0..separations) {
            list.add(curDistance.toInt().toString())
            curDistance -= distance
        }
        return list
    }

    private fun genTVOCRowValues(maxTVOC: Int, minTVOC: Int, separations: Int): List<String> {
        val list: MutableList<String> = mutableListOf()
        val distance = (maxTVOC - minTVOC).toDouble() / separations
        var curDistance = maxTVOC.toDouble()
        for (i in 0..separations) {
            list.add(curDistance.toInt().toString())
            curDistance -= distance
        }
        return list
    }
}