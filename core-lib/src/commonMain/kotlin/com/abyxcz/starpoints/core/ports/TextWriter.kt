package com.abyxcz.starpoints.core.ports

/**
 * A language model that writes text from a prompt: the on-device model, behind an adapter in the
 * app. The core never asks it to compute anything: prompts carry the facts, and what comes back is
 * checked before it is shown.
 */
interface TextWriter {
    /** The model's name, as the app shows it on what it wrote (e.g. "MiniCPM5-1B"). */
    val model: String

    /**
     * The model's reply to [prompt], one user turn (the adapter applies the model's chat template),
     * at most [maxTokens] long. The same [seed] and prompt give the same reply.
     */
    suspend fun write(prompt: String, maxTokens: Int, seed: Int): String
}
