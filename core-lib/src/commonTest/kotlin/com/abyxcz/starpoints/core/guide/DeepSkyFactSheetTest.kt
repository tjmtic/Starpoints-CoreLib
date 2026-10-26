package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.catalog.DeepSkyKind
import com.abyxcz.starpoints.core.catalog.DeepSkyObject
import com.abyxcz.starpoints.core.math.Equatorial
import kotlin.test.Test
import kotlin.test.assertEquals

/** The prime, degree and multiplication signs are written as escapes (′, °, ×). */
class DeepSkyFactSheetTest {
    private val andromeda =
        DeepSkyObject(
            id = "M31",
            kind = DeepSkyKind.SPIRAL_GALAXY,
            position = Equatorial(10.6751, 41.2667),
            magnitude = 3.4,
            sizeArcminutes = listOf(190.0, 60.0),
            designation = "NGC 224",
            name = "Andromeda",
            classification = "SA(s)b LINER",
        )

    @Test
    fun aNamedObjectIsTitledByNameAndIdAndListsItsFactsInOrder() {
        val sheet = deepSkyFactSheet(andromeda)
        assertEquals(GuideAnchor.DeepSky("M31"), sheet.anchor)
        assertEquals("Andromeda (M31)", sheet.title)
        assertEquals(
            listOf(
                Fact("name", "Andromeda"),
                Fact("designation", "M31"),
                Fact("other designation", "NGC 224"),
                Fact("type", "spiral galaxy"),
                Fact("magnitude", "3.4"),
                Fact("apparent size", "190′ × 60′"),
                Fact("class", "SA(s)b LINER"),
                Fact("right ascension", "0h 42m 42.0s"),
                Fact("declination", "+41° 16′ 00″"),
            ),
            sheet.facts,
        )
    }

    @Test
    fun anObjectWithNoNameSizeOrClassLeavesThemOut() {
        val m40 =
            DeepSkyObject(
                "M40",
                DeepSkyKind.STAR_GROUPING,
                Equatorial(185.5999, 58.0833),
                8.0,
                designation = "WN 4",
            )
        val sheet = deepSkyFactSheet(m40)
        assertEquals("M40", sheet.title)
        assertEquals(
            listOf(
                "designation",
                "other designation",
                "type",
                "magnitude",
                "right ascension",
                "declination",
            ),
            sheet.facts.map { it.key },
        )
        assertEquals(Fact("magnitude", "8.0"), sheet.facts[3])
        assertEquals(Fact("type", "star grouping"), sheet.facts[2])
    }

    @Test
    fun aSizeThatIsNotWholeKeepsOneDecimal() {
        val ring = andromeda.copy(sizeArcminutes = listOf(1.4, 1.0), name = null)
        val size = deepSkyFactSheet(ring).facts.single { it.key == "apparent size" }
        assertEquals("1.4′ × 1′", size.value)
    }
}
