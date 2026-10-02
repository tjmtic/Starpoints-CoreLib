package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.StarRecord
import com.abyxcz.starpoints.core.catalog.magnitudeLimitFor
import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.math.apparentAltitudeDegrees
import com.abyxcz.starpoints.core.math.equatorialToHorizontal
import com.abyxcz.starpoints.core.math.julianDate
import com.abyxcz.starpoints.core.math.localSiderealTimeDegrees
import com.abyxcz.starpoints.core.projection.ScreenPoint
import com.abyxcz.starpoints.core.projection.project

private const val OFFSCREEN_MARGIN_PX = 20.0

/**
 * A star projected onto the screen.
 *
 * @property id catalog identifier.
 * @property x screen x in pixels.
 * @property y screen y in pixels.
 * @property magnitude apparent visual magnitude.
 * @property colorIndex color index (B−V).
 */
data class SkyPoint(
    val id: Int,
    val x: Double,
    val y: Double,
    val magnitude: Double,
    val colorIndex: Double,
)

/**
 * The screen dimensions in pixels.
 *
 * @property widthPx width in pixels.
 * @property heightPx height in pixels.
 */
data class ScreenSize(val widthPx: Double, val heightPx: Double)

private fun ScreenSize.shows(p: ScreenPoint): Boolean =
    p.x in -OFFSCREEN_MARGIN_PX..(widthPx + OFFSCREEN_MARGIN_PX) &&
        p.y in -OFFSCREEN_MARGIN_PX..(heightPx + OFFSCREEN_MARGIN_PX)

/**
 * Projects the visible stars to screen points.
 *
 * @param stars the catalog records.
 * @param view the sky view state.
 * @param observer the observer's position.
 * @param epochMillis the observation instant in epoch milliseconds.
 * @param screen the screen dimensions.
 * @return the visible stars as screen points, brightest first.
 */
fun visibleStars(
    stars: List<StarRecord>,
    view: SkyView,
    observer: Observer,
    epochMillis: Long,
    screen: ScreenSize,
    magnitudeLimit: Double = magnitudeLimitFor(view.fieldOfViewDegrees),
): List<SkyPoint> {
    val limit = magnitudeLimit
    val jd = julianDate(epochMillis)
    val lst = localSiderealTimeDegrees(jd, observer.longitudeEastDegrees)
    return stars
        .asSequence()
        .filter { it.magnitude <= limit }
        .mapNotNull { pointFor(it, view, observer.latitudeDegrees, lst, screen) }
        .sortedBy { it.magnitude }
        .toList()
}

private fun pointFor(
    star: StarRecord,
    view: SkyView,
    latitudeDegrees: Double,
    lst: Double,
    screen: ScreenSize,
): SkyPoint? {
    val horizontal =
        equatorialToHorizontal(
            star.rightAscensionDegrees.toDouble(),
            star.declinationDegrees.toDouble(),
            latitudeDegrees,
            lst,
        )
    val altitude = apparentAltitudeDegrees(horizontal.altitudeDegrees)
    if (altitude < 0.0) return null
    return project(
            Horizontal(altitude, horizontal.azimuthDegrees),
            view.look,
            view.fieldOfViewDegrees,
            screen.widthPx,
            screen.heightPx,
        )
        ?.takeIf { screen.shows(it) }
        ?.let {
            SkyPoint(
                star.id,
                it.x,
                it.y,
                star.magnitude.toDouble(),
                star.colorIndex.toDouble(),
            )
        }
}
