package com.abyxcz.starpoints.core.math

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SiderealTimeTest {
    @Test
    fun meeusExample12a() {
        assertEquals(197.693195, greenwichMeanSiderealTimeDegrees(2446895.5), 1e-5)
    }

    @Test
    fun meeusExample12b() {
        assertEquals(128.7378733, greenwichMeanSiderealTimeDegrees(2446896.30625), 1e-5)
    }

    @Test
    fun localSiderealTimeWestLongitude() {
        assertEquals(120.6276394, localSiderealTimeDegrees(2446895.5, -77.0655556), 1e-5)
    }

    @Test
    fun localSiderealTimeNormalizesLargeLongitude() {
        val lst = localSiderealTimeDegrees(2446895.5, 200.0)
        assertTrue(lst in 0.0..360.0, "expected [0, 360), got $lst")
    }
}
