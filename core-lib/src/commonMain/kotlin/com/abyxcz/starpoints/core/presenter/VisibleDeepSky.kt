package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.DeepSkyKind
import com.abyxcz.starpoints.core.catalog.DeepSkyObject
import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.math.apparentAltitudeDegrees
import com.abyxcz.starpoints.core.math.equatorialToHorizontal
import com.abyxcz.starpoints.core.math.julianDate
import com.abyxcz.starpoints.core.math.localSiderealTimeDegrees
import com.abyxcz.starpoints.core.math.toRadians
import com.abyxcz.starpoints.core.projection.project
import kotlin.math.tan

private const val ARCMINUTES_PER_DEGREE = 60.0
private const val MIN_RADIUS_PX = 6.0
private const val MAX_RADIUS_PX = 120.0

/** How a deep-sky object is drawn: one map symbol for each family of [DeepSkyKind]. */
enum class DeepSkySymbol {
    /** An ellipse: every kind of galaxy and galaxy cluster. */
    GALAXY,

    /** A dashed circle: an open cluster or a star grouping. */
    OPEN_CLUSTER,

    /** A circle crossed by two lines: a globular cluster. */
    GLOBULAR_CLUSTER,

    /** A square: an emission, bright, reflection or dark nebula, or a star-forming region. */
    NEBULA,

    /** A circle with four ticks: a planetary nebula or a supernova remnant. */
    PLANETARY_NEBULA,
}

/** The map symbol for this kind of deep-sky object. */
val DeepSkyKind.symbol: DeepSkySymbol
    get() =
        when (this) {
            DeepSkyKind.GALAXY_CLUSTER,
            DeepSkyKind.GALAXY,
            DeepSkyKind.SPIRAL_GALAXY,
            DeepSkyKind.LENTICULAR_GALAXY,
            DeepSkyKind.DWARF_GALAXY,
            DeepSkyKind.ELLIPTICAL_GALAXY,
            DeepSkyKind.IRREGULAR_GALAXY -> DeepSkySymbol.GALAXY
            DeepSkyKind.OPEN_CLUSTER,
            DeepSkyKind.STAR_GROUPING -> DeepSkySymbol.OPEN_CLUSTER
            DeepSkyKind.GLOBULAR_CLUSTER -> DeepSkySymbol.GLOBULAR_CLUSTER
            DeepSkyKind.PLANETARY_NEBULA,
            DeepSkyKind.SUPERNOVA_REMNANT -> DeepSkySymbol.PLANETARY_NEBULA
            DeepSkyKind.EMISSION_NEBULA,
            DeepSkyKind.BRIGHT_NEBULA,
            DeepSkyKind.STAR_FORMING_REGION,
            DeepSkyKind.REFLECTION_NEBULA,
            DeepSkyKind.DARK_NEBULA -> DeepSkySymbol.NEBULA
        }

/**
 * A deep-sky object projected onto the screen.
 *
 * @property id catalog identifier, as `M31`.
 * @property symbol how it is drawn.
 * @property x screen x in pixels.
 * @property y screen y in pixels.
 * @property radiusPx half its apparent size on screen, clamped so a small object still shows and a
 *   large one does not cover the view.
 */
data class DeepSkyPoint(
    val id: String,
    val symbol: DeepSkySymbol,
    val x: Double,
    val y: Double,
    val radiusPx: Double,
)

/**
 * Projects the deep-sky objects above the horizon and on [screen] to screen points.
 *
 * @param objects the catalog entries.
 * @param view the sky view state.
 * @param observer the observer's position.
 * @param epochMillis the observation instant in epoch milliseconds.
 * @param screen the screen dimensions.
 * @return the visible objects, in catalog order.
 */
fun visibleDeepSky(
    objects: List<DeepSkyObject>,
    view: SkyView,
    observer: Observer,
    epochMillis: Long,
    screen: ScreenSize,
): List<DeepSkyPoint> {
    val lst = localSiderealTimeDegrees(julianDate(epochMillis), observer.longitudeEastDegrees)
    val pxPerDegree = pixelsPerDegree(view.fieldOfViewDegrees, screen.widthPx)
    return objects.mapNotNull { dso ->
        val horizontal =
            equatorialToHorizontal(
                dso.position.rightAscensionDegrees,
                dso.position.declinationDegrees,
                observer.latitudeDegrees,
                lst,
            )
        val altitude = apparentAltitudeDegrees(horizontal.altitudeDegrees)
        if (altitude < 0.0) return@mapNotNull null
        project(
                Horizontal(altitude, horizontal.azimuthDegrees),
                view.look,
                view.fieldOfViewDegrees,
                screen.widthPx,
                screen.heightPx,
            )
            ?.takeIf { it.x in 0.0..screen.widthPx && it.y in 0.0..screen.heightPx }
            ?.let { DeepSkyPoint(dso.id, dso.kind.symbol, it.x, it.y, radiusOf(dso, pxPerDegree)) }
    }
}

/** Screen pixels per degree at the centre of a gnomonic view [fieldOfViewDegrees] wide. */
private fun pixelsPerDegree(fieldOfViewDegrees: Double, widthPx: Double): Double {
    val focalPx = widthPx / 2 / tan((fieldOfViewDegrees / 2).toRadians())
    return focalPx * 1.0.toRadians()
}

/** Half the object's largest catalog size, in pixels, within the drawable range. */
private fun radiusOf(dso: DeepSkyObject, pxPerDegree: Double): Double {
    val major = dso.sizeArcminutes.maxOrNull() ?: 0.0
    return (major / ARCMINUTES_PER_DEGREE / 2 * pxPerDegree).coerceIn(MIN_RADIUS_PX, MAX_RADIUS_PX)
}
