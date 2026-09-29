package com.abyxcz.starpoints.core.math

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Converts horizontal coordinates to equatorial coordinates (Meeus ch. 13 inverted).
 *
 * @param altitudeDegrees altitude above the horizon in degrees.
 * @param azimuthDegrees azimuth measured from NORTH, eastward, in [0, 360).
 * @param latitudeDegrees observer latitude φ in degrees.
 * @param localSiderealTimeDegrees local sidereal time LST in degrees.
 * @return [Equatorial] right ascension and declination.
 */
fun horizontalToEquatorial(
    altitudeDegrees: Double,
    azimuthDegrees: Double,
    latitudeDegrees: Double,
    localSiderealTimeDegrees: Double,
): Equatorial {
    val h = altitudeDegrees.toRadians()
    val a = azimuthDegrees.toRadians()
    val phi = latitudeDegrees.toRadians()
    val lst = localSiderealTimeDegrees.toRadians()

    val sinDeclination = sin(phi) * sin(h) + cos(phi) * cos(h) * cos(a)
    val declination = asin(sinDeclination)

    val hourAngle = atan2(-sin(a) * cos(h), sin(h) * cos(phi) - cos(h) * sin(phi) * cos(a))

    val rightAscension = (lst - hourAngle).toDegrees().normalizeDegrees()

    return Equatorial(rightAscension, declination.toDegrees())
}
