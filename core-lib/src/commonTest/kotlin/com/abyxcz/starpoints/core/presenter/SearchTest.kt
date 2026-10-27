package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.Constellation
import com.abyxcz.starpoints.core.catalog.DeepSkyKind
import com.abyxcz.starpoints.core.catalog.DeepSkyObject
import com.abyxcz.starpoints.core.math.Equatorial
import kotlin.test.Test
import kotlin.test.assertEquals

class SearchTest {
    private val names = mapOf(1 to "Vega", 2 to "Vindemiatrix", 3 to "Avior", 4 to "Sirius")
    private val serRa = 236.0
    private val serDec = 10.0
    private val virRa = 180.0
    private val virDec = 0.0
    private val constellations =
        listOf(
            Constellation("Vir", "Virgo", virRa, virDec),
            Constellation("Ser", "Serpens", serRa, serDec),
            Constellation("Ser", "Serpens", serRa, serDec),
        )

    @Test
    fun ranksPrefixMatchesAlphabeticallyThenContains() {
        val results = search("v", names, constellations)
        assertEquals(
            listOf(
                StarResult(1, "Vega"),
                StarResult(2, "Vindemiatrix"),
                ConstellationResult("Vir", "Virgo"),
                StarResult(3, "Avior"),
            ),
            results,
        )
    }

    @Test
    fun exactMatchIsCaseInsensitive() {
        val results = search("VEGA", names, constellations)
        assertEquals(listOf(StarResult(1, "Vega")), results)
    }

    @Test
    fun deduplicatesConstellationsById() {
        val results = search("ser", names, constellations)
        assertEquals(listOf(ConstellationResult("Ser", "Serpens")), results)
    }

    @Test
    fun emptyQueryReturnsEmptyList() {
        assertEquals(emptyList<SearchResult>(), search("", names, constellations))
    }

    @Test
    fun limitCapsResults() {
        val results = search("v", names, constellations, limit = 2)
        assertEquals(2, results.size)
    }

    private val deepSky =
        listOf(
            DeepSkyObject(
                "M31",
                DeepSkyKind.SPIRAL_GALAXY,
                Equatorial(10.68, 41.27),
                3.4,
                designation = "NGC 224",
                name = "Andromeda",
            ),
            DeepSkyObject("M3", DeepSkyKind.GLOBULAR_CLUSTER, Equatorial(205.55, 28.38), 6.2),
        )

    @Test
    fun findsADeepSkyObjectByNameIdOrDesignation() {
        val andromeda = DeepSkyResult("M31", "Andromeda (M31)")
        assertEquals(listOf(andromeda), search("andromeda", names, constellations, deepSky))
        assertEquals(listOf(andromeda), search("ngc 224", names, constellations, deepSky))
        assertEquals(
            listOf(DeepSkyResult("M3", "M3"), andromeda),
            search("m3", names, constellations, deepSky),
        )
    }

    @Test
    fun deepSkyIsLeftOutWhenNotGiven() {
        assertEquals(emptyList(), search("andromeda", names, constellations))
    }
}
