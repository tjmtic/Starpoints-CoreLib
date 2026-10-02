package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.StarRecord
import com.abyxcz.starpoints.core.projection.LookDirection
import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOLERANCE = 0.5
private const val EPOCH_MILLIS = 1_790_542_800_000L
private const val WIDTH_PX = 1000.0
private const val HEIGHT_PX = 500.0

class StarPositionCacheTest {
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
    fun cachedVisibleStarsMatchUncached() {
        val view = SkyView(look = vegaLook, fieldOfViewDegrees = 60.0)
        val starList = stars()
        val uncached = visibleStars(starList, view, observer, EPOCH_MILLIS, screen)

        val cache = StarPositionCache(starList)
        val positions = cache.positions(observer, EPOCH_MILLIS)
        val cached = visibleStars(starList, positions, view, screen)

        assertEquals(uncached.size, cached.size)
        uncached.forEachIndexed { i, p ->
            assertEquals(p.x, cached[i].x, TOLERANCE)
            assertEquals(p.y, cached[i].y, TOLERANCE)
        }
    }

    @Test
    fun computationsIncrementOnTimeAndObserverChange() {
        val starList = stars()
        val cache = StarPositionCache(starList)

        cache.positions(observer, EPOCH_MILLIS)
        cache.positions(observer, EPOCH_MILLIS + 500L)
        cache.positions(observer, EPOCH_MILLIS + 999L)
        assertEquals(1, cache.computations)

        cache.positions(observer, EPOCH_MILLIS + 1500L)
        assertEquals(2, cache.computations)

        val differentObserver = Observer(48.8566, 2.3522)
        cache.positions(differentObserver, EPOCH_MILLIS + 1500L)
        assertEquals(3, cache.computations)
    }
}
