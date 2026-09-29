package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.StarRecord
import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOLERANCE = 0.5
private const val EPOCH_MILLIS = 1_790_542_800_000L
private const val WIDTH_PX = 1000.0
private const val HEIGHT_PX = 500.0

class VisibleStarsTest {
    private val observer = Observer(51.4769, 0.0)
    private val screen = ScreenSize(WIDTH_PX, HEIGHT_PX)
    private val vegaLook =
        LookDirection(azimuthDegrees = 263.4886, altitudeDegrees = 58.0508, rollDegrees = 0.0)

    private fun stars(): List<StarRecord> =
        listOf(
            StarRecord(91262, 279.23473f, 38.78369f, 0.03f, 0.0f),
            StarRecord(32349, 101.28716f, -16.71612f, -1.46f, 0.0f),
            StarRecord(27989, 88.79294f, 7.40706f, 0.50f, 1.85f),
            StarRecord(102098, 310.35798f, 45.28034f, 1.25f, 0.09f),
            StarRecord(97649, 297.69583f, 8.86832f, 0.76f, 0.22f),
        )

    @Test
    fun vegaProjectsToCenter() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val points = visibleStars(stars(), view, observer, EPOCH_MILLIS, screen)
        val vega = points.first { it.id == 91262 }
        assertEquals(500.0, vega.x, TOLERANCE)
        assertEquals(250.0, vega.y, TOLERANCE)
    }

    @Test
    fun belowHorizonStarsAreAbsent() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val points = visibleStars(stars(), view, observer, EPOCH_MILLIS, screen)
        assertTrue(points.none { it.id == 32349 }, "Sirius should be absent")
        assertTrue(points.none { it.id == 27989 }, "Betelgeuse should be absent")
    }

    @Test
    fun offscreenStarsAreAbsent() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val points = visibleStars(stars(), view, observer, EPOCH_MILLIS, screen)
        assertTrue(points.none { it.id == 102098 }, "Deneb should be absent")
        assertTrue(points.none { it.id == 97649 }, "Altair should be absent")
    }

    @Test
    fun faintStarAbsentAtWideFovPresentAtNarrow() {
        val faint = StarRecord(1, 279.23473f, 38.78369f, 7.5f, 0.0f)
        val wideView = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val narrowView = SkyView(look = vegaLook, fieldOfViewDegrees = 15.0)
        assertTrue(visibleStars(listOf(faint), wideView, observer, EPOCH_MILLIS, screen).isEmpty())
        assertTrue(
            visibleStars(listOf(faint), narrowView, observer, EPOCH_MILLIS, screen).isNotEmpty()
        )
    }
}
