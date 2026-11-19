package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.math.Horizontal
import com.abyxcz.starpoints.core.math.normalizeDegrees
import com.abyxcz.starpoints.core.projection.LookDirection

private const val MIN_FOV_DEGREES = 5.0
private const val MAX_FOV_DEGREES = 120.0
private const val MIN_ALTITUDE_DEGREES = -90.0
private const val MAX_ALTITUDE_DEGREES = 90.0

/**
 * The sky view state: where the camera looks, how wide, and the display mode.
 *
 * @property look the camera's look direction.
 * @property fieldOfViewDegrees horizontal field of view in degrees.
 * @property nightMode whether night mode is on.
 */
data class SkyView(
    val look: LookDirection =
        LookDirection(azimuthDegrees = 180.0, altitudeDegrees = 45.0, rollDegrees = 0.0),
    val fieldOfViewDegrees: Double = 60.0,
    val nightMode: Boolean = false,
)

/**
 * Pans the view by a screen drag. The sky follows the finger: dragging right moves the look azimuth
 * left, dragging down moves the look altitude up.
 *
 * @param dxPx horizontal drag in pixels (positive = right).
 * @param dyPx vertical drag in pixels (positive = down).
 * @param widthPx screen width in pixels.
 * @return the panned view.
 */
fun SkyView.pan(dxPx: Double, dyPx: Double, widthPx: Double): SkyView {
    val degreesPerPixel = fieldOfViewDegrees / widthPx
    val newAzimuth = (look.azimuthDegrees - dxPx * degreesPerPixel).normalizeDegrees()
    val newAltitude =
        (look.altitudeDegrees + dyPx * degreesPerPixel).coerceIn(
            MIN_ALTITUDE_DEGREES,
            MAX_ALTITUDE_DEGREES,
        )
    return copy(look = look.copy(azimuthDegrees = newAzimuth, altitudeDegrees = newAltitude))
}

/**
 * Zooms the view by a pinch scale. A scale above 1 zooms in (narrows the field of view).
 *
 * @param scale pinch scale; a value at or below 0 leaves the view unchanged.
 * @return the zoomed view.
 */
fun SkyView.zoom(scale: Double): SkyView {
    if (scale <= 0.0) return this
    val newFov = (fieldOfViewDegrees / scale).coerceIn(MIN_FOV_DEGREES, MAX_FOV_DEGREES)
    return copy(fieldOfViewDegrees = newFov)
}

/** Toggles night mode. */
fun SkyView.toggleNightMode(): SkyView = copy(nightMode = !nightMode)

/**
 * Centres the view on a target: looks straight at it (azimuth and altitude from it, roll 0),
 * keeping the field of view and night mode.
 *
 * @param target the sky direction to look at.
 * @return the centred view.
 */
fun SkyView.centeredOn(target: Horizontal): SkyView =
    copy(
        look =
            LookDirection(
                azimuthDegrees = target.azimuthDegrees,
                altitudeDegrees = target.altitudeDegrees,
                rollDegrees = 0.0,
            )
    )
