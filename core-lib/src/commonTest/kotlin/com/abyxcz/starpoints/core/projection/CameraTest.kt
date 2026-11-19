package com.abyxcz.starpoints.core.projection

import com.abyxcz.starpoints.core.math.Horizontal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val WIDTH_PX = 1000.0
private const val HEIGHT_PX = 500.0
private const val HFOV_DEGREES = 60.0
private const val TOLERANCE = 1e-6

class CameraTest {
    @Test
    fun lookDirectionProjectsToCenter() {
        val look = LookDirection(azimuthDegrees = 0.0, altitudeDegrees = 0.0, rollDegrees = 0.0)
        val target = Horizontal(altitudeDegrees = 0.0, azimuthDegrees = 0.0)
        val point = project(target, look, HFOV_DEGREES, WIDTH_PX, HEIGHT_PX)
        assertEquals(500.0, point?.x ?: 0.0, TOLERANCE)
        assertEquals(250.0, point?.y ?: 0.0, TOLERANCE)
    }

    @Test
    fun azimuthThirtyProjectsToRightEdge() {
        val look = LookDirection(azimuthDegrees = 0.0, altitudeDegrees = 0.0, rollDegrees = 0.0)
        val target = Horizontal(altitudeDegrees = 0.0, azimuthDegrees = 30.0)
        val point = project(target, look, HFOV_DEGREES, WIDTH_PX, HEIGHT_PX)
        assertEquals(1000.0, point?.x ?: 0.0, TOLERANCE)
        assertEquals(250.0, point?.y ?: 0.0, TOLERANCE)
    }

    @Test
    fun altitudeTenProjectsAboveCenter() {
        val look = LookDirection(azimuthDegrees = 0.0, altitudeDegrees = 0.0, rollDegrees = 0.0)
        val target = Horizontal(altitudeDegrees = 10.0, azimuthDegrees = 0.0)
        val point = project(target, look, HFOV_DEGREES, WIDTH_PX, HEIGHT_PX)
        assertTrue((point?.y ?: 0.0) < 250.0, "expected y < 250, got ${point?.y}")
    }

    @Test
    fun rollOneEightyFlipsAltitude() {
        val look = LookDirection(azimuthDegrees = 0.0, altitudeDegrees = 0.0, rollDegrees = 180.0)
        val target = Horizontal(altitudeDegrees = 10.0, azimuthDegrees = 0.0)
        val point = project(target, look, HFOV_DEGREES, WIDTH_PX, HEIGHT_PX)
        assertTrue((point?.y ?: 0.0) > 250.0, "expected y > 250, got ${point?.y}")
    }

    @Test
    fun azimuthOneEightyIsBehind() {
        val look = LookDirection(azimuthDegrees = 0.0, altitudeDegrees = 0.0, rollDegrees = 0.0)
        val target = Horizontal(altitudeDegrees = 0.0, azimuthDegrees = 180.0)
        val point = project(target, look, HFOV_DEGREES, WIDTH_PX, HEIGHT_PX)
        assertNull(point)
    }
}
