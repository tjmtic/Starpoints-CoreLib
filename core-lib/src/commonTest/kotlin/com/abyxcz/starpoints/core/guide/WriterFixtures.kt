package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.catalog.StarRecord

/** Shared test data for the on-device writer: Vega's fact sheet and two voices. */
internal object WriterFixtures {
    val vega = StarRecord(91262, 279.234711f, 38.783699f, 0.03f, -0.001f)
    val sirius = StarRecord(32349, 101.287201f, -16.716101f, -1.44f, 0.009f)
    val vegaSheet: FactSheet = starFactSheet(vega, listOf(sirius, vega), mapOf(91262 to "Vega"))

    val plain =
        WriterVoice(
            voice = GuideVoice(GuideVoice.PLAIN_ID, "Plain", fiction = false),
            summary = "Clear notes.",
            rules = listOf("Say only what the fact sheet says.", "One paragraph."),
            minWords = 3,
            maxWords = 12,
        )

    val guide =
        WriterVoice(
            voice = GuideVoice(GuideVoice.GUIDE_ID, "The Guide", fiction = true),
            summary = "Dry and warm.",
            rules = listOf("This is fiction."),
            minWords = 3,
            maxWords = 12,
            denyPhrases = listOf("don't panic"),
        )

    const val TEMPLATE =
        "{{voice.name}}: {{voice.summary}}\n{{voice.rules}}\n" +
            "{{length.min}}-{{length.max}} words about {{subject}} ({{anchor}}).\n{{facts}}"
}
