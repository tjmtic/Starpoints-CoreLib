package com.abyxcz.starpoints.core.catalog

import kotlin.test.Test
import kotlin.test.assertEquals

class StarNamesTest {
    @Test
    fun parsesValidStarNames() {
        val result = parseStarNames("{\"32349\":\"Sirius\",\"91262\":\"Vega\"}")
        assertEquals(2, result.size)
        assertEquals("Sirius", result[32349])
        assertEquals("Vega", result[91262])
    }

    @Test
    fun emptyJsonGivesEmptyMap() {
        val result = parseStarNames("{}")
        assertEquals(0, result.size)
    }

    @Test
    fun skipsNonNumericKeys() {
        val result = parseStarNames("{\"x\":\"Nope\",\"7588\":\"Achernar\"}")
        assertEquals(1, result.size)
        assertEquals("Achernar", result[7588])
    }
}
