package com.abyxcz.starpoints.core.math

import kotlin.test.Test
import kotlin.test.assertEquals

class HorizontalTest {
    @Test
    fun meeusExample13bVenusFromWashington() {
        val result =
            equatorialToHorizontal(
                rightAscensionDegrees = 347.3193375,
                declinationDegrees = -6.7198917,
                latitudeDegrees = 38.9213889,
                localSiderealTimeDegrees = 51.6713319,
            )
        assertEquals(15.1250, result.altitudeDegrees, 1e-3)
        assertEquals(248.0336, result.azimuthDegrees, 1e-3)
    }

    @Test
    fun celestialPoleSeenFromLatitude40() {
        val result =
            equatorialToHorizontal(
                rightAscensionDegrees = 0.0,
                declinationDegrees = 90.0,
                latitudeDegrees = 40.0,
                localSiderealTimeDegrees = 0.0,
            )
        assertEquals(40.0, result.altitudeDegrees, 1e-6)
        assertEquals(0.0, result.azimuthDegrees, 1e-6)
    }
}
