package com.abyxcz.starpoints.core.math

import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

class AnglesTest {
    @Test
    fun convertsBetweenDegreesAndRadians() {
        assertEquals(PI, 180.0.toRadians(), 1e-12)
        assertEquals(90.0, (PI / 2).toDegrees(), 1e-12)
    }

    @Test
    fun normalizesIntoOneTurn() {
        assertEquals(10.0, 370.0.normalizeDegrees(), 1e-12)
        assertEquals(350.0, (-10.0).normalizeDegrees(), 1e-12)
        assertEquals(0.0, 720.0.normalizeDegrees(), 1e-12)
    }

    @Test
    fun convertsHoursAndDegrees() {
        assertEquals(180.0, 12.0.hoursToDegrees(), 1e-12)
        assertEquals(6.0, 90.0.degreesToHours(), 1e-12)
    }
}
