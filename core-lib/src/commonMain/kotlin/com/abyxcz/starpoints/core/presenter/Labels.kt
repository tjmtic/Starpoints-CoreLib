package com.abyxcz.starpoints.core.presenter

private const val WIDE_LABEL_FOV_DEGREES = 90.0
private const val NARROW_LABEL_FOV_DEGREES = 20.0
private const val WIDE_LABEL_MAGNITUDE = 1.5
private const val NARROW_LABEL_MAGNITUDE = 3.0

/**
 * A named star to be labelled on the screen.
 *
 * @property id catalog identifier.
 * @property name the star's name.
 * @property x screen x in pixels.
 * @property y screen y in pixels.
 */
data class StarLabel(val id: Int, val name: String, val x: Double, val y: Double)

/**
 * Brightest magnitude that gets a name at a given field of view.
 *
 * 1.5 at 90° and wider, 3.0 at 20° and narrower, linear in between.
 *
 * @param fieldOfViewDegrees field of view in degrees.
 * @return brightest magnitude to label.
 */
fun labelLimitFor(fieldOfViewDegrees: Double): Double {
    val t =
        ((WIDE_LABEL_FOV_DEGREES - fieldOfViewDegrees) /
                (WIDE_LABEL_FOV_DEGREES - NARROW_LABEL_FOV_DEGREES))
            .coerceIn(0.0, 1.0)
    return WIDE_LABEL_MAGNITUDE + t * (NARROW_LABEL_MAGNITUDE - WIDE_LABEL_MAGNITUDE)
}

/**
 * The named stars to label at a given field of view.
 *
 * @param points the visible stars as screen points.
 * @param names the star names keyed by catalog id.
 * @param fieldOfViewDegrees field of view in degrees.
 * @return the labelled stars, in the points' order.
 */
fun labelsFor(
    points: List<SkyPoint>,
    names: Map<Int, String>,
    fieldOfViewDegrees: Double,
): List<StarLabel> {
    val limit = labelLimitFor(fieldOfViewDegrees)
    return points.mapNotNull { point ->
        names[point.id]
            ?.takeIf { point.magnitude <= limit }
            ?.let { name -> StarLabel(point.id, name, point.x, point.y) }
    }
}
