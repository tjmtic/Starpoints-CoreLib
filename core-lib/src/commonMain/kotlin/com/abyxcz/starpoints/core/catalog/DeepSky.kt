package com.abyxcz.starpoints.core.catalog

import com.abyxcz.starpoints.core.math.Equatorial
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * What kind of object a deep-sky object is, by the type [code] the catalog's source (d3-celestial)
 * uses, with the [description] a fact sheet gives it.
 */
enum class DeepSkyKind(val code: String, val description: String) {
    GALAXY_CLUSTER("gg", "galaxy cluster"),
    GALAXY("g", "galaxy"),
    SPIRAL_GALAXY("s", "spiral galaxy"),
    LENTICULAR_GALAXY("s0", "lenticular galaxy"),
    DWARF_GALAXY("sd", "dwarf galaxy"),
    ELLIPTICAL_GALAXY("e", "elliptical galaxy"),
    IRREGULAR_GALAXY("i", "irregular galaxy"),
    OPEN_CLUSTER("oc", "open cluster"),
    GLOBULAR_CLUSTER("gc", "globular cluster"),
    EMISSION_NEBULA("en", "emission nebula"),
    BRIGHT_NEBULA("bn", "bright nebula"),
    STAR_FORMING_REGION("sfr", "star-forming region"),
    REFLECTION_NEBULA("rn", "reflection nebula"),
    PLANETARY_NEBULA("pn", "planetary nebula"),
    SUPERNOVA_REMNANT("snr", "supernova remnant"),
    DARK_NEBULA("dn", "dark nebula"),
    /** A star cloud, double star or asterism: M24, M40 and M73 among the Messier objects. */
    STAR_GROUPING("pos", "star grouping"),
}

/**
 * A deep-sky object from the bundled catalog (catalog/deep-sky.json): today the 110 Messier
 * objects.
 *
 * @property id its catalog id, the Messier number (`M31`).
 * @property kind what kind of object it is.
 * @property position its J2000 position in degrees.
 * @property magnitude its apparent visual magnitude.
 * @property sizeArcminutes its apparent size in arcminutes: [major, minor], [diameter], or empty
 *   when the source gives none.
 * @property designation another catalog's designation (`NGC 224`), when the source has one.
 * @property name its common name (`Andromeda`), when it has one.
 * @property classification its morphological or cluster class (`SA(s)b LINER`), when known.
 */
data class DeepSkyObject(
    val id: String,
    val kind: DeepSkyKind,
    val position: Equatorial,
    val magnitude: Double,
    val sizeArcminutes: List<Double> = emptyList(),
    val designation: String? = null,
    val name: String? = null,
    val classification: String? = null,
)

private val DEEP_SKY_JSON = Json { ignoreUnknownKeys = true }

@Serializable
private data class DeepSkyEntry(
    val id: String,
    val kind: String,
    val ra: Double,
    val dec: Double,
    val mag: Double,
    val size: List<Double> = emptyList(),
    val designation: String? = null,
    val name: String? = null,
    @SerialName("class") val classification: String? = null,
)

/**
 * Parses the deep-sky catalog, keeping the file's order. Throws [IllegalArgumentException] for
 * malformed JSON or a kind code [DeepSkyKind] does not know.
 */
fun parseDeepSky(json: String): List<DeepSkyObject> =
    DEEP_SKY_JSON.decodeFromString<List<DeepSkyEntry>>(json).map { entry ->
        val kind =
            requireNotNull(DeepSkyKind.entries.firstOrNull { it.code == entry.kind }) {
                "${entry.id} has an unknown kind '${entry.kind}'"
            }
        DeepSkyObject(
            id = entry.id,
            kind = kind,
            position = Equatorial(entry.ra, entry.dec),
            magnitude = entry.mag,
            sizeArcminutes = entry.size,
            designation = entry.designation,
            name = entry.name,
            classification = entry.classification,
        )
    }
