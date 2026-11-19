package com.abyxcz.starpoints.core.math

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Angular separation between two sky positions in degrees (Meeus ch. 17, haversine form).
 *
 * The haversine form is preferred over the plain cosine form because it remains accurate for small
 * separations.
 *
 * @param ra1 right ascension of the first point in degrees.
 * @param dec1 declination of the first point in degrees.
 * @param ra2 right ascension of the second point in degrees.
 * @param dec2 declination of the second point in degrees.
 * @return angular separation in degrees, in [0, 180].
 */
fun angularSeparationDegrees(
    ra1: Double,
    dec1: Double,
    ra2: Double,
    dec2: Double,
): Double {
    val deltaRa = (ra2 - ra1).toRadians()
    val deltaDec = (dec2 - dec1).toRadians()
    val d1 = dec1.toRadians()
    val d2 = dec2.toRadians()

    val h =
        sin(deltaDec / 2.0) * sin(deltaDec / 2.0) +
            cos(d1) * cos(d2) * sin(deltaRa / 2.0) * sin(deltaRa / 2.0)
    val clamped = h.coerceIn(0.0, 1.0)
    return (2.0 * asin(sqrt(clamped))).toDegrees()
}
