package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.test.Test
import kotlin.test.assertEquals

class ArAlignmentTest {
    @Test
    fun fieldOfViewAtZoom() {
        assertEquals(32.2042, fieldOfViewAtZoom(60.0, 2.0), 1e-3)
        assertEquals(60.0, fieldOfViewAtZoom(60.0, 1.0), 1e-3)
        assertEquals(18.734, fieldOfViewAtZoom(60.0, 3.5), 1e-3)
    }

    @Test
    fun nudgeShiftsAzimuth() {
        val offset = AlignmentOffset().nudge(100.0, 0.0, 1000.0, 60.0)
        assertEquals(-6.0, offset.azimuthDegrees, 1e-9)
        assertEquals(0.0, offset.altitudeDegrees, 1e-9)
    }

    @Test
    fun nudgeClamps() {
        val az = AlignmentOffset().nudge(10000.0, 0.0, 1000.0, 60.0)
        assertEquals(-30.0, az.azimuthDegrees, 1e-9)
        val alt = AlignmentOffset().nudge(0.0, 10000.0, 1000.0, 60.0)
        assertEquals(15.0, alt.altitudeDegrees, 1e-9)
    }

    @Test
    fun alignedNormalizesAzimuth() {
        val look = LookDirection(355.0, 10.0, 0.0).aligned(AlignmentOffset(10.0, 0.0))
        assertEquals(5.0, look.azimuthDegrees, 1e-9)
        assertEquals(10.0, look.altitudeDegrees, 1e-9)
    }
}
