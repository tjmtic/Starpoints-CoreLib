package com.abyxcz.starpoints.core.math

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeTest {
    @Test
    fun j2000Epoch() {
        val jd = julianDate(946_728_000_000L)
        assertEquals(2451545.0, jd, 1e-9)
        assertEquals(0.0, julianCenturiesSinceJ2000(jd), 1e-9)
    }

    @Test
    fun meeusExample7a() {
        val jd = julianDate(-386_310_816_000L)
        assertEquals(2436116.31, jd, 1e-6)
    }

    @Test
    fun unixEpoch() {
        assertEquals(2440587.5, julianDate(0L), 1e-9)
    }
}
