package com.abyxcz.starpoints.core.math

/** Greenwich mean sidereal time in degrees (Meeus eq. 12.4), normalized into [0, 360). */
fun greenwichMeanSiderealTimeDegrees(julianDate: Double): Double {
    val t = julianCenturiesSinceJ2000(julianDate)
    val gmst =
        280.46061837 + 360.98564736629 * (julianDate - 2451545.0) + 0.000387933 * t * t -
            t * t * t / 38_710_000.0
    return gmst.normalizeDegrees()
}

/** Local sidereal time in degrees: GMST plus east longitude, normalized into [0, 360). */
fun localSiderealTimeDegrees(julianDate: Double, longitudeEastDegrees: Double): Double {
    return (greenwichMeanSiderealTimeDegrees(julianDate) + longitudeEastDegrees).normalizeDegrees()
}
