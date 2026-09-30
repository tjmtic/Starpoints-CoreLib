package com.abyxcz.starpoints.core.presenter

import com.abyxcz.starpoints.core.catalog.Constellation

sealed interface SearchResult

data class StarResult(val id: Int, val name: String) : SearchResult

data class ConstellationResult(val id: String, val name: String) : SearchResult

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

    return (starMatches + constellationMatches)
        .filter { it.first != Int.MAX_VALUE }
        .sortedWith(compareBy({ it.first }, { it.second.lowercase() }))
        .take(limit)
        .map { it.third }
}
