package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.magnitudeLimitFor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val MIN_OFFSET = -2.0
private const val MAX_OFFSET = 2.0
private const val MIN_LIMIT = 2.0
private const val MAX_LIMIT = 8.0

/**
 * The reader's sky settings, kept between launches. [onDeviceWriting]: whether the on-device model
 * writes a Guide entry for an object no content pack covers — on by default, since the on-device
 * model is the app's showcase. Settings saved before a field existed read it as its default.
 */
@Serializable
data class SkySettings(
    val showConstellationLines: Boolean = true,
    val showStarNames: Boolean = true,
    val showConstellationNames: Boolean = true,
    val magnitudeOffset: Double = 0.0,
    val onDeviceWriting: Boolean = true,
)

fun SkySettings.withMagnitudeOffset(offset: Double): SkySettings =
    copy(magnitudeOffset = offset.coerceIn(MIN_OFFSET, MAX_OFFSET))

fun effectiveMagnitudeLimit(fieldOfViewDegrees: Double, settings: SkySettings): Double =
    (magnitudeLimitFor(fieldOfViewDegrees) + settings.magnitudeOffset).coerceIn(
        MIN_LIMIT,
        MAX_LIMIT,
    )

private val settingsJson = Json { ignoreUnknownKeys = true }

fun encodeSettings(settings: SkySettings): String = settingsJson.encodeToString(settings)

fun decodeSettings(json: String): SkySettings = runCatching {
    settingsJson.decodeFromString<SkySettings>(json)
}
    .getOrDefault(SkySettings())
