package com.abyxcz.starpoints.core.presenter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOLERANCE = 1e-9

class SkySettingsTest {
    @Test
    fun effectiveLimitAtMidFovIsSeven() {
        assertEquals(7.0, effectiveMagnitudeLimit(37.5, SkySettings()), TOLERANCE)
    }

    @Test
    fun effectiveLimitWithPositiveOffsetIsEight() {
        val settings = SkySettings().withMagnitudeOffset(1.0)
        assertEquals(8.0, effectiveMagnitudeLimit(37.5, settings), TOLERANCE)
    }

    @Test
    fun effectiveLimitClampedAtEight() {
        val settings = SkySettings().withMagnitudeOffset(2.0)
        assertEquals(8.0, effectiveMagnitudeLimit(37.5, settings), TOLERANCE)
    }

    @Test
    fun effectiveLimitAtWideFovWithNegativeOffsetIsFour() {
        val settings = SkySettings().withMagnitudeOffset(-2.0)
        assertEquals(4.0, effectiveMagnitudeLimit(90.0, settings), TOLERANCE)
    }

    @Test
    fun withMagnitudeOffsetClampsToMinusTwo() {
        val settings = SkySettings().withMagnitudeOffset(-5.0)
        assertEquals(-2.0, settings.magnitudeOffset, TOLERANCE)
    }

    @Test
    fun settingsRoundTripThroughJson() {
        val settings = SkySettings(showConstellationLines = false, magnitudeOffset = 1.5)
        val decoded = decodeSettings(encodeSettings(settings))
        assertEquals(settings, decoded)
    }

    @Test
    fun malformedJsonGivesDefaults() {
        assertEquals(SkySettings(), decodeSettings("not json"))
    }

    @Test
    fun skyTimeOffsetClampsToSevenTwenty() {
        assertEquals(720, SkyTime().withOffset(800).offsetMinutes)
    }

    @Test
    fun skyTimeEpochMillisAddsOffset() {
        assertEquals(5_401_000L, SkyTime(90).epochMillis(1_000))
    }

    @Test
    fun skyTimeIsLiveWhenOffsetIsZero() {
        assertTrue(SkyTime().isLive)
    }
}
