package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.StarRecord
import com.abyxcz.starpoints.core.math.DEGREES_PER_HOUR
import com.abyxcz.starpoints.core.math.apparentAltitudeDegrees
import com.abyxcz.starpoints.core.math.equatorialToHorizontal
import com.abyxcz.starpoints.core.math.julianDate
import com.abyxcz.starpoints.core.math.localSiderealTimeDegrees
import com.abyxcz.starpoints.core.math.normalizeDegrees
import kotlin.math.abs
import kotlin.math.roundToLong

private const val TENTHS_PER_SECOND = 10L
private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L
private const val SECONDS_PER_HOUR = 3600L
private const val TENTHS_PER_DAY = 864_000L
private const val ARCSECONDS_PER_DEGREE = 3600L

private const val WHITE_FROM = 0.0
private const val YELLOW_WHITE_FROM = 0.3
private const val YELLOW_FROM = 0.6
private const val ORANGE_FROM = 0.8
private const val RED_FROM = 1.4

private fun pad2(n: Long): String = n.toString().padStart(2, '0')

/**
 * The computed facts behind a star's detail card.
 *
 * @property id catalog identifier.
 * @property name the star's name, if known.
 * @property magnitude apparent visual magnitude.
 * @property colorName the color name derived from the color index.
 * @property rightAscension right ascension formatted as hours, minutes, seconds.
 * @property declination declination formatted as degrees, arcminutes, arcseconds.
 * @property altitudeDegrees apparent altitude above the horizon in degrees.
 * @property azimuthDegrees azimuth measured from north, eastward, in degrees.
 * @property aboveHorizon whether the star is above the horizon.
 */
data class StarFacts(
    val id: Int,
    val name: String?,
    val magnitude: Double,
    val colorName: String,
    val rightAscension: String,
    val declination: String,
    val altitudeDegrees: Double,
    val azimuthDegrees: Double,
    val aboveHorizon: Boolean,
)

/** Right ascension as `18h 36m 56.3s`, rounded to tenths of a second first, 24h wrapping to 0h. */
fun formatRightAscension(degrees: Double): String {
    val tenths =
        (degrees.normalizeDegrees() / DEGREES_PER_HOUR * SECONDS_PER_HOUR * TENTHS_PER_SECOND)
            .roundToLong() % TENTHS_PER_DAY
    val seconds = tenths / TENTHS_PER_SECOND
    val hours = seconds / SECONDS_PER_HOUR
    val minutes = seconds / SECONDS_PER_MINUTE % MINUTES_PER_HOUR
    return "${hours}h ${pad2(minutes)}m ${pad2(seconds % SECONDS_PER_MINUTE)}.${tenths % TENTHS_PER_SECOND}s"
}

/** Declination as `+38° 47′ 01″`, rounded to whole arcseconds first; the sign always shown. */
fun formatDeclination(degrees: Double): String {
    val sign = if (degrees < 0.0) "-" else "+"
    val arcseconds = (abs(degrees) * ARCSECONDS_PER_DEGREE).roundToLong()
    val arcminutes = arcseconds / SECONDS_PER_MINUTE % MINUTES_PER_HOUR
    return "$sign${arcseconds / ARCSECONDS_PER_DEGREE}° ${pad2(arcminutes)}′ ${pad2(arcseconds % SECONDS_PER_MINUTE)}″"
}

/** The color name for a color index (B−V). */
fun colorName(colorIndex: Double): String =
    when {
        colorIndex < WHITE_FROM -> "blue-white"
        colorIndex < YELLOW_WHITE_FROM -> "white"
        colorIndex < YELLOW_FROM -> "yellow-white"
        colorIndex < ORANGE_FROM -> "yellow"
        colorIndex < RED_FROM -> "orange"
        else -> "red"
    }

/**
 * Computes the star facts for a catalog record at a given instant.
 *
 * @param star the catalog record.
 * @param names the star name map.
 * @param observer the observer's position.
 * @param epochMillis the observation instant in epoch milliseconds.
 * @return the computed [StarFacts].
 */
fun starFacts(
    star: StarRecord,
    names: Map<Int, String>,
    observer: Observer,
    epochMillis: Long,
): StarFacts {
    val jd = julianDate(epochMillis)
    val lst = localSiderealTimeDegrees(jd, observer.longitudeEastDegrees)
    val horizontal =
        equatorialToHorizontal(
            star.rightAscensionDegrees.toDouble(),
            star.declinationDegrees.toDouble(),
            observer.latitudeDegrees,
            lst,
        )
    val altitude = apparentAltitudeDegrees(horizontal.altitudeDegrees)
    return StarFacts(
        id = star.id,
        name = names[star.id],
        magnitude = star.magnitude.toDouble(),
        colorName = colorName(star.colorIndex.toDouble()),
        rightAscension = formatRightAscension(star.rightAscensionDegrees.toDouble()),
        declination = formatDeclination(star.declinationDegrees.toDouble()),
        altitudeDegrees = altitude,
        azimuthDegrees = horizontal.azimuthDegrees,
        aboveHorizon = altitude > 0.0,
    )
}
