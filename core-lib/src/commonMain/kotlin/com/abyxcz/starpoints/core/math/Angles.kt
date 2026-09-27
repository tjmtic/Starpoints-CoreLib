package com.abyxcz.starpoints.core.math

import kotlin.math.PI

/** Degrees in a half turn (π radians). */
const val DEGREES_PER_HALF_TURN = 180.0

/** Degrees in a full turn. */
const val DEGREES_PER_TURN = 360.0

/** Degrees per hour of right ascension or sidereal time: 360° / 24h. */
const val DEGREES_PER_HOUR = 15.0

/** Degrees to radians. */
fun Double.toRadians(): Double = this * PI / DEGREES_PER_HALF_TURN

/** Radians to degrees. */
fun Double.toDegrees(): Double = this * DEGREES_PER_HALF_TURN / PI

/** This angle in degrees, wrapped into [0, 360). */
fun Double.normalizeDegrees(): Double {
    val wrapped = this % DEGREES_PER_TURN
    return if (wrapped < 0.0) wrapped + DEGREES_PER_TURN else wrapped
}

/** Hours (right ascension, sidereal time) to degrees: 1h = 15°. */
fun Double.hoursToDegrees(): Double = this * DEGREES_PER_HOUR

/** Degrees to hours: 15° = 1h. */
fun Double.degreesToHours(): Double = this / DEGREES_PER_HOUR
