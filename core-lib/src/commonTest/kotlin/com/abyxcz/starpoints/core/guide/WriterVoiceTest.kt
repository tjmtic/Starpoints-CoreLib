package com.abyxcz.starpoints.core.guide

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WriterVoiceTest {
    private val plainFile =
        """{"id": "plain", "name": "Plain", "fiction": false, "summary": "Clear notes.",
           "rules": ["One paragraph.", "All ages."],
           "length": {"minWords": 30, "maxWords": 100},
           "checks": {"numbers": "fact-sheet-only", "denyPhrases": ["towel"]}}"""

    @Test
    fun aVoiceFileReadsIntoAWriterVoice() {
        val voice = parseWriterVoice(plainFile)
        assertEquals(GuideVoice("plain", "Plain", fiction = false), voice.voice)
        assertEquals("Clear notes.", voice.summary)
        assertEquals(listOf("One paragraph.", "All ages."), voice.rules)
        assertEquals(30, voice.minWords)
        assertEquals(100, voice.maxWords)
        assertEquals(NumberRule.FACT_SHEET_ONLY, voice.numbers)
        assertEquals(listOf("towel"), voice.denyPhrases)
    }

    @Test
    fun checksDefaultToFactSheetNumbersAndNoDeniedPhrases() {
        val voice =
            parseWriterVoice(
                """{"id": "guide", "name": "The Guide", "fiction": true, "summary": "s",
                   "rules": [], "length": {"minWords": 1, "maxWords": 2}}"""
            )
        assertEquals(NumberRule.FACT_SHEET_ONLY, voice.numbers)
        assertEquals(emptyList(), voice.denyPhrases)
    }

    @Test
    fun anyNumbersReads() {
        val voice = parseWriterVoice(plainFile.replace("fact-sheet-only", "any"))
        assertEquals(NumberRule.ANY, voice.numbers)
    }

    @Test
    fun anUnknownKeyOrABadLengthIsRefused() {
        assertFailsWith<IllegalArgumentException> {
            parseWriterVoice(plainFile.replace("\"summary\"", "\"tone\": \"x\", \"summary\""))
        }
        assertFailsWith<IllegalArgumentException> {
            parseWriterVoice(plainFile.replace("\"minWords\": 30", "\"minWords\": 0"))
        }
        assertFailsWith<IllegalArgumentException> {
            parseWriterVoice(plainFile.replace("\"minWords\": 30", "\"minWords\": 101"))
        }
    }
}
