package com.abyxcz.starpoints.core.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RefractionTest {
    @Test
    fun refractionAtHorizon() {
        assertEquals(0.48303, refractionDegrees(0.0), 1e-5)
    }

    @Test
    fun refractionAtTenDegrees() {
        assertEquals(0.090128, refractionDegrees(10.0), 1e-5)
    }

    @Test
    fun refractionAtFortyFiveDegrees() {
        assertEquals(0.016878, refractionDegrees(45.0), 1e-5)
    }

    @Test
    fun refractionBelowMinusOneIsZero() {
        assertEquals(0.0, refractionDegrees(-5.0), 1e-12)
    }

    @Test
    fun refractionDecreasesWithAltitude() {
        val r0 = refractionDegrees(0.0)
        val r10 = refractionDegrees(10.0)
        val r45 = refractionDegrees(45.0)
        val r80 = refractionDegrees(80.0)
        assertTrue(r0 > r10, "expected r(0) > r(10), got $r0 vs $r10")
        assertTrue(r10 > r45, "expected r(10) > r(45), got $r10 vs $r45")
        assertTrue(r45 > r80, "expected r(45) > r(80), got $r45 vs $r80")
    }

    @Test
    fun apparentAltitudeIsTruePlusRefraction() {
        val trueAlt = 10.0
        val expected = trueAlt + refractionDegrees(trueAlt)
        assertEquals(expected, apparentAltitudeDegrees(trueAlt), 1e-12)
    }
}
