package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.catalog.StarRecord
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** The degree, prime and double-prime signs are written as escapes (\u00b0, \u2032, \u2033). */
class StarFactSheetTest {
    private val sirius = StarRecord(32349, 101.287201f, -16.716101f, -1.44f, 0.009f)
    private val vega = StarRecord(91262, 279.234711f, 38.783699f, 0.03f, -0.001f)
    private val achernar = StarRecord(7588, 24.428499f, -57.236801f, 0.45f, -0.158f)
    private val betelgeuse = StarRecord(27989, 88.792900f, 7.407100f, 0.45f, 1.500f)

    /** The catalog's ten brightest stars, brightest first; Achernar and Betelgeuse tie at 0.45. */
    private val topTen =
        listOf(
            sirius,
            StarRecord(30438, 95.987999f, -52.695702f, -0.62f, 0.164f),
            StarRecord(69673, 213.915298f, 19.182400f, -0.05f, 1.239f),
            StarRecord(71683, 219.902100f, -60.834000f, -0.01f, 0.710f),
            vega,
            StarRecord(24608, 79.172302f, 45.998001f, 0.08f, 0.795f),
            StarRecord(24436, 78.634499f, -8.201600f, 0.18f, -0.030f),
            StarRecord(37279, 114.825500f, 5.225000f, 0.40f, 0.432f),
            achernar,
            betelgeuse,
        )

    private val names = mapOf(91262 to "Vega", 32349 to "Sirius")

    @Test
    fun aNamedStarIsTitledByNameAndListsItsFactsInOrder() {
        val sheet = starFactSheet(vega, topTen, names)
        assertEquals(GuideAnchor.Star(91262), sheet.anchor)
        assertEquals("Vega", sheet.title)
        assertEquals(
            listOf(
                Fact("name", "Vega"),
                Fact("designation", "HIP 91262"),
                Fact("magnitude", "0.03"),
                Fact("brightness rank", "5 of 10"),
                Fact("color", "blue-white"),
                Fact("right ascension", "18h 36m 56.3s"),
                Fact("declination", "+38\u00b0 47\u2032 01\u2033"),
            ),
            sheet.facts,
        )
    }

    @Test
    fun aNegativeMagnitudeKeepsItsSign() {
        val sheet = starFactSheet(sirius, topTen, names)
        assertEquals(Fact("magnitude", "-1.44"), sheet.facts[2])
        assertEquals(Fact("brightness rank", "1 of 10"), sheet.facts[3])
        assertEquals(Fact("color", "white"), sheet.facts[4])
        assertEquals(Fact("declination", "-16\u00b0 42\u2032 58\u2033"), sheet.facts[6])
    }

    @Test
    fun starsOfEqualMagnitudeShareARank() {
        // Both are unnamed here, so the rank is the third fact.
        assertEquals(
            Fact("brightness rank", "9 of 10"),
            starFactSheet(achernar, topTen, names).facts[2],
        )
        assertEquals(
            Fact("brightness rank", "9 of 10"),
            starFactSheet(betelgeuse, topTen, names).facts[2],
        )
    }

    @Test
    fun anUnnamedStarIsTitledByItsDesignationAndHasNoNameFact() {
        val star = StarRecord(66657, 204.971893f, -53.466400f, 2.29f, -0.171f)
        val sheet = starFactSheet(star, topTen + star, names)
        assertEquals("HIP 66657", sheet.title)
        assertEquals(Fact("designation", "HIP 66657"), sheet.facts.first())
        assertEquals(Fact("brightness rank", "11 of 11"), sheet.facts[2])
        assertEquals(6, sheet.facts.size)
    }

    @Test
    fun aStarOutsideTheCatalogIsRefused() {
        assertFailsWith<IllegalArgumentException> { starFactSheet(vega, listOf(sirius), names) }
    }

    @Test
    fun aRecordThatDiffersFromTheCatalogsIsRefused() {
        val brighterVega = vega.copy(magnitude = -2.0f)
        assertFailsWith<IllegalArgumentException> { starFactSheet(brighterVega, topTen, names) }
    }
}
