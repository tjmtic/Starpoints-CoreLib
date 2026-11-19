package com.abyxcz.starpoints.core.catalog

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The bundled catalog/deep-sky.json parses: all 110 Messier objects, in Messier order. Reads the
 * repo's own file, so it runs on the JVM, whose tests start in the module directory.
 */
class BundledDeepSkyTest {
    private val objects = parseDeepSky(File("../catalog/deep-sky.json").readText())

    @Test
    fun theMessierCatalogIsComplete() {
        assertEquals((1..110).map { "M$it" }, objects.map { it.id })
    }

    @Test
    fun objectsCarryTheirSourceFields() {
        val andromeda = objects.single { it.id == "M31" }
        assertEquals(DeepSkyKind.SPIRAL_GALAXY, andromeda.kind)
        assertEquals("NGC 224", andromeda.designation)
        assertEquals(listOf(190.0, 60.0), andromeda.sizeArcminutes)
        assertEquals("Ptolemy's Cluster", objects.single { it.id == "M7" }.name)
    }
}
