package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.Constellation
import com.abyxcz.starpoints.core.catalog.DeepSkyObject

sealed interface SearchResult

data class StarResult(val id: Int, val name: String) : SearchResult

data class ConstellationResult(val id: String, val name: String) : SearchResult

/**
 * A deep-sky object found by its catalog id ("M31"), its name ("Andromeda") or its other
 * designation ("NGC 224").
 *
 * @property id the catalog id.
 * @property name its name and id, as "Andromeda (M31)", or the id alone when it has no name.
 */
data class DeepSkyResult(val id: String, val name: String) : SearchResult

private const val RANK_EXACT = 0
private const val RANK_PREFIX = 1
private const val RANK_CONTAINS = 2

private fun rankOf(name: String, query: String): Int =
    when {
        name == query -> RANK_EXACT
        name.startsWith(query) -> RANK_PREFIX
        name.contains(query) -> RANK_CONTAINS
        else -> Int.MAX_VALUE
    }

fun search(
    query: String,
    names: Map<Int, String>,
    constellations: List<Constellation>,
    deepSky: List<DeepSkyObject> = emptyList(),
    limit: Int = 20,
): List<SearchResult> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()

    val starMatches =
        names.entries
            .filter { it.value.lowercase().contains(q) }
            .map { (id, name) -> Triple(rankOf(name.lowercase(), q), name, StarResult(id, name)) }

    val seen = mutableSetOf<String>()
    val constellationMatches =
        constellations
            .filter { seen.add(it.id) }
            .filter { it.name.lowercase().contains(q) }
            .map { c ->
                Triple(rankOf(c.name.lowercase(), q), c.name, ConstellationResult(c.id, c.name))
            }

    val deepSkyMatches = deepSky.mapNotNull { dso ->
        val label = dso.name?.let { "$it (${dso.id})" } ?: dso.id
        val rank =
            listOfNotNull(dso.id, dso.name, dso.designation).minOf { rankOf(it.lowercase(), q) }
        if (rank == Int.MAX_VALUE) null else Triple(rank, label, DeepSkyResult(dso.id, label))
    }

    return (starMatches + constellationMatches + deepSkyMatches)
        .filter { it.first != Int.MAX_VALUE }
        .sortedWith(compareBy({ it.first }, { it.second.lowercase() }))
        .take(limit)
        .map { it.third }
}
