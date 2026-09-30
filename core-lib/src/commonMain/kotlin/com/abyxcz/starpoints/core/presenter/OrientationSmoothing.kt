package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.projection.LookDirection
import com.abyxcz.starpoints.core.projection.cameraBasis

/**
 * Smooths the look direction on unit vectors, never on angles, so 359° and 1° average to 0°.
 * [alpha] is clamped into (0, 1]; 1, or no [previous], returns [next].
 */
fun smooth(previous: LookDirection?, next: LookDirection, alpha: Double): LookDirection {
    val a = alpha.coerceIn(Double.MIN_VALUE, 1.0)
    return if (previous == null || a >= 1.0) next else blend(previous, next, a)
}

private fun blend(previous: LookDirection, next: LookDirection, a: Double): LookDirection {
    val p = cameraBasis(previous)
    val n = cameraBasis(next)
    val forward = (p.forward * (1 - a) + n.forward * a).normalized()
    val mixedUp = p.up * (1 - a) + n.up * a
    // Re-orthogonalize up against forward; right completes the frame.
    val up = (mixedUp - forward * (mixedUp dot forward)).normalized()
    return lookFrom(right = forward cross up, up = up, forward = forward)
}
