package com.abyxcz.starpoints.core.presenter

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SavedPointsTest {
    @Test
    fun roundTripsTwoPointsIncludingNullStarId() {
        val points =
            listOf(
                SavedPoint(
                    id = "a",
                    name = "Sirius",
                    rightAscensionDegrees = 101.287,
                    declinationDegrees = -16.716,
                    savedAtEpochMillis = 1000L,
                    starId = 32349,
                ),
                SavedPoint(
                    id = "b",
                    name = "Vega",
                    rightAscensionDegrees = 279.235,
                    declinationDegrees = 38.784,
                    savedAtEpochMillis = 2000L,
                    starId = null,
                ),
            )
        val decoded = decodeSavedPoints(encodeSavedPoints(points))
        assertEquals(points, decoded)
    }

    @Test
    fun decodeMalformedJsonIsEmpty() {
        assertTrue(decodeSavedPoints("not json").isEmpty())
    }

    @Test
    fun decodeEmptyArrayIsEmpty() {
        assertTrue(decodeSavedPoints("[]").isEmpty())
    }

    @Test
    fun upsertReplacesExistingId() {
        val original =
            SavedPoint(
                id = "a",
                name = "Old",
                rightAscensionDegrees = 10.0,
                declinationDegrees = 20.0,
                savedAtEpochMillis = 1000L,
            )
        val updated =
            SavedPoint(
                id = "a",
                name = "New",
                rightAscensionDegrees = 11.0,
                declinationDegrees = 21.0,
                savedAtEpochMillis = 2000L,
            )
        val result = listOf(original).upsert(updated)
        assertEquals(1, result.size)
        assertEquals("New", result[0].name)
    }

    @Test
    fun upsertOrdersNewestFirst() {
        val older =
            SavedPoint(
                id = "a",
                name = "Old",
                rightAscensionDegrees = 10.0,
                declinationDegrees = 20.0,
                savedAtEpochMillis = 1000L,
            )
        val newer =
            SavedPoint(
                id = "b",
                name = "New",
                rightAscensionDegrees = 30.0,
                declinationDegrees = 40.0,
                savedAtEpochMillis = 3000L,
            )
        val result = listOf(older).upsert(newer)
        assertEquals(2, result.size)
        assertEquals("New", result[0].name)
        assertEquals("Old", result[1].name)
    }

    @Test
    fun removeDropsById() {
        val a =
            SavedPoint(
                id = "a",
                name = "A",
                rightAscensionDegrees = 10.0,
                declinationDegrees = 20.0,
                savedAtEpochMillis = 1000L,
            )
        val b =
            SavedPoint(
                id = "b",
                name = "B",
                rightAscensionDegrees = 30.0,
                declinationDegrees = 40.0,
                savedAtEpochMillis = 2000L,
            )
        val result = listOf(a, b).remove("a")
        assertEquals(1, result.size)
        assertEquals("B", result[0].name)
    }
}
