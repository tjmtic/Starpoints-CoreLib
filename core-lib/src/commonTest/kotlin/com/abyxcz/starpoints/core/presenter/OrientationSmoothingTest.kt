package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrientationSmoothingTest {
    private fun look(azimuth: Double, altitude: Double) = LookDirection(azimuth, altitude, 0.0)

    @Test
    fun alphaOneReturnsNext() {
        val next = look(10.0, 20.0)
        assertEquals(next, smooth(look(300.0, -5.0), next, 1.0))
    }

    @Test
    fun acrossNorth() {
        val az = smooth(look(350.0, 0.0), look(10.0, 0.0), 0.5).azimuthDegrees
        val onCircle = minOf(abs(az), abs(360.0 - az))
        assertTrue(onCircle < 1e-6, "azimuth $az")
    }

    @Test
    fun halfway() {
        assertEquals(45.0, smooth(look(0.0, 0.0), look(90.0, 0.0), 0.5).azimuthDegrees, 1e-6)
        assertEquals(20.0, smooth(look(120.0, 10.0), look(120.0, 30.0), 0.5).altitudeDegrees, 1e-6)
    }

    @Test
    fun noPreviousReturnsNext() {
        val next = look(10.0, 20.0)
        assertEquals(next, smooth(null, next, 0.2))
    }
}
