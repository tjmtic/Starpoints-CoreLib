package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.catalog.Constellation
import com.abyxcz.starpoints.core.catalog.ConstellationFigure
import com.abyxcz.starpoints.core.math.Equatorial
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** The degree, prime and double-prime signs are written as escapes (\u00b0, \u2032, \u2033). */
class ConstellationFactSheetTest {
    private val lyra = Constellation("Lyr", "Lyra", 279.0, 30.0)
    private val caput = Constellation("Ser", "Serpens Caput", 232.5, 5.0)
    private val cauda = Constellation("Ser", "Serpens Cauda", 280.5, 3.0)
    private val rows = listOf(lyra, caput, cauda)

    private fun line(vararg points: Pair<Double, Double>) = points.map {
        Equatorial(it.first, it.second)
    }

    /** Lyra's figure from the catalog: one polyline of 8 points, so 7 segments. */
    private val lyraFigure =
        ConstellationFigure(
            "Lyr",
            listOf(
                line(
                    281.1932 to 37.6051,
                    281.0949 to 39.6127,
                    279.2347 to 38.7837,
                    281.1932 to 37.6051,
                    283.6262 to 36.8986,
                    284.7359 to 32.6896,
                    282.52 to 33.3627,
                    281.1932 to 37.6051,
                )
            ),
        )

    /** Serpens' merged figure from the catalog: Caput (9 points) and Cauda (6), so 13 segments. */
    private val serpensFigure =
        ConstellationFigure(
            "Ser",
            listOf(
                line(
                    236.5469 to 15.4218,
                    235.3877 to 19.6704,
                    237.1849 to 18.1416,
                    239.1133 to 15.6616,
                    236.5469 to 15.4218,
                    233.7006 to 10.5389,
                    236.067 to 6.4256,
                    237.704 to 4.4777,
                    243.5864 to -3.6943,
                ),
                line(
                    257.5945 to -15.7249,
                    264.3967 to -15.3986,
                    269.7566 to -9.7736,
                    270.7705 to -8.1803,
                    275.3275 to -2.8988,
                    284.0549 to 4.2036,
                ),
            ),
        )

    @Test
    fun aConstellationListsItsNameAbbreviationLabelAndFigure() {
        val sheet = constellationFactSheet("Lyr", rows, lyraFigure)
        assertEquals(GuideAnchor.Constellation("Lyr"), sheet.anchor)
        assertEquals("Lyra", sheet.title)
        assertEquals(
            listOf(
                Fact("name", "Lyra"),
                Fact("abbreviation", "Lyr"),
                Fact("label position", "18h 36m 00.0s, +30\u00b0 00\u2032 00\u2033"),
                Fact("figure lines", "7"),
            ),
            sheet.facts,
        )
    }

    @Test
    fun serpensIsOneSheetCoveringBothParts() {
        val sheet = constellationFactSheet("Ser", rows, serpensFigure)
        assertEquals(GuideAnchor.Constellation("Ser"), sheet.anchor)
        assertEquals("Serpens", sheet.title)
        assertEquals(
            listOf(
                Fact("name", "Serpens"),
                Fact("abbreviation", "Ser"),
                Fact("parts", "Serpens Caput, Serpens Cauda"),
                Fact(
                    "label positions",
                    "Serpens Caput 15h 30m 00.0s, +5\u00b0 00\u2032 00\u2033; " +
                        "Serpens Cauda 18h 42m 00.0s, +3\u00b0 00\u2032 00\u2033",
                ),
                Fact("figure lines", "13"),
            ),
            sheet.facts,
        )
    }

    @Test
    fun theCatalogsRowsGiveOneSheetPerAnchor() {
        val sheets = rows.map { it.id }.distinct().map { constellationFactSheet(it, rows, null) }
        assertEquals(listOf("Lyra", "Serpens"), sheets.map { it.title })
    }

    @Test
    fun withoutAFigureThereIsNoFigureFact() {
        assertEquals(3, constellationFactSheet("Lyr", rows, null).facts.size)
    }

    @Test
    fun anUnknownIdOrAnotherConstellationsFigureIsRefused() {
        assertFailsWith<IllegalArgumentException> { constellationFactSheet("UMa", rows, null) }
        assertFailsWith<IllegalArgumentException> {
            constellationFactSheet("Lyr", rows, serpensFigure)
        }
    }
}
