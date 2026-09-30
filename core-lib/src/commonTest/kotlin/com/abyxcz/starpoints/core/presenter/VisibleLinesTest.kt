package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.ConstellationFigure
import com.abyxcz.starpoints.core.math.Equatorial
import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOLERANCE = 0.5
private const val EPOCH_MILLIS = 1_790_542_800_000L
private const val WIDTH_PX = 1000.0
private const val HEIGHT_PX = 500.0

class VisibleLinesTest {
    private val observer = Observer(51.4769, 0.0)
    private val screen = ScreenSize(WIDTH_PX, HEIGHT_PX)
    private val vegaLook =
        LookDirection(azimuthDegrees = 263.4886, altitudeDegrees = 58.0508, rollDegrees = 0.0)
    private val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)

    private fun figure(vararg points: Equatorial): List<ConstellationFigure> =
        listOf(ConstellationFigure("test", listOf(points.toList())))

    @Test
    fun vegaSheliakGivesOneSegment() {
        val vega = Equatorial(279.23473, 38.78369)
        val sheliak = Equatorial(282.51998, 33.36267)
        val segments = visibleLines(figure(vega, sheliak), view, observer, EPOCH_MILLIS, screen)
        assertEquals(1, segments.size)
        assertEquals(500.0, segments[0].x1, TOLERANCE)
        assertEquals(250.0, segments[0].y1, TOLERANCE)
        assertEquals(409.96, segments[0].x2, TOLERANCE)
        assertEquals(266.48, segments[0].y2, TOLERANCE)
    }

    @Test
    fun belowHorizonFigureGivesNothing() {
        val betelgeuse = Equatorial(88.79294, 7.40706)
        val rigel = Equatorial(78.63447, -8.20164)
        val segments = visibleLines(figure(betelgeuse, rigel), view, observer, EPOCH_MILLIS, screen)
        assertTrue(segments.isEmpty(), "Betelgeuse-Rigel should be below the horizon")
    }

    @Test
    fun vegaPolarisGivesOneSegment() {
        val vega = Equatorial(279.23473, 38.78369)
        val polaris = Equatorial(37.95456, 89.26411)
        val segments = visibleLines(figure(vega, polaris), view, observer, EPOCH_MILLIS, screen)
        assertEquals(1, segments.size)
        assertEquals(500.0, segments[0].x1, TOLERANCE)
        assertEquals(250.0, segments[0].y1, TOLERANCE)
    }

    @Test
    fun threePointFigureGivesTwoSegments() {
        val vega = Equatorial(279.23473, 38.78369)
        val sheliak = Equatorial(282.51998, 33.36267)
        val alkaid = Equatorial(286.65374, 49.31329)
        val segments =
            visibleLines(figure(vega, sheliak, alkaid), view, observer, EPOCH_MILLIS, screen)
        assertEquals(2, segments.size)
    }
}
