package com.abyxcz.starpoints.core.math

/** Julian date of a UTC instant given as epoch milliseconds. */
fun julianDate(epochMillis: Long): Double = epochMillis / 86_400_000.0 + 2_440_587.5

/** Julian centuries since J2000.0 (Meeus ch. 12, T). */
fun julianCenturiesSinceJ2000(julianDate: Double): Double = (julianDate - 2_451_545.0) / 36_525.0
