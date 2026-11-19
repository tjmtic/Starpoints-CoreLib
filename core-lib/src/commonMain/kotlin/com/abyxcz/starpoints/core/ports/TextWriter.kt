package com.abyxcz.starpoints.core.ports

/**
 * One call to a [TextWriter].
 *
 * @property prompt the user turn (the adapter applies the model's chat template).
 * @property maxTokens the longest reply allowed.
 * @property seed the same seed and request give the same reply.
 * @property system a system turn before [prompt], or none.
 * @property grammar a GBNF grammar (llama.cpp's format, rule `root`) the reply must match, or none.
 * @property temperature how varied the reply is: 0 picks the likeliest token every time; null is
 *   the adapter's default, varied enough that another seed writes something different.
 */
data class WriteRequest(
    val prompt: String,
    val maxTokens: Int,
    val seed: Int,
    val system: String? = null,
    val grammar: String? = null,
    val temperature: Float? = null,
)

/**
 * A language model that writes text from a prompt: the on-device model, behind an adapter in the
 * app. The core never asks it to compute anything: prompts carry the facts, and what comes back is
 * checked before it is used.
 */
interface TextWriter {
    /** The model's name, as the app shows it on what it wrote (e.g. "MiniCPM5-2B"). */
    val model: String

    /** The model's reply to [request]. */
    suspend fun write(request: WriteRequest): String
}
