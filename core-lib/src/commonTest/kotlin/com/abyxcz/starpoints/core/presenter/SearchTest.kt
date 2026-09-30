package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.Constellation
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
}
