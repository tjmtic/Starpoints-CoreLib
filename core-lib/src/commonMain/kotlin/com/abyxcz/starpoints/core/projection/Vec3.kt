package com.abyxcz.starpoints.core.projection

import kotlin.math.sqrt

/**
 * A 3D vector in the East-North-Up frame.
 *
 * @property x east component.
 * @property y north component.
 * @property z up component.
 */
internal data class Vec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(o: Vec3): Vec3 = Vec3(x + o.x, y + o.y, z + o.z)

    operator fun minus(o: Vec3): Vec3 = Vec3(x - o.x, y - o.y, z - o.z)

    operator fun times(s: Double): Vec3 = Vec3(x * s, y * s, z * s)

    operator fun unaryMinus(): Vec3 = Vec3(-x, -y, -z)

    infix fun dot(o: Vec3): Double = x * o.x + y * o.y + z * o.z

    infix fun cross(o: Vec3): Vec3 = Vec3(y * o.z - z * o.y, z * o.x - x * o.z, x * o.y - y * o.x)

    fun length(): Double = sqrt(x * x + y * y + z * z)

    fun normalized(): Vec3 {
        val len = length()
        return if (len < DEGENERATE_LENGTH) Vec3(0.0, 0.0, 0.0) else Vec3(x / len, y / len, z / len)
    }
}

private const val DEGENERATE_LENGTH = 1e-12
