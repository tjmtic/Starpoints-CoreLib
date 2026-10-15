package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.guide.WriterFixtures.plain
import kotlin.test.Test
import kotlin.test.assertEquals

/** [plain] wants 3 to 12 words. */
class FitToLengthTest {
    @Test
    fun aBodyWithinTheLimitIsUnchanged() {
        val body = "Vega is white. It is bright."
        assertEquals(body, fitToLength(body, plain))
    }

    @Test
    fun aLongBodyLosesWholeSentencesFromTheEnd() {
        val body = "Vega is a white star. Look for it overhead! It is part of a summer triangle."
        assertEquals("Vega is a white star. Look for it overhead!", fitToLength(body, plain))
    }

    @Test
    fun aDecimalPointEndsNoSentence() {
        val body = "Vega has magnitude 0.03 and is white. It sits in Lyra all summer long."
        assertEquals("Vega has magnitude 0.03 and is white.", fitToLength(body, plain))
    }

    @Test
    fun aReplyCutOffMidSentenceLosesTheFragment() {
        assertEquals("Vega is white.", fitToLength("Vega is white. It sits in the", plain))
    }

    @Test
    fun wrappingQuotesGoAndAClosingQuotationStays() {
        assertEquals("Vega is white.", cleanBody("  \u201cVega is white.\u201d \n"))
        assertEquals("Vega is white.", cleanBody("\"Vega is white.\""))
        val quoted = "Travellers call it \u201cthe lamp.\u201d"
        assertEquals(quoted, cleanBody(quoted))
    }

    @Test
    fun closingQuotesStayWithTheirSentence() {
        val body =
            "Travellers call it “the lamp.” It hangs high in summer and is easy to find tonight."
        assertEquals("Travellers call it “the lamp.”", fitToLength(body, plain))
    }

    @Test
    fun aBodyWhoseFirstSentenceIsTooLongIsLeftForTheCheckToRefuse() {
        val body =
            "Vega is a white star that travellers find overhead on warm summer nights. Truly."
        assertEquals(body, fitToLength(body, plain))
    }

    @Test
    fun aCutThatWouldFallShortOfTheMinimumIsNotMade() {
        val body = "Vega. Bright white star overhead on summer nights for every single traveller."
        assertEquals(body, fitToLength(body, plain))
    }
}
