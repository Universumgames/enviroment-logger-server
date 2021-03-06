package de.universegame.envLoggerServer.svg

/**
 * Data struct to define the size if the simple named grid
 * */
data class NamedGrid_Dimensions(
    val startXGrid: Double,
    val endXGrid: Double,
    val startYGrid: Double,
    val endYGrid: Double
)

/**
 * Simple data struct to set settings or the text placed in the svg
 * */
data class NamedGrid_TextData(
    val startXMajRowText: Double,
    val endXMajRowText: Double,
    val endXText: Double,
    val offsetYForTextRow: Double = 50.0,
    val distanceTextGridMajCol: Double = 10.0,
    val distanceTextGridMinCol: Double = 20.0,
    val textColor: String = "black",
    val avgCharLength: Double = 10.0
)

/**
 * create a named grid
 * */
fun namedGrid(
    majRowNames: List<String>,
    majColNames: List<String>,
    minRowNames: List<String>,
    minColNames: List<String>,
    gridDims: NamedGrid_Dimensions,
    textData: NamedGrid_TextData,
    numSubRowsPerRow: Int,
    numSubColsPerCol: Int
): String {
    return """<svg:g id="namedGrid">
        ${majColumnNames(majColNames, gridDims, textData)}
        ${majRowNames(majRowNames, gridDims, textData)}
        ${minRowNames(minRowNames, gridDims, textData)}
        ${minColumnNames(minColNames, gridDims, textData)}
    ${
        grid_Template(
            gridDims.startXGrid,
            gridDims.startYGrid,
            gridDims.endXGrid,
            gridDims.endYGrid,
            majRowNames.size,
            numSubRowsPerRow,
            majColNames.size,
            numSubColsPerCol
        )
    }
    </svg:g>
    """.trimIndent()
}

/** Internal method to generate the svg elements for the left hand side of the grid
 * @param names list of strings for the row-declarations
 * @param gridDims data struct for the grid dimensions
 * @param textData data struct for the text settings
 * */
private fun majRowNames(
    names: List<String>,
    gridDims: NamedGrid_Dimensions,
    textData: NamedGrid_TextData
): String {
    var lines = """<svg:g id="majRowTextNames" fill="${textData.textColor}">""" + "\n"
    val maxTextLength = textData.endXMajRowText - textData.startXMajRowText
    val distance = (gridDims.endYGrid - gridDims.startYGrid) / names.size
    var curDistance = gridDims.startYGrid + textData.offsetYForTextRow
    for (name in names) {
        val nameLength = name.length * textData.avgCharLength
        val offsetX = (maxTextLength - nameLength) / 2
        lines += """<svg:text x="${textData.startXMajRowText + offsetX}" y="${curDistance}">${name}</svg:text>""" + "\n"
        curDistance += distance
    }
    lines += """</svg:g>"""
    return lines
}

/** Internal method to generate the svg elements for the top column names of the grid
 * @param names list of strings for the row-declarations
 * @param gridDims data struct for the grid dimensions
 * @param textData data struct for the text settings
 * */
private fun majColumnNames(
    names: List<String>,
    gridDims: NamedGrid_Dimensions,
    textData: NamedGrid_TextData
): String {
    var lines = """<svg:g id="majColTextNames" fill="${textData.textColor}">""" + "\n"
    val distance = (gridDims.endXGrid - gridDims.startXGrid) / names.size
    var curDistance = gridDims.startXGrid
    for (name in names) {
        val nameLength = name.length * textData.avgCharLength
        val startXText = curDistance + (distance - nameLength) / 2
        lines += """<svg:text x="${startXText}" y="${gridDims.startYGrid - textData.distanceTextGridMajCol}">${name}</svg:text>""" + "\n"
        curDistance += distance
    }
    lines += """</svg:g>"""
    return lines
}

/** Internal method to generate the svg elements for the right hand side of the grid
 * @param names list of strings for the row-declarations
 * @param gridDims data struct for the grid dimensions
 * @param textData data struct for the text settings
 * */
private fun minRowNames(
    names: List<String>,
    gridDims: NamedGrid_Dimensions,
    textData: NamedGrid_TextData
): String {
    var lines = """<svg:g id="minRowTextNames" fill="${textData.textColor}">""" + "\n"
    val maxTextLength = textData.endXText - gridDims.endXGrid
    val distance = (gridDims.endYGrid - gridDims.startYGrid) / (names.size - 1)
    var curDistance = gridDims.startYGrid
    for (name in names) {
        val nameLength = name.length * textData.avgCharLength
        val offsetX = (maxTextLength - nameLength) / 2
        val textXStart = gridDims.endXGrid + 5// + offset
        lines += """<svg:text x="${textXStart}" y="${curDistance + 2}">${name}</svg:text>""" + "\n"
        curDistance += distance
    }
    lines += """</svg:g>"""
    return lines
}

/** Internal method to generate the svg elements for the bottom side of the grid
 * @param names list of strings for the row-declarations
 * @param gridDims data struct for the grid dimensions
 * @param textData data struct for the text settings
 * */
private fun minColumnNames(
    names: List<String>,
    gridDims: NamedGrid_Dimensions,
    textData: NamedGrid_TextData
): String {
    var lines = """<svg:g id="minColTextNames" fill="${textData.textColor}">""" + "\n"
    val distance = (gridDims.endXGrid - gridDims.startXGrid) / (names.size - 1)
    var curDistance = gridDims.startXGrid
    for (name in names) {
        val nameLength = name.length * textData.avgCharLength
        val startXText = curDistance - nameLength / 2
        lines += """<svg:text x="${startXText}" y="${gridDims.endYGrid + textData.distanceTextGridMinCol}">${name}</svg:text>""" + "\n"
        curDistance += distance
    }
    lines += """</svg:g>"""
    return lines
}