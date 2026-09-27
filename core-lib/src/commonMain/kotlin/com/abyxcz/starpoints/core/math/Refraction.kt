package com.abyxcz.starpoints.core.math

import kotlin.math.tan

/**
 * Atmospheric refraction in degrees (Saemundsson's formula, Meeus eq. 16.4).
 *
 * @param trueAltitudeDegrees true (geometric) altitude above the horizon in degrees.
 * @return refraction in degrees; 0.0 below -1°.
 */
fun refractionDegrees(trueAltitudeDegrees: Double): Double {
    if (trueAltitudeDegrees < -1.0) return 0.0
    val h = trueAltitudeDegrees
    val rArcmin = 1.02 / tan((h + 10.3 / (h + 5.11)).toRadians())
    return rArcmin / 60.0
}

/**
 * Apparent altitude in degrees: true altitude plus atmospheric refraction.
 *
 * @param trueAltitudeDegrees true (geometric) altitude above the horizon in degrees.
 * @return apparent altitude in degrees.
 */
fun apparentAltitudeDegrees(trueAltitudeDegrees: Double): Double {
    return trueAltitudeDegrees + refractionDegrees(trueAltitudeDegrees)
}
