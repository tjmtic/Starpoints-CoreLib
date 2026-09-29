package com.abyxcz.starpoints.core.catalog

import kotlinx.serialization.json.Json

private val NAMES_JSON = Json { ignoreUnknownKeys = true }

fun parseStarNames(json: String): Map<Int, String> =
    NAMES_JSON.decodeFromString<Map<String, String>>(json)
        .mapNotNull { (key, name) -> key.toIntOrNull()?.let { id -> id to name } }
        .toMap()
