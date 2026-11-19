package com.abyxcz.starpoints.core.math

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Horizontal coordinates of a celestial object.
 *
 * @property altitudeDegrees altitude above the horizon in degrees.
 * @property azimuthDegrees azimuth measured from NORTH, eastward, in [0, 360).
 */
data class Horizontal(val altitudeDegrees: Double, val azimuthDegrees: Double)

/**
 * Converts equatorial coordinates to horizontal coordinates (Meeus ch. 13).
 *
 * @param rightAscensionDegrees right ascension α in degrees.
 * @param declinationDegrees declination δ in degrees.
 * @param latitudeDegrees observer latitude φ in degrees.
 * @param localSiderealTimeDegrees local sidereal time LST in degrees.
 * @return [Horizontal] altitude and azimuth (from north, eastward).
 */
fun equatorialToHorizontal(
    rightAscensionDegrees: Double,
    declinationDegrees: Double,
    latitudeDegrees: Double,
    localSiderealTimeDegrees: Double,
): Horizontal {
    val alpha = rightAscensionDegrees.toRadians()
    val delta = declinationDegrees.toRadians()
    val phi = latitudeDegrees.toRadians()
    val lst = localSiderealTimeDegrees.toRadians()

    val hourAngle = lst - alpha

    val sinAltitude = sin(phi) * sin(delta) + cos(phi) * cos(delta) * cos(hourAngle)
    val altitude = asin(sinAltitude)

    val azimuth =
        atan2(
            -cos(delta) * sin(hourAngle),
            sin(delta) * cos(phi) - cos(delta) * sin(phi) * cos(hourAngle),
        )

    return Horizontal(altitude.toDegrees(), azimuth.toDegrees().normalizeDegrees())
}
