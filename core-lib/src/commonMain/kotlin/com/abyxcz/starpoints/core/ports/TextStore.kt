package com.abyxcz.starpoints.core.ports

/** Small-file text storage used by the app: saved points now, settings next. */
interface TextStore {
    /** Reads the named file, or null if it does not exist. */
    suspend fun read(name: String): String?

    /** Writes [text] to the named file, replacing any existing content. */
    suspend fun write(name: String, text: String)
}
