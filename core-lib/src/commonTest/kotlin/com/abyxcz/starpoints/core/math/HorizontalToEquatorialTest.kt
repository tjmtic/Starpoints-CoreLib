package com.abyxcz.starpoints.core.math

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals

class HorizontalToEquatorialTest {

    @Test
    fun meeusExample13bReverse() {
        val result =
            horizontalToEquatorial(
                altitudeDegrees = 15.1250,
                azimuthDegrees = 248.0336,
                latitudeDegrees = 38.9213889,
                localSiderealTimeDegrees = 51.6713319,
            )
        assertEquals(347.3194, result.rightAscensionDegrees, 1e-3)
        assertEquals(-6.7199, result.declinationDegrees, 1e-3)
    }

    @Test
    fun roundTripEquatorialToHorizontal() {
        val latitude = 51.4769
        val lst = 100.0
        val cases = listOf(10.0 to 20.0, 200.0 to -45.0, 300.0 to 80.0)
        for ((ra, dec) in cases) {
            val horizontal =
                equatorialToHorizontal(
                    rightAscensionDegrees = ra,
                    declinationDegrees = dec,
                    latitudeDegrees = latitude,
                    localSiderealTimeDegrees = lst,
                )
            val back =
                horizontalToEquatorial(
                    altitudeDegrees = horizontal.altitudeDegrees,
                    azimuthDegrees = horizontal.azimuthDegrees,
                    latitudeDegrees = latitude,
                    localSiderealTimeDegrees = lst,
                )
            assertEquals(dec, back.declinationDegrees, 1e-9)
            val raDiff = abs(back.rightAscensionDegrees - ra)
            val circularDiff = minOf(raDiff, 360.0 - raDiff)
            assertEquals(0.0, circularDiff, 1e-9)
        }
    }
}
