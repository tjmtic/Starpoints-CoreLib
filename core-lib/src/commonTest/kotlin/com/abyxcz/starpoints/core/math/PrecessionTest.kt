package com.abyxcz.starpoints.core.math

import kotlin.test.Test
import kotlin.test.assertEquals

class PrecessionTest {
    @Test
    fun meeusExample21bThetaPersei() {
        val result = precessFromJ2000(Equatorial(41.054063, 49.227750), 2462088.69)
        assertEquals(41.547214, result.rightAscensionDegrees, 1e-4)
        assertEquals(49.348483, result.declinationDegrees, 1e-4)
    }

    @Test
    fun j2000EpochIsUnchanged() {
        val result = precessFromJ2000(Equatorial(41.054063, 49.227750), 2451545.0)
        assertEquals(41.054063, result.rightAscensionDegrees, 1e-12)
        assertEquals(49.227750, result.declinationDegrees, 1e-12)
    }
}
