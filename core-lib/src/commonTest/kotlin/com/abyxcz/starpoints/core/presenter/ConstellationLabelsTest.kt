package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.Constellation
import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOLERANCE = 0.5
private const val EPOCH_MILLIS = 1_790_542_800_000L
private const val WIDTH_PX = 1000.0
private const val HEIGHT_PX = 500.0

class ConstellationLabelsTest {
    private val observer = Observer(51.4769, 0.0)
    private val screen = ScreenSize(WIDTH_PX, HEIGHT_PX)
    private val vegaLook =
        LookDirection(azimuthDegrees = 263.4886, altitudeDegrees = 58.0508, rollDegrees = 0.0)

    @Test
    fun vegaProjectsToCenter() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val constellations = listOf(Constellation("Lyr", "Lyra", 279.23473, 38.78369))
        val labels =
            visibleConstellationLabels(constellations, view, observer, EPOCH_MILLIS, screen)
        assertEquals(1, labels.size)
        assertEquals(500.0, labels[0].x, TOLERANCE)
        assertEquals(250.0, labels[0].y, TOLERANCE)
    }

    @Test
    fun belowHorizonConstellationsAreAbsent() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val constellations = listOf(Constellation("Ori", "Orion", 88.79294, 7.40706))
        val labels =
            visibleConstellationLabels(constellations, view, observer, EPOCH_MILLIS, screen)
        assertTrue(labels.isEmpty(), "Orion should be absent")
    }

    @Test
    fun twoSerpensEntriesGiveTwoLabels() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val constellations =
            listOf(
                Constellation("Serp1", "Serpens", 279.23473, 38.78369),
                Constellation("Serp2", "Serpens", 279.23473, 38.78369),
            )
        val labels =
            visibleConstellationLabels(constellations, view, observer, EPOCH_MILLIS, screen)
        assertEquals(2, labels.size)
    }
}
