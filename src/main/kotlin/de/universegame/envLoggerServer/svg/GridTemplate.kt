package de.universegame.env_logger_server.svg

fun grid_Template(
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
    numRows: Int,
    numSubRowsPerRow: Int,
    numColumns: Int,
    numSubColsPerCol: Int,
    style: String = "stroke:black",
    majStyle: String = "stroke-width:2",
    minStyle: String = "stroke-width:0.3",
): String {
    return """
        <svg:g id="grid_temp" fill="none" style="${style}">
            <svg:g id="maj_lines" style="${majStyle}">
                <svg:g id="maj_vert_lines">
                    ${maj_vert_lines(startX, startY, endX, endY, numColumns)}
                </svg:g>

                <svg:g id="major_hor_lines">
                    ${maj_hor_lines(startX, startY, endX, endY, numRows)}
                </svg:g>
            </svg:g>
            <svg:g id="min_lines" style="${minStyle}">
                <svg:g id="min_hor_lines">
                    ${min_hor_lines(startX, startY, endX, endY, numRows * numSubRowsPerRow)}
                </svg:g>
                <svg:g id="min_vert_lines">
                    ${min_vert_lines(startX, startY, endX, endY, numColumns * numSubColsPerCol)}
                </svg:g>
            </svg:g>
        </svg:g>
    """.trimIndent()
}

private fun min_hor_lines(
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
    numRows: Int
): String {
    var lines = ""
    val distance = (endY - startY) / numRows
    var curDist = startY
    for (i in 0..numRows) {
        lines += """<svg:line x1="${startX}" y1="${curDist}" x2="${endX}" y2="${curDist}"/>""" + "\n"
        curDist += distance
    }
    return lines
}

private fun maj_hor_lines(
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
    numRows: Int
): String {
    var lines = ""
    val distance = (endY - startY) / numRows
    var curDist = startY
    for (i in 0..numRows) {
        lines += """<svg:line x1="${startX}" y1="${curDist}" x2="${endX}" y2="${curDist}"/>""" + "\n"
        curDist += distance
    }
    return lines
}

private fun min_vert_lines(
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
    numColumns: Int
): String {
    var lines = ""
    val distance = (endX - startX) / numColumns
    var curDist = startX
    for (i in 0..numColumns) {
        lines += """<svg:line x1="${curDist}" y1="${startY}" x2="${curDist}" y2="${endY}"/>""" + "\n"
        curDist += distance
    }
    return lines
}

private fun maj_vert_lines(
    startX: Double,
    startY: Double,
    endX: Double,
    endY: Double,
    numColumns: Int
): String {
    var lines = ""
    val distance = (endX - startX) / numColumns
    var curDist = startX
    for (i in 0..numColumns) {
        lines += """<svg:line x1="${curDist}" y1="${startY}" x2="${curDist}" y2="${endY}"/>""" + "\n"
        curDist += distance
    }
    return lines
}