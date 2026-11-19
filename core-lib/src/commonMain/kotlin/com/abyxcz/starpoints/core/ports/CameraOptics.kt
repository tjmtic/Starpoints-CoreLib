package com.abyxcz.starpoints.core.ports

import kotlinx.coroutines.flow.Flow

/**
 * Camera optics sample.
 *
 * @property horizontalFieldOfViewDegrees horizontal field of view at 1× zoom, for the preview as
 *   shown on screen.
 * @property zoomRatio current zoom ratio.
 */
data class CameraFrame(
    val horizontalFieldOfViewDegrees: Double,
    val zoomRatio: Double,
)

/** Source of camera optics samples. */
interface CameraOptics {
    val frame: Flow<CameraFrame>
}
