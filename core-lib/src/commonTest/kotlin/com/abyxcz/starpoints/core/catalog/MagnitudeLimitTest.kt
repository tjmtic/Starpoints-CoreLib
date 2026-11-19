package com.abyxcz.starpoints.core.catalog

import kotlin.test.Test
import kotlin.test.assertEquals

class MagnitudeLimitTest {
    @Test
    fun wideFieldOfViewIsSix() {
        assertEquals(6.0, magnitudeLimitFor(90.0), 1e-9)
        assertEquals(6.0, magnitudeLimitFor(60.0), 1e-9)
    }

    @Test
    fun narrowFieldOfViewIsEight() {
        assertEquals(8.0, magnitudeLimitFor(15.0), 1e-9)
        assertEquals(8.0, magnitudeLimitFor(5.0), 1e-9)
    }

    @Test
    fun midFieldOfViewIsSeven() {
        assertEquals(7.0, magnitudeLimitFor(37.5), 1e-9)
    }
}
