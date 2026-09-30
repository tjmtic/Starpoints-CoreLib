package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.ConstellationFigure
import com.abyxcz.starpoints.core.math.Equatorial
import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.math.apparentAltitudeDegrees
import com.abyxcz.starpoints.core.math.equatorialToHorizontal
import com.abyxcz.starpoints.core.math.julianDate
import com.abyxcz.starpoints.core.math.localSiderealTimeDegrees
import com.abyxcz.starpoints.core.projection.ScreenPoint
import com.abyxcz.starpoints.core.projection.project

private const val OFFSCREEN_MARGIN_PX = 20.0

/**
 * A line segment on the screen in pixels.
 *
 * @property x1 start x in pixels.
 * @property y1 start y in pixels.
 * @property x2 end x in pixels.
 * @property y2 end y in pixels.
 */
data class ScreenSegment(val x1: Double, val y1: Double, val x2: Double, val y2: Double)

private fun ScreenSize.shows(p: ScreenPoint): Boolean =
    p.x in -OFFSCREEN_MARGIN_PX..(widthPx + OFFSCREEN_MARGIN_PX) &&
        p.y in -OFFSCREEN_MARGIN_PX..(heightPx + OFFSCREEN_MARGIN_PX)

/**
 * Projects the visible constellation lines to screen segments.
 *
 * @param figures the constellation figures.
 * @param view the sky view state.
 * @param observer the observer's position.
 * @param epochMillis the observation instant in epoch milliseconds.
 * @param screen the screen dimensions.
 * @return the visible line segments.
 */
fun visibleLines(
    figures: List<ConstellationFigure>,
    view: SkyView,
    observer: Observer,
    epochMillis: Long,
    screen: ScreenSize,
): List<ScreenSegment> {
    val jd = julianDate(epochMillis)
    val lst = localSiderealTimeDegrees(jd, observer.longitudeEastDegrees)
    return figures
        .asSequence()
        .flatMap { figure -> figure.polylines.asSequence() }
        .flatMap { line -> segmentsFor(line, view, observer.latitudeDegrees, lst, screen) }
        .toList()
}

private fun segmentsFor(
    line: List<Equatorial>,
    view: SkyView,
    latitudeDegrees: Double,
    lst: Double,
    screen: ScreenSize,
): Sequence<ScreenSegment> {
    val projected = line.mapNotNull { pointFor(it, view, latitudeDegrees, lst, screen) }
    return projected.windowed(2).asSequence().mapNotNull { (a, b) ->
        if (screen.shows(a) || screen.shows(b)) {
            ScreenSegment(a.x, a.y, b.x, b.y)
        } else {
            null
        }
    }
}

private fun pointFor(
    point: Equatorial,
    view: SkyView,
    latitudeDegrees: Double,
    lst: Double,
    screen: ScreenSize,
): ScreenPoint? {
    val horizontal =
        equatorialToHorizontal(
            point.rightAscensionDegrees,
            point.declinationDegrees,
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
}
