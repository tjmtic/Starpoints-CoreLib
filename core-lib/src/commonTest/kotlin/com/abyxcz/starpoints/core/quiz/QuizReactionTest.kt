package com.abyxcz.starpoints.core.quiz

import com.abyxcz.starpoints.core.guide.GuideVoice
import com.abyxcz.starpoints.core.guide.WriterVoice
import com.abyxcz.starpoints.core.ports.TextWriter
import com.abyxcz.starpoints.core.ports.WriteRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class QuizReactionTest {
    private val question =
        QuizQuestion(
            starId = 91262,
            starName = "Vega",
            kind = QuizKind.ABOVE_HORIZON,
            text = "Is Vega above the horizon right now?",
            choices = listOf("Yes", "No"),
            answer = 0,
            fact = "Vega is 58° above the horizon right now.",
        )
    private val voice =
        WriterVoice(
            voice = GuideVoice(GuideVoice.GUIDE_ID, "The Guide", fiction = true),
            summary = "Dry and warm.",
            rules = emptyList(),
            minWords = 3,
            maxWords = 20,
            language = "en",
        )
    private val template = "{{star}}|{{question}}|{{chosen}}|{{answer}}|{{result}}|{{fact}}"

    private class Scripted(private val replies: List<String>) : TextWriter {
        override val model = "Test"
        val asked = mutableListOf<WriteRequest>()

        override suspend fun write(request: WriteRequest): String {
            asked += request
            return replies[minOf(request.seed, replies.size) - 1]
        }
    }

    @Test
    fun thePromptCarriesTheQuestionTheAnswerAndTheResult() {
        assertEquals(
            "Vega|Is Vega above the horizon right now?|No|Yes|wrong|Vega is 58° above the horizon right now.",
            reactionPrompt(template, question, chosen = 1),
        )
        assertEquals("right", reactionPrompt("{{result}}", question, chosen = 0))
        assertFailsWith<IllegalArgumentException> { reactionPrompt("{{score}}", question, 0) }
    }

    @Test
    fun aReactionThatPassesTheChecksIsKept() = runTest {
        val writer =
            Scripted(listOf("Well spotted, traveller: Vega is up there, 58° and smug about it."))
        val reaction = QuizReactor(writer, template, voice).react(question, chosen = 0)
        assertEquals("Well spotted, traveller: Vega is up there, 58° and smug about it.", reaction)
        assertEquals(40, writer.asked.single().maxTokens)
    }

    @Test
    fun aReactionWithAnInventedNumberIsWrittenAgain() = runTest {
        val writer =
            Scripted(
                listOf("Vega is 25 light years away, friend.", "Close, but Vega is up, friend.")
            )
        assertEquals(
            "Close, but Vega is up, friend.",
            QuizReactor(writer, template, voice).react(question, 1),
        )
        assertEquals(listOf(1, 2), writer.asked.map { it.seed })
    }

    @Test
    fun whenNoAttemptPassesThereIsNoReaction() = runTest {
        val writer = Scripted(listOf("Vega ist heute Abend sehr schön zu sehen."))
        assertNull(QuizReactor(writer, template, voice).react(question, 0))
        assertEquals(2, writer.asked.size)
    }

    @Test
    fun aChoiceOutsideTheQuestionIsRefused() = runTest {
        assertFailsWith<IllegalArgumentException> {
            QuizReactor(Scripted(listOf("x")), template, voice).react(question, chosen = 2)
        }
    }
}
