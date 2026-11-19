package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.StarRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StarFactsTest {
    @Test
    fun formatRightAscension() {
        assertEquals("18h 36m 56.3s", formatRightAscension(279.23473))
        assertEquals("0h 00m 00.0s", formatRightAscension(0.0))
        assertEquals("0h 00m 00.0s", formatRightAscension(359.9999))
        assertEquals("6h 45m 08.9s", formatRightAscension(101.28716))
    }

    @Test
    fun formatDeclination() {
        assertEquals("+38° 47′ 01″", formatDeclination(38.78369))
        assertEquals("-16° 42′ 58″", formatDeclination(-16.71612))
        assertEquals("+0° 00′ 00″", formatDeclination(0.0))
        assertEquals("-0° 30′ 00″", formatDeclination(-0.5))
        assertEquals("+90° 00′ 00″", formatDeclination(89.99999))
    }

    @Test
    fun colorName() {
        assertEquals("blue-white", colorName(-0.03))
        assertEquals("white", colorName(0.0))
        assertEquals("orange", colorName(1.23))
        assertEquals("red", colorName(1.85))
    }

    @Test
    fun starFactsForVega() {
        val star = StarRecord(91262, 279.23473f, 38.78369f, 0.03f, 0.0f)
        val names = mapOf(91262 to "Vega")
        val observer = Observer(51.4769, 0.0)
        val facts = starFacts(star, names, observer, 1_790_542_800_000)

        assertEquals("Vega", facts.name)
        assertEquals("white", facts.colorName)
        assertEquals(58.0508, facts.altitudeDegrees, 1e-3)
        assertEquals(263.4886, facts.azimuthDegrees, 1e-3)
        assertTrue(facts.aboveHorizon)
    }
}
