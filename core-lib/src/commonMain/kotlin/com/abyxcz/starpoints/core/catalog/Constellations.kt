package com.abyxcz.starpoints.core.catalog

import com.abyxcz.starpoints.core.math.Equatorial
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A named constellation with a label position in J2000 degrees. */
data class Constellation(
    val id: String,
    val name: String,
    val labelRaDegrees: Double,
    val labelDecDegrees: Double,
)

/** The stick-figure lines for one constellation, as polylines of J2000 positions. */
data class ConstellationFigure(
    val id: String,
    val polylines: List<List<Equatorial>>,
)

private val CONSTELLATIONS_JSON = Json { ignoreUnknownKeys = true }

@Serializable
private data class ConstellationEntry(
    val id: String,
    val name: String,
    val labelRa: Double,
    val labelDec: Double,
)

/** Parses the constellation catalog, keeping the file's order and any duplicate ids. */
fun parseConstellations(json: String): List<Constellation> =
    CONSTELLATIONS_JSON.decodeFromString<List<ConstellationEntry>>(json).map {
        Constellation(it.id, it.name, it.labelRa, it.labelDec)
    }

/** Parses the constellation lines, giving one figure per id sorted by id. */
fun parseConstellationLines(json: String): List<ConstellationFigure> {
    val raw = CONSTELLATIONS_JSON.decodeFromString<Map<String, List<List<List<Double>>>>>(json)
    return raw.entries
        .sortedBy { it.key }
        .map { (id, lines) ->
            ConstellationFigure(
                id,
                lines.map { line -> line.map { (ra, dec) -> Equatorial(ra, dec) } },
            )
        }
}
