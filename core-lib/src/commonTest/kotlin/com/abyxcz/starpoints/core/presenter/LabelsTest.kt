package com.abyxcz.starpoints.core.presenter

import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOLERANCE = 1e-9

class LabelsTest {
    @Test
    fun limitIsOnePointFiveAtWideFov() {
        assertEquals(1.5, labelLimitFor(120.0), TOLERANCE)
        assertEquals(1.5, labelLimitFor(90.0), TOLERANCE)
    }

    @Test
    fun limitIsTwoPointTwoFiveAtMidFov() {
        assertEquals(2.25, labelLimitFor(55.0), TOLERANCE)
    }

    @Test
    fun limitIsThreeAtNarrowFov() {
        assertEquals(3.0, labelLimitFor(20.0), TOLERANCE)
        assertEquals(3.0, labelLimitFor(10.0), TOLERANCE)
    }

    @Test
    fun labelsNamedBrightStarsOnly() {
        val points =
            listOf(
                SkyPoint(1, 10.0, 20.0, 2.0, 0.0),
                SkyPoint(2, 30.0, 40.0, 2.5, 0.0),
                SkyPoint(3, 50.0, 60.0, 0.0, 0.0),
            )
        val names = mapOf(1 to "Alpha", 2 to "Beta")
        val labels = labelsFor(points, names, 55.0)
        assertEquals(1, labels.size)
        assertEquals(1, labels[0].id)
        assertEquals("Alpha", labels[0].name)
    }

    @Test
    fun labelCarriesPointCoordinates() {
        val points = listOf(SkyPoint(7, 123.0, 456.0, 1.0, 0.0))
        val labels = labelsFor(points, mapOf(7 to "Gamma"), 55.0)
        assertEquals(123.0, labels[0].x, TOLERANCE)
        assertEquals(456.0, labels[0].y, TOLERANCE)
    }
}
