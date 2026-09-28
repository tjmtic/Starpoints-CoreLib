package com.abyxcz.starpoints.core.math

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Equatorial coordinates in degrees. */
data class Equatorial(val rightAscensionDegrees: Double, val declinationDegrees: Double)

/** Precession angles (Meeus ch. 21) for a given Julian date, in degrees. */
internal data class PrecessionAngles(val zeta: Double, val z: Double, val theta: Double)

/**
 * Precesses an equatorial position from the J2000.0 epoch to the given Julian date (Meeus ch. 21,
 * rigorous method).
 *
 * @param position equatorial coordinates at J2000.0 in degrees.
 * @param julianDate target Julian date.
 * @return precessed [Equatorial] coordinates in degrees.
 */
fun precessFromJ2000(position: Equatorial, julianDate: Double): Equatorial {
    val t = julianCenturiesSinceJ2000(julianDate)

    val zeta = (2306.2181 * t + 0.30188 * t * t + 0.017998 * t * t * t) / 3600.0
    val z = (2306.2181 * t + 1.09468 * t * t + 0.018203 * t * t * t) / 3600.0
    val theta = (2004.3109 * t - 0.42665 * t * t - 0.041833 * t * t * t) / 3600.0

    val zetaRad = zeta.toRadians()
    val thetaRad = theta.toRadians()

    val alpha0 = position.rightAscensionDegrees.toRadians()
    val delta0 = position.declinationDegrees.toRadians()

    val a = cos(delta0) * sin(alpha0 + zetaRad)
    val b = cos(thetaRad) * cos(delta0) * cos(alpha0 + zetaRad) - sin(thetaRad) * sin(delta0)
    val c = sin(thetaRad) * cos(delta0) * cos(alpha0 + zetaRad) + cos(thetaRad) * sin(delta0)

    val alpha = (atan2(a, b).toDegrees() + z).normalizeDegrees()
    val delta = asin(c).toDegrees()

    return Equatorial(alpha, delta)
}
