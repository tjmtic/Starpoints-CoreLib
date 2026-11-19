package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.catalog.Constellation
import com.abyxcz.starpoints.core.catalog.ConstellationFigure
import com.abyxcz.starpoints.core.catalog.DeepSkyObject
import com.abyxcz.starpoints.core.catalog.StarRecord
import com.abyxcz.starpoints.core.presenter.colorName
import com.abyxcz.starpoints.core.presenter.formatDeclination
import com.abyxcz.starpoints.core.presenter.formatRightAscension
import kotlin.math.abs
import kotlin.math.roundToLong

private const val HUNDREDTHS = 100L
private const val TENTHS = 10L
private const val HIP_PREFIX = "HIP "

/**
 * One true, computed fact about an anchored object: a stable [key] and its [value] as text. Only
 * the core makes one.
 */
@ConsistentCopyVisibility data class Fact internal constructor(val key: String, val value: String)

/**
 * The true, computed facts about one anchored object, which a Guide entry is grounded on. Only the
 * core builds one (its constructor and `copy` are internal), from catalog data, so nothing outside
 * the core can put fiction into a fact sheet.
 *
 * @property anchor the object the facts are about.
 * @property title the object's name, or its designation when it has none; a deep-sky object's name
 *   is followed by its catalog id, as in "Andromeda (M31)".
 * @property facts the facts, in a fixed order.
 */
@ConsistentCopyVisibility
data class FactSheet
internal constructor(val anchor: GuideAnchor, val title: String, val facts: List<Fact>)

/**
 * The fact sheet for a [star], which must be one of [catalog]'s own records. Its brightness rank is
 * one more than the number of catalog stars strictly brighter, so stars of equal magnitude share a
 * rank.
 */
fun starFactSheet(
    star: StarRecord,
    catalog: List<StarRecord>,
    names: Map<Int, String>,
): FactSheet {
    require(star in catalog) { "HIP ${star.id} is not this catalog's record" }
    val rank = catalog.count { it.magnitude < star.magnitude } + 1
    val name = names[star.id]
    val designation = HIP_PREFIX + star.id
    val facts = buildList {
        name?.let { add(Fact("name", it)) }
        add(Fact("designation", designation))
        add(Fact("magnitude", twoDecimals(star.magnitude.toDouble())))
        add(Fact("brightness rank", "$rank of ${catalog.size}"))
        add(Fact("color", colorName(star.colorIndex.toDouble())))
        add(Fact("right ascension", formatRightAscension(star.rightAscensionDegrees.toDouble())))
        add(Fact("declination", formatDeclination(star.declinationDegrees.toDouble())))
    }
    return FactSheet(GuideAnchor.Star(star.id), name ?: designation, facts)
}

/**
 * The fact sheet for the constellation [id]: one sheet per anchor. Its [parts] are the catalog
 * entries with that id, usually one; Serpens has two (Caput and Cauda), which share the anchor and
 * the one merged [figure], so its sheet is titled by the words their names share ("Serpens") and
 * lists both parts.
 */
fun constellationFactSheet(
    id: String,
    parts: List<Constellation>,
    figure: ConstellationFigure?,
): FactSheet {
    val own = parts.filter { it.id == id }
    require(own.isNotEmpty()) { "no catalog entry for constellation $id" }
    require(figure == null || figure.id == id) { "figure ${figure?.id} is not $id's" }
    val title = sharedWords(own.map { it.name })
    val facts = buildList {
        add(Fact("name", title))
        add(Fact("abbreviation", id))
        if (own.size == 1) {
            add(Fact("label position", positionOf(own.single())))
        } else {
            add(Fact("parts", own.joinToString(", ") { it.name }))
            add(Fact("label positions", own.joinToString("; ") { "${it.name} ${positionOf(it)}" }))
        }
        figure?.let { f -> add(Fact("figure lines", f.polylines.sumOf { it.size - 1 }.toString())) }
    }
    return FactSheet(GuideAnchor.Constellation(id), title, facts)
}

/**
 * The fact sheet for the deep-sky object [dso], titled by its name and catalog id ("Andromeda
 * (M31)"), or by the id alone when it has no name. Its magnitude keeps the catalog's one decimal.
 */
fun deepSkyFactSheet(dso: DeepSkyObject): FactSheet {
    val facts = buildList {
        dso.name?.let { add(Fact("name", it)) }
        add(Fact("designation", dso.id))
        dso.designation?.let { add(Fact("other designation", it)) }
        add(Fact("type", dso.kind.description))
        add(Fact("magnitude", tenths(dso.magnitude)))
        if (dso.sizeArcminutes.isNotEmpty()) {
            add(
                Fact(
                    "apparent size",
                    dso.sizeArcminutes.joinToString(" \u00d7 ") { arcminutes(it) },
                )
            )
        }
        dso.classification?.let { add(Fact("class", it)) }
        add(Fact("right ascension", formatRightAscension(dso.position.rightAscensionDegrees)))
        add(Fact("declination", formatDeclination(dso.position.declinationDegrees)))
    }
    val title = dso.name?.let { "$it (${dso.id})" } ?: dso.id
    return FactSheet(GuideAnchor.DeepSky(dso.id), title, facts)
}

/** An angle in arcminutes as `190′`, or `1.4′` when it is not a whole number. */
private fun arcminutes(value: Double): String {
    val text = tenths(value)
    return text.removeSuffix(".0") + "\u2032"
}

/** A constellation label's position, as `18h 36m 00.0s, +30° 00′ 00″`. */
private fun positionOf(part: Constellation): String =
    formatRightAscension(part.labelRaDegrees) + ", " + formatDeclination(part.labelDecDegrees)

/** The leading words all [names] share ("Serpens"), or the one name when there is only one. */
private fun sharedWords(names: List<String>): String {
    val words = names.map { it.split(' ') }
    val shared =
        words.first().indices.takeWhile { i -> words.all { it.getOrNull(i) == words.first()[i] } }
    return if (shared.isEmpty()) names.first()
    else words.first().take(shared.size).joinToString(" ")
}

/** [value] rounded to one decimal, as `3.4` or `-0.5`. */
private fun tenths(value: Double): String {
    val tenths = (value * TENTHS).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    val size = abs(tenths)
    return "$sign${size / TENTHS}.${size % TENTHS}"
}

/** [value] rounded to two decimals, as `0.03` or `-1.44`. */
internal fun twoDecimals(value: Double): String {
    val hundredths = (value * HUNDREDTHS).roundToLong()
    val sign = if (hundredths < 0) "-" else ""
    val size = abs(hundredths)
    return "$sign${size / HUNDREDTHS}.${(size % HUNDREDTHS).toString().padStart(2, '0')}"
}
