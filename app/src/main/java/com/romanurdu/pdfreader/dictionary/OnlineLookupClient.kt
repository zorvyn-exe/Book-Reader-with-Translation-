package com.romanurdu.pdfreader.dictionary

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.romanurdu.pdfreader.data.model.DefinitionEntry
import com.romanurdu.pdfreader.data.model.WordLookupResult
import com.romanurdu.pdfreader.data.model.LookupSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/**
 * Free Online Lookup Client for words missing from offline dictionary.
 * Does not require any paid API keys.
 */
object OnlineLookupClient {

    /**
     * Looks up word online using free public endpoints
     */
    suspend fun lookupWordOnline(rawWord: String): WordLookupResult = withContext(Dispatchers.IO) {
        val word = rawWord.trim().lowercase(Locale.ROOT)
        if (word.isBlank()) {
            return@withContext WordLookupResult(
                word = rawWord,
                matchedMeaning = "No word specified",
                isFound = false,
                source = LookupSource.NOT_FOUND
            )
        }

        try {
            // 1. Try free public translation API to get Urdu translation
            val urduMeaning = fetchTranslationToUrdu(word)
            if (!urduMeaning.isNullOrBlank()) {
                val romanUrdu = transliterateUrduToRoman(urduMeaning)
                return@withContext WordLookupResult(
                    word = rawWord,
                    matchedMeaning = romanUrdu,
                    allDefinitions = listOf(
                        DefinitionEntry(
                            pos = "word",
                            romanUrdu = romanUrdu,
                            english = "Urdu: $urduMeaning"
                        )
                    ),
                    isFound = true,
                    source = LookupSource.ONLINE_LOOKUP,
                    note = "Fetched online & cached"
                )
            }

            // 2. Fallback to Free Dictionary API for English definition
            val englishDef = fetchFreeDictionaryDefinition(word)
            if (!englishDef.isNullOrBlank()) {
                return@withContext WordLookupResult(
                    word = rawWord,
                    matchedMeaning = "(English) $englishDef",
                    allDefinitions = listOf(
                        DefinitionEntry(
                            pos = "general",
                            romanUrdu = "(Eng) $englishDef",
                            english = englishDef
                        )
                    ),
                    isFound = true,
                    source = LookupSource.ONLINE_LOOKUP,
                    note = "Definition from Free Dictionary API"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext WordLookupResult(
            word = rawWord,
            matchedMeaning = "Meaning not found online. Please check network connection.",
            isFound = false,
            source = LookupSource.NOT_FOUND,
            note = "Offline or word unavailable"
        )
    }

    /**
     * Query free MyMemory translation endpoint (no auth/key required for reading lookups)
     */
    private fun fetchTranslationToUrdu(word: String): String? {
        var connection: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(word, "UTF-8")
            val endpoint = "https://api.mymemory.translated.net/get?q=$encoded&langpair=en|ur"
            val url = URL(endpoint)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.setRequestProperty("User-Agent", "RomanUrduPDFReader/1.0")

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val json = JsonParser.parseString(response).asJsonObject
                val responseData = json.getAsJsonObject("responseData")
                val translatedText = responseData?.get("translatedText")?.asString
                if (!translatedText.isNullOrBlank() && translatedText != word) {
                    return translatedText.trim()
                }
            }
        } catch (e: Exception) {
            // Ignore and fallback
        } finally {
            connection?.disconnect()
        }
        return null
    }

    /**
     * Query Free Dictionary API (https://api.dictionaryapi.dev)
     */
    private fun fetchFreeDictionaryDefinition(word: String): String? {
        var connection: HttpURLConnection? = null
        try {
            val encoded = URLEncoder.encode(word, "UTF-8")
            val endpoint = "https://api.dictionaryapi.dev/api/v2/entries/en/$encoded"
            val url = URL(endpoint)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 4000
            connection.readTimeout = 4000

            if (connection.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                val jsonArr = JsonParser.parseString(response).asJsonArray
                if (jsonArr.size() > 0) {
                    val entry = jsonArr[0].asJsonObject
                    val meanings = entry.getAsJsonArray("meanings")
                    if (meanings != null && meanings.size() > 0) {
                        val firstMeaning = meanings[0].asJsonObject
                        val defs = firstMeaning.getAsJsonArray("definitions")
                        if (defs != null && defs.size() > 0) {
                            return defs[0].asJsonObject.get("definition")?.asString
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        } finally {
            connection?.disconnect()
        }
        return null
    }

    /**
     * Converts Urdu script characters to intuitive Roman Urdu phonetic letters
     */
    fun transliterateUrduToRoman(urduText: String): String {
        // Direct common translations if present
        val cleaned = urduText.trim()
        val charMap = mapOf(
            'ا' to "a", 'آ' to "aa", 'ب' to "b", 'پ' to "p", 'ت' to "t",
            'ٹ' to "t", 'ث' to "s", 'ج' to "j", 'چ' to "ch", 'ح' to "h",
            'خ' to "kh", 'د' to "d", 'ڈ' to "d", 'ذ' to "z", 'ر' to "r",
            'ڑ' to "r", 'ز' to "z", 'ژ' to "zh", 'س' to "s", 'ش' to "sh",
            'ص' to "s", 'ض' to "z", 'ط' to "t", 'ظ' to "z", 'ع' to "a",
            'غ' to "gh", 'ف' to "f", 'ق' to "q", 'ک' to "k", 'گ' to "g",
            'ل' to "l", 'م' to "m", 'ن' to "n", 'ں' to "n", 'و' to "o",
            'ہ' to "h", 'ھ' to "h", 'ء' to "", 'ی' to "i", 'ے' to "e"
        )

        val sb = StringBuilder()
        var hasUrduChars = false
        for (ch in cleaned) {
            if (charMap.containsKey(ch)) {
                sb.append(charMap[ch])
                hasUrduChars = true
            } else {
                sb.append(ch)
            }
        }

        val romanized = sb.toString()
            .replace("  ", " ")
            .trim()

        return if (hasUrduChars) "$romanized ($cleaned)" else cleaned
    }
}
