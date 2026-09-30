package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.ports.DeviceAttitude
import com.abyxcz.starpoints.core.ports.HeadingAccuracy
import com.abyxcz.starpoints.core.projection.project
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ArOrientationTest {
    private fun attitude(r: List<Double>, declination: Double = 0.0) =
        DeviceAttitude(r, HeadingAccuracy.HIGH, declination, 0L)

    private val upright = listOf(1.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 1.0, 0.0)
    private val rolled =
        listOf(
            -0.639739,
            -0.295765,
            -0.709406,
            -0.71571,
            0.565691,
            0.409576,
            0.280166,
            0.769751,
            -0.573576,
        )

    @Test
    fun phoneUprightFacingNorth() {
        val look = lookDirectionFrom(attitude(upright))
        assertEquals(0.0, look.azimuthDegrees, 1e-9)
        assertEquals(0.0, look.altitudeDegrees, 1e-9)
        assertEquals(0.0, look.rollDegrees, 1e-9)
    }

    @Test
    fun declinationTurnsTheAzimuth() {
        assertEquals(10.0, lookDirectionFrom(attitude(upright, 10.0)).azimuthDegrees, 1e-9)
    }

    @Test
    fun facingAzimuth120Altitude35Rolled20() {
        val look = lookDirectionFrom(attitude(rolled))
        assertEquals(120.0, look.azimuthDegrees, 1e-3)
        assertEquals(35.0, look.altitudeDegrees, 1e-3)
        val a = assertNotNull(project(Horizontal(38.0, 125.0), look, 60.0, 1000.0, 500.0))
        assertEquals(572.179, a.x, 0.01)
        assertEquals(226.261, a.y, 0.01)
        val b = assertNotNull(project(Horizontal(30.0, 110.0), look, 60.0, 1000.0, 500.0))
        assertEquals(351.878, b.x, 0.01)
        assertEquals(270.542, b.y, 0.01)
    }
}
