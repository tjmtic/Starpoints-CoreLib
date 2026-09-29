package com.abyxcz.starpoints.core.presenter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOLERANCE = 1e-9

class StarStyleTest {
    @Test
    fun radiusByMagnitude() {
        assertEquals(3.5, starRadiusPx(0.0), TOLERANCE)
        assertEquals(0.8, starRadiusPx(6.0), TOLERANCE)
        assertEquals(4.25, starRadiusPx(-1.5), TOLERANCE)
    }

    @Test
    fun colorAtZeroColorIndex() {
        assertEquals(0xFFF8F7FF.toInt(), starColor(0.0, nightMode = false))
    }

    @Test
    fun colorClampsBelowStops() {
        assertEquals(0xFF9BB0FF.toInt(), starColor(-1.0, nightMode = false))
    }

    @Test
    fun colorInterpolatesBetweenStops() {
        assertEquals(0xFFFAF6FA.toInt(), starColor(0.15, nightMode = false))
    }

    @Test
    fun nightModeKeepsOnlyRed() {
        val color = starColor(0.0, nightMode = true)
        assertEquals(0xFFF80000.toInt(), color)
    }

    @Test
    fun nightModeRedAtLeastNinetyAndGreenBlueZero() {
        for (bv in listOf(-0.4, 0.6, 2.0)) {
            val color = starColor(bv, nightMode = true)
            val red = color shr 16 and 0xFF
            val green = color shr 8 and 0xFF
            val blue = color and 0xFF
            assertTrue(red >= 90, "expected red >= 90 for B−V $bv, got $red")
            assertEquals(0, green, "expected green 0 for B−V $bv")
            assertEquals(0, blue, "expected blue 0 for B−V $bv")
        }
    }
}
