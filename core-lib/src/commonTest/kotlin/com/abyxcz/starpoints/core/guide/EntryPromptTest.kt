package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.guide.WriterFixtures.TEMPLATE
import com.abyxcz.starpoints.core.guide.WriterFixtures.plain
import com.abyxcz.starpoints.core.guide.WriterFixtures.vegaSheet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EntryPromptTest {
    @Test
    fun everyPlaceholderIsFilledFromTheVoiceAndTheFactSheet() {
        val prompt = renderEntryPrompt(TEMPLATE, plain, GuideAnchor.Star(91262), vegaSheet)
        val facts = vegaSheet.facts.joinToString("\n") { "- ${it.key}: ${it.value}" }
        assertEquals(
            "Plain: Clear notes.\n- Say only what the fact sheet says.\n- One paragraph.\n" +
                "3-12 words about Vega (star:91262).\n$facts",
            prompt,
        )
    }

    @Test
    fun aTopicHasNoFactsAndItsSlugAsTheSubject() {
        val prompt = renderEntryPrompt(TEMPLATE, plain, GuideAnchor.Topic("the-tea-nebula"), null)
        assertEquals(
            "Plain: Clear notes.\n- Say only what the fact sheet says.\n- One paragraph.\n" +
                "3-12 words about the tea nebula (topic:the-tea-nebula).\n$NO_FACTS",
            prompt,
        )
    }

    @Test
    fun spacesInsideAPlaceholderAreAllowed() {
        assertEquals("Vega", renderEntryPrompt("{{ subject }}", plain, vegaSheet.anchor, vegaSheet))
    }

    @Test
    fun anUnknownOrMalformedPlaceholderIsRefused() {
        assertFailsWith<IllegalArgumentException> {
            renderEntryPrompt("{{voice.tone}}", plain, vegaSheet.anchor, vegaSheet)
        }
        assertFailsWith<IllegalArgumentException> {
            renderEntryPrompt("{{subject", plain, vegaSheet.anchor, vegaSheet)
        }
    }
}
