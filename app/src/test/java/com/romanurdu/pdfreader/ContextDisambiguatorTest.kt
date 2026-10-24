package com.romanurdu.pdfreader

import com.romanurdu.pdfreader.data.model.DefinitionEntry
import com.romanurdu.pdfreader.data.model.DictionaryEntry
import com.romanurdu.pdfreader.dictionary.ContextDisambiguator
import org.junit.Assert.assertTrue
import org.junit.Test

class ContextDisambiguatorTest {

    private val bookEntry = DictionaryEntry(
        defaultMeaning = "Kitab / Pustak",
        definitions = listOf(
            DefinitionEntry(
                pos = "noun",
                romanUrdu = "Kitab / Pustak",
                english = "A printed work",
                keywords = listOf("read", "author", "page", "chapter", "novel", "library")
            ),
            DefinitionEntry(
                pos = "verb",
                romanUrdu = "Pehle se seat ya kamra mehfooz karna",
                english = "To reserve accommodation or ticket",
                keywords = listOf("hotel", "ticket", "room", "flight", "seat", "advance")
            )
        )
    )

    private val lightEntry = DictionaryEntry(
        defaultMeaning = "Roshni / Halka",
        definitions = listOf(
            DefinitionEntry(
                pos = "noun",
                romanUrdu = "Roshni / Noor",
                english = "Stimulates sight",
                keywords = listOf("dark", "sun", "lamp", "room", "bright", "candle")
            ),
            DefinitionEntry(
                pos = "adjective",
                romanUrdu = "Halka / Wazan mein kam",
                english = "Of little weight",
                keywords = listOf("weight", "heavy", "bag", "box", "feather")
            ),
            DefinitionEntry(
                pos = "verb",
                romanUrdu = "Jalana / Roshani karna",
                english = "To ignite",
                keywords = listOf("fire", "candle", "match", "flame")
            )
        )
    )

    private val bankEntry = DictionaryEntry(
        defaultMeaning = "Bank / Kinara",
        definitions = listOf(
            DefinitionEntry(
                pos = "noun",
                romanUrdu = "Darya ya jheel ka kinara",
                english = "Land along water",
                keywords = listOf("river", "water", "stream", "mud", "lake")
            ),
            DefinitionEntry(
                pos = "noun",
                romanUrdu = "Bank / Maali idara",
                english = "Financial institution",
                keywords = listOf("money", "account", "deposit", "cash", "loan")
            )
        )
    )

    @Test
    fun testBookAsVerbInHotelSentence() {
        val sentence = "I called the agent to book a room at the hotel in advance."
        val result = ContextDisambiguator.disambiguate(bookEntry, sentence, "book")

        assertTrue("Expected verb meaning for booking hotel", result.primaryMeaning.contains("mehfooz karna"))
        assertTrue("Expected detected POS to be verb", result.detectedPos.equals("verb", ignoreCase = true))
    }

    @Test
    fun testBookAsNounInReadingSentence() {
        val sentence = "He sat in the library reading an old history book all afternoon."
        val result = ContextDisambiguator.disambiguate(bookEntry, sentence, "book")

        assertTrue("Expected noun meaning for library book", result.primaryMeaning.contains("Kitab"))
        assertTrue("Expected detected POS to be noun", result.detectedPos.equals("noun", ignoreCase = true))
    }

    @Test
    fun testLightAsAdjectiveForWeight() {
        val sentence = "This suitcase is very light and easy to carry without heavy weight."
        val result = ContextDisambiguator.disambiguate(lightEntry, sentence, "light")

        assertTrue("Expected adjective meaning for weight", result.primaryMeaning.contains("Halka"))
    }

    @Test
    fun testLightAsNounInDarkRoom() {
        val sentence = "Please switch on the bright light in this dark room."
        val result = ContextDisambiguator.disambiguate(lightEntry, sentence, "light")

        assertTrue("Expected noun meaning for illumination", result.primaryMeaning.contains("Roshni"))
    }

    @Test
    fun testBankRiverVsMoneyDisambiguation() {
        val riverSentence = "They sat quietly by the river bank watching the clear water flow."
        val riverResult = ContextDisambiguator.disambiguate(bankEntry, riverSentence, "bank")
        assertTrue("Expected river bank meaning", riverResult.primaryMeaning.contains("kinara"))

        val moneySentence = "He went to deposit his monthly cash salary at the bank."
        val moneyResult = ContextDisambiguator.disambiguate(bankEntry, moneySentence, "bank")
        assertTrue("Expected financial bank meaning", moneyResult.primaryMeaning.contains("Maali idara"))
    }
}
