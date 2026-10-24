package com.romanurdu.pdfreader

import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DictionaryEngineTest {

    @Test
    fun testDictionaryJsonIntegrityAndFormat() {
        val jsonFile = File("src/main/assets/dictionary/roman_urdu_dictionary.json")
        val altFile = File("app/src/main/assets/dictionary/roman_urdu_dictionary.json")
        val targetFile = if (jsonFile.exists()) jsonFile else altFile

        assertTrue("Dictionary JSON asset file must exist on disk", targetFile.exists())

        val content = targetFile.readText(Charsets.UTF_8)
        val json = JSONObject(content)

        // Ensure key foundational words are present with Roman Urdu meanings
        val requiredWords = listOf(
            "book", "light", "bank", "right", "read", "abandon", "beautiful",
            "courage", "knowledge", "freedom", "wisdom", "journey", "peace"
        )

        for (word in requiredWords) {
            assertTrue("Dictionary must contain entry for '$word'", json.has(word))
            val entry = json.getJSONObject(word)
            assertTrue("Word '$word' must have a non-empty default meaning", entry.has("default"))
            val defaultMeaning = entry.getString("default")
            assertTrue("Default meaning for '$word' must not be blank", defaultMeaning.isNotBlank())
        }
    }

    @Test
    fun testWordSanitizationAndPunctuationTrimming() {
        val testCases = mapOf(
            "\"extraordinary,\"" to "extraordinary",
            "(courage)!" to "courage",
            "knowledge..." to "knowledge",
            "'wisdom'" to "wisdom",
            "well-known" to "well-known"
        )

        for ((input, expected) in testCases) {
            val sanitized = input.trim().replace(Regex("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$"), "")
            assertTrue("Expected '$expected' from input '$input' but got '$sanitized'", sanitized == expected)
        }
    }
}
