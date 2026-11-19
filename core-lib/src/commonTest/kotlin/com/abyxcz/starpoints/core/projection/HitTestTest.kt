package com.abyxcz.starpoints.core.projection

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HitTestTest {
    @Test
    fun brighterStarWinsNearTie() {
        val vega = ProjectedStar(1, 30.0, 0.0, 0.03)
        val faint = ProjectedStar(2, 10.0, 0.0, 6.0)
        val picked = pickStar(listOf(vega, faint), tapX = 0.0, tapY = 0.0, radiusPx = 40.0)
        assertEquals(1, picked?.id)
    }

    @Test
    fun outOfRangeReturnsNull() {
        val star = ProjectedStar(1, 50.0, 0.0, 0.0)
        val picked = pickStar(listOf(star), tapX = 0.0, tapY = 0.0, radiusPx = 40.0)
        assertNull(picked)
    }

    @Test
    fun equalMagnitudePicksNearer() {
        val near = ProjectedStar(1, 10.0, 0.0, 3.0)
        val far = ProjectedStar(2, 30.0, 0.0, 3.0)
        val picked = pickStar(listOf(far, near), tapX = 0.0, tapY = 0.0, radiusPx = 40.0)
        assertEquals(1, picked?.id)
    }

    @Test
    fun emptyListReturnsNull() {
        val picked = pickStar(emptyList(), tapX = 0.0, tapY = 0.0, radiusPx = 40.0)
        assertNull(picked)
    }
}
