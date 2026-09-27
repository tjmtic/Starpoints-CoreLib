package com.abyxcz.starpoints.core.math

import kotlin.test.Test
import kotlin.test.assertEquals

class SeparationTest {
    @Test
    fun meeusExample17aArcturusToSpica() {
        val separation =
            angularSeparationDegrees(
                ra1 = 213.9154,
                dec1 = 19.1825,
                ra2 = 201.2983,
                dec2 = -11.1614,
            )
        assertEquals(32.7930, separation, 1e-4)
    }

    @Test
    fun samePointIsZero() {
        val separation = angularSeparationDegrees(45.0, 30.0, 45.0, 30.0)
        assertEquals(0.0, separation, 1e-12)
    }

    @Test
    fun celestialPolesAreOneHundredEighty() {
        val separation = angularSeparationDegrees(0.0, 90.0, 0.0, -90.0)
        assertEquals(180.0, separation, 1e-9)
    }

    @Test
    fun smallDeclinationDifference() {
        val separation = angularSeparationDegrees(100.0, 20.0, 100.0, 20.001)
        assertEquals(0.001, separation, 1e-9)
    }
}
