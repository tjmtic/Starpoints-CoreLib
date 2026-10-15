package com.abyxcz.starpoints.core.catalog

import com.abyxcz.starpoints.core.math.Equatorial
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeepSkyTest {
    @Test
    fun parsesObjectsKeepingOrderAndOptionalFields() {
        val json =
            "[{\"id\":\"M31\",\"designation\":\"NGC 224\",\"name\":\"Andromeda\",\"kind\":\"s\"," +
                "\"ra\":10.6751,\"dec\":41.2667,\"mag\":3.4,\"size\":[190.0,60.0]," +
                "\"class\":\"SA(s)b LINER\"}," +
                "{\"id\":\"M40\",\"designation\":\"WN 4\",\"kind\":\"pos\",\"ra\":185.5999," +
                "\"dec\":58.0833,\"mag\":8.0,\"size\":[]}]"
        val parsed = parseDeepSky(json)
        assertEquals(
            DeepSkyObject(
                id = "M31",
                kind = DeepSkyKind.SPIRAL_GALAXY,
                position = Equatorial(10.6751, 41.2667),
                magnitude = 3.4,
                sizeArcminutes = listOf(190.0, 60.0),
                designation = "NGC 224",
                name = "Andromeda",
                classification = "SA(s)b LINER",
            ),
            parsed[0],
        )
        assertEquals(
            DeepSkyObject(
                "M40",
                DeepSkyKind.STAR_GROUPING,
                Equatorial(185.5999, 58.0833),
                8.0,
                designation = "WN 4",
            ),
            parsed[1],
        )
    }

    @Test
    fun everyKindHasItsOwnCode() {
        val codes = DeepSkyKind.entries.map { it.code }
        assertEquals(codes.size, codes.toSet().size)
        assertEquals(DeepSkyKind.GLOBULAR_CLUSTER, DeepSkyKind.entries.single { it.code == "gc" })
    }

    @Test
    fun anUnknownKindIsRefused() {
        val json = "[{\"id\":\"M1\",\"kind\":\"quasar\",\"ra\":83.6,\"dec\":22.0,\"mag\":8.4}]"
        assertFailsWith<IllegalArgumentException> { parseDeepSky(json) }
    }
}
