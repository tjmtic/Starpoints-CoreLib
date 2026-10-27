package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.DeepSkyKind
import com.abyxcz.starpoints.core.catalog.DeepSkyObject
import com.abyxcz.starpoints.core.math.Equatorial
import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOLERANCE = 0.5
private const val EPOCH_MILLIS = 1_790_542_800_000L
private const val WIDTH_PX = 1000.0
private const val HEIGHT_PX = 500.0

class VisibleDeepSkyTest {
    private val observer = Observer(51.4769, 0.0)
    private val screen = ScreenSize(WIDTH_PX, HEIGHT_PX)
    private val vegaLook =
        LookDirection(azimuthDegrees = 263.4886, altitudeDegrees = 58.0508, rollDegrees = 0.0)

    private fun atVega(size: List<Double>, kind: DeepSkyKind = DeepSkyKind.PLANETARY_NEBULA) =
        DeepSkyObject("M57", kind, Equatorial(279.23473, 38.78369), 8.8, size)

    @Test
    fun anObjectAtTheLookPointProjectsToTheCentre() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val points =
            visibleDeepSky(listOf(atVega(listOf(1.4))), view, observer, EPOCH_MILLIS, screen)
        assertEquals(1, points.size)
        assertEquals("M57", points[0].id)
        assertEquals(DeepSkySymbol.PLANETARY_NEBULA, points[0].symbol)
        assertEquals(500.0, points[0].x, TOLERANCE)
        assertEquals(250.0, points[0].y, TOLERANCE)
    }

    @Test
    fun belowTheHorizonIsAbsent() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val orion = DeepSkyObject("M42", DeepSkyKind.EMISSION_NEBULA, Equatorial(83.82, -5.39), 4.0)
        assertTrue(visibleDeepSky(listOf(orion), view, observer, EPOCH_MILLIS, screen).isEmpty())
    }

    @Test
    fun theRadiusIsHalfTheLargestSizeAtTheViewsScale() {
        // A 90° view 1000 px wide: the focal length is 500 px, 8.727 px per degree at the centre.
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 90.0)
        val wide = atVega(listOf(600.0, 120.0))
        val point = visibleDeepSky(listOf(wide), view, observer, EPOCH_MILLIS, screen).single()
        assertEquals(43.63, point.radiusPx, 0.01)
    }

    @Test
    fun theRadiusIsClamped() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 90.0)
        val tiny = visibleDeepSky(listOf(atVega(emptyList())), view, observer, EPOCH_MILLIS, screen)
        assertEquals(6.0, tiny.single().radiusPx)
        val narrow = SkyView(look = vegaLook, fieldOfViewDegrees = 5.0)
        val huge =
            visibleDeepSky(listOf(atVega(listOf(190.0))), narrow, observer, EPOCH_MILLIS, screen)
        assertEquals(120.0, huge.single().radiusPx)
    }

    @Test
    fun everyKindHasASymbol() {
        assertEquals(DeepSkySymbol.GALAXY, DeepSkyKind.SPIRAL_GALAXY.symbol)
        assertEquals(DeepSkySymbol.OPEN_CLUSTER, DeepSkyKind.STAR_GROUPING.symbol)
        assertEquals(DeepSkySymbol.GLOBULAR_CLUSTER, DeepSkyKind.GLOBULAR_CLUSTER.symbol)
        assertEquals(DeepSkySymbol.NEBULA, DeepSkyKind.DARK_NEBULA.symbol)
        assertEquals(DeepSkySymbol.PLANETARY_NEBULA, DeepSkyKind.SUPERNOVA_REMNANT.symbol)
    }
}
