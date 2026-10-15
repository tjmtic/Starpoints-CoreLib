package com.abyxcz.starpoints.core.guide

import com.abyxcz.starpoints.core.guide.WriterFixtures.guide
import com.abyxcz.starpoints.core.guide.WriterFixtures.plain
import com.abyxcz.starpoints.core.guide.WriterFixtures.vegaSheet
import kotlin.test.Test
import kotlin.test.assertEquals

/** The minus, prime and double-prime signs are written as escapes (−, ′, ″). */
class EntryChecksTest {
    @Test
    fun numbersAreReadWithTheirSignsAndWithoutSeparators() {
        assertEquals(setOf("41411"), numbersIn("41,411 stars"))
        assertEquals(setOf("38"), numbersIn("+38° and 38"))
        assertEquals(setOf("1"), numbersIn("01″"))
        assertEquals(setOf("-1.44"), numbersIn("−1.44 or -1.44"))
        assertEquals(setOf("5", "10"), numbersIn("5-10"))
        assertEquals(setOf("0"), numbersIn("-0"))
        assertEquals(setOf("5.3", "1.4", "0"), numbersIn("05.30s, 1.40 and 0.0"))
        assertEquals(emptySet(), numbersIn("forty-two"))
    }

    @Test
    fun aGoodEntryPasses() {
        assertEquals(
            emptyList(),
            checkEntry("Vega shines at magnitude 0.03 tonight.", plain, vegaSheet),
        )
    }

    @Test
    fun aNumberTheFactSheetDoesNotStateIsCaught() {
        assertEquals(
            listOf("states [25], which the fact sheet does not"),
            checkEntry("Vega is 25 light years away.", plain, vegaSheet),
        )
    }

    @Test
    fun aTopicMayStateNoNumbers() {
        assertEquals(
            listOf("states [3], which the fact sheet does not"),
            checkEntry("The tea nebula serves 3 kinds.", plain, null),
        )
    }

    @Test
    fun anyNumbersPassWhenTheVoiceAllowsThem() {
        val loose = plain.copy(numbers = NumberRule.ANY)
        assertEquals(emptyList(), checkEntry("Vega is 25 light years away.", loose, vegaSheet))
    }

    @Test
    fun lengthIsCountedInWords() {
        assertEquals(
            listOf("2 words; plain wants 3 to 12"),
            checkEntry("Vega shines.", plain, vegaSheet),
        )
        assertEquals(
            listOf("13 words; plain wants 3 to 12"),
            checkEntry(
                "one two three four five six seven eight nine ten eleven twelve thirteen.",
                plain,
                null,
            ),
        )
    }

    @Test
    fun aPaddedFactSheetNumberMayBeWrittenPlainly() {
        val sheet =
            FactSheet(
                GuideAnchor.Star(1),
                "Test",
                listOf(Fact("ra", "05h 05m 05.30s"), Fact("mag", "1.40")),
            )
        assertEquals(emptyList(), checkEntry("It reads 5.3 seconds and 1.4 bright.", plain, sheet))
    }

    @Test
    fun aBodyCutOffMidSentenceIsCaught() {
        assertEquals(
            listOf("does not end a sentence"),
            checkEntry("Vega shines white in the constellation of", plain, vegaSheet),
        )
        assertEquals(
            emptyList(),
            checkEntry("Travellers call Vega \u201cthe lamp.\u201d", plain, null),
        )
    }

    @Test
    fun aDeniedPhraseIsCaughtWhateverItsCaseQuotesOrLineBreaks() {
        assertEquals(
            listOf("uses the denied phrase 'don't panic'"),
            checkEntry("Travellers say DON’T\npanic here, warmly.", guide, vegaSheet),
        )
    }
}
