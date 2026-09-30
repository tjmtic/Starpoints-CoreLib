package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.Constellation
import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.math.apparentAltitudeDegrees
import com.abyxcz.starpoints.core.math.equatorialToHorizontal
import com.abyxcz.starpoints.core.math.julianDate
import com.abyxcz.starpoints.core.math.localSiderealTimeDegrees
import com.abyxcz.starpoints.core.projection.project

/**
 * A constellation name label projected onto the screen.
 *
 * @property id catalog identifier.
 * @property name the constellation's name.
 * @property x screen x in pixels.
 * @property y screen y in pixels.
 */
data class ConstellationLabel(val id: String, val name: String, val x: Double, val y: Double)

/**
 * The constellation labels projected onto the screen.
 *
 * @property labels the visible labels.
 */
data class ConstellationLabels(val labels: List<ConstellationLabel>)

private fun ScreenSize.contains(p: com.abyxcz.starpoints.core.projection.ScreenPoint): Boolean =
    p.x in 0.0..widthPx && p.y in 0.0..heightPx

/**
 * Projects the visible constellation labels to screen points.
 *
 * @param constellations the catalog entries.
 * @param view the sky view state.
 * @param observer the observer's position.
 * @param epochMillis the observation instant in epoch milliseconds.
 * @param screen the screen dimensions.
 * @return the visible labels as screen points.
 */
fun visibleConstellationLabels(
    constellations: List<Constellation>,
    view: SkyView,
    observer: Observer,
    epochMillis: Long,
    screen: ScreenSize,
): List<ConstellationLabel> {
    val jd = julianDate(epochMillis)
    val lst = localSiderealTimeDegrees(jd, observer.longitudeEastDegrees)
    return constellations
        .asSequence()
        .mapNotNull { labelFor(it, view, observer.latitudeDegrees, lst, screen) }
        .toList()
}

private fun labelFor(
    constellation: Constellation,
    view: SkyView,
    latitudeDegrees: Double,
    lst: Double,
    screen: ScreenSize,
): ConstellationLabel? {
    val horizontal =
        equatorialToHorizontal(
            constellation.labelRaDegrees,
            constellation.labelDecDegrees,
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
        ?.takeIf { screen.contains(it) }
        ?.let { ConstellationLabel(constellation.id, constellation.name, it.x, it.y) }
}
