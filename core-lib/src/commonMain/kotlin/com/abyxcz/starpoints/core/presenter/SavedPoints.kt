package com.abyxcz.starpoints.core.presenter

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val SAVED_POINTS_JSON = Json { ignoreUnknownKeys = true }

/**
 * A saved sky position.
 *
 * @property id stable identifier.
 * @property name user-visible name.
 * @property rightAscensionDegrees right ascension in degrees.
 * @property declinationDegrees declination in degrees.
 * @property savedAtEpochMillis when the point was saved.
 * @property starId optional catalog identifier of the underlying star.
 */
@Serializable
data class SavedPoint(
    val id: String,
    val name: String,
    val rightAscensionDegrees: Double,
    val declinationDegrees: Double,
    val savedAtEpochMillis: Long,
    val starId: Int? = null,
)

/**
 * Replaces any point with the same [SavedPoint.id] and returns the list ordered newest first by
 * [SavedPoint.savedAtEpochMillis].
 */
fun List<SavedPoint>.upsert(point: SavedPoint): List<SavedPoint> {
    val merged = filter { it.id != point.id } + point
    return merged.sortedByDescending { it.savedAtEpochMillis }
}

/** Returns the list without the point whose id equals [id]. */
fun List<SavedPoint>.remove(id: String): List<SavedPoint> = filter { it.id != id }

/** Encodes [points] to JSON. */
fun encodeSavedPoints(points: List<SavedPoint>): String = SAVED_POINTS_JSON.encodeToString(points)

/**
 * Decodes [json] to a list of saved points.
 *
 * Returns an empty list for malformed JSON so a corrupt file never crashes the app.
 */
fun decodeSavedPoints(json: String): List<SavedPoint> {
    return runCatching { SAVED_POINTS_JSON.decodeFromString<List<SavedPoint>>(json) }
        .getOrDefault(emptyList())
}
