package com.abyxcz.starpoints.core.catalog

import com.abyxcz.starpoints.core.math.Equatorial
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstellationsTest {
    @Test
    fun parsesConstellationsKeepingOrderAndDuplicateIds() {
        val json =
            "[{\"id\":\"And\",\"name\":\"Andromeda\",\"labelRa\":0.75,\"labelDec\":43.0}," +
                "{\"id\":\"Ser\",\"name\":\"Serpens\",\"labelRa\":236.0,\"labelDec\":10.0}," +
                "{\"id\":\"Ser\",\"name\":\"Serpens\",\"labelRa\":275.0,\"labelDec\":-10.0}]"
        val parsed = parseConstellations(json)
        assertEquals(3, parsed.size)
        assertEquals("And", parsed[0].id)
        assertEquals("Andromeda", parsed[0].name)
        assertEquals(0.75, parsed[0].labelRaDegrees, 1e-9)
        assertEquals(43.0, parsed[0].labelDecDegrees, 1e-9)
        assertEquals("Ser", parsed[1].id)
        assertEquals("Ser", parsed[2].id)
        assertEquals(236.0, parsed[1].labelRaDegrees, 1e-9)
        assertEquals(275.0, parsed[2].labelRaDegrees, 1e-9)
    }

    @Test
    fun parsesConstellationLinesSortedById() {
        val json =
            "{\"Ser\":[[[1.0,2.0],[3.0,4.0]],[[5.0,6.0],[7.0,8.0]]]," +
                "\"Lyr\":[[[279.2347,38.7837],[282.52,33.3627]]]}"
        val figures = parseConstellationLines(json)
        assertEquals(2, figures.size)
        assertEquals("Lyr", figures[0].id)
        assertEquals("Ser", figures[1].id)
        assertEquals(1, figures[0].polylines.size)
        assertEquals(2, figures[1].polylines.size)
        assertEquals(Equatorial(279.2347, 38.7837), figures[0].polylines[0][0])
        assertEquals(Equatorial(1.0, 2.0), figures[1].polylines[0][0])
        assertEquals(Equatorial(7.0, 8.0), figures[1].polylines[1][1])
    }

    @Test
    fun ignoresUnknownKeysInConstellation() {
        val json =
            "[{\"id\":\"And\",\"name\":\"Andromeda\",\"labelRa\":0.75,\"labelDec\":43.0,\"extra\":1}]"
        val parsed = parseConstellations(json)
        assertEquals(1, parsed.size)
        assertEquals("And", parsed[0].id)
        assertEquals(0.75, parsed[0].labelRaDegrees, 1e-9)
    }
}
