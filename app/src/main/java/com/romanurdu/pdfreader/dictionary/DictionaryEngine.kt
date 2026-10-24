package com.romanurdu.pdfreader.dictionary

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.romanurdu.pdfreader.data.database.AppDatabase
import com.romanurdu.pdfreader.data.model.DefinitionEntry
import com.romanurdu.pdfreader.data.model.DictionaryCacheEntity
import com.romanurdu.pdfreader.data.model.DictionaryEntry
import com.romanurdu.pdfreader.data.model.LookupSource
import com.romanurdu.pdfreader.data.model.WordLookupResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.Locale

class DictionaryEngine(private val context: Context) {

    private val localDictionary = HashMap<String, DictionaryEntry>()
    private val database = AppDatabase.getInstance(context)
    private var isInitialized = false

    /**
     * Load the offline dictionary JSON from assets into memory
     */
    suspend fun initialize() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext

        try {
            val assetManager = context.assets
            val inputStream = assetManager.open("dictionary/roman_urdu_dictionary.json")
            val reader = InputStreamReader(inputStream, "UTF-8")

            val type = object : TypeToken<Map<String, DictionaryEntry>>() {}.type
            val parsed: Map<String, DictionaryEntry> = Gson().fromJson(reader, type)
            localDictionary.putAll(parsed)
            reader.close()
            inputStream.close()
            isInitialized = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Looks up an English word, applying context disambiguation from sentence
     */
    suspend fun lookupWord(rawWord: String, sentence: String? = null): WordLookupResult = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            initialize()
        }

        val cleanWord = rawWord.trim().replace(Regex("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$"), "")
        val lowerWord = cleanWord.lowercase(Locale.ROOT)

        if (lowerWord.isBlank()) {
            return@withContext WordLookupResult(
                word = rawWord,
                matchedMeaning = "Empty word",
                isFound = false,
                source = LookupSource.NOT_FOUND
            )
        }

        // 1. Direct match in local dictionary
        val directEntry = localDictionary[lowerWord]
        if (directEntry != null) {
            val disambiguated = ContextDisambiguator.disambiguate(directEntry, sentence, cleanWord)
            return@withContext WordLookupResult(
                word = cleanWord,
                matchedMeaning = disambiguated.primaryMeaning,
                allDefinitions = directEntry.definitions,
                detectedPos = disambiguated.detectedPos,
                contextSentence = sentence,
                isFound = true,
                source = LookupSource.LOCAL_DICTIONARY,
                note = disambiguated.explanation
            )
        }

        // 2. Lemmatized match (e.g., 'running' -> 'run', 'books' -> 'book')
        val candidateLemmas = Lemmatizer.getCandidateLemmas(lowerWord)
        for (lemma in candidateLemmas) {
            if (lemma == lowerWord) continue
            val lemmaEntry = localDictionary[lemma]
            if (lemmaEntry != null) {
                val disambiguated = ContextDisambiguator.disambiguate(lemmaEntry, sentence, cleanWord)
                return@withContext WordLookupResult(
                    word = cleanWord,
                    matchedMeaning = disambiguated.primaryMeaning,
                    allDefinitions = lemmaEntry.definitions,
                    detectedPos = disambiguated.detectedPos,
                    contextSentence = sentence,
                    isFound = true,
                    source = LookupSource.LEMMA_MATCH,
                    note = "Root form: '$lemma' (${disambiguated.explanation})"
                )
            }
        }

        // 3. Check persistent database cache (previously fetched words)
        val cached = database.dictionaryCacheDao().getCachedWord(lowerWord)
        if (cached != null) {
            return@withContext WordLookupResult(
                word = cleanWord,
                matchedMeaning = cached.romanUrduMeaning,
                allDefinitions = listOf(
                    DefinitionEntry(
                        pos = cached.pos.ifBlank { "word" },
                        romanUrdu = cached.romanUrduMeaning,
                        english = cached.englishDefinition
                    )
                ),
                detectedPos = cached.pos.ifBlank { null },
                contextSentence = sentence,
                isFound = true,
                source = LookupSource.DATABASE_CACHE,
                note = "Saved from previous lookup"
            )
        }

        // 4. Not found locally
        return@withContext WordLookupResult(
            word = cleanWord,
            matchedMeaning = "Word not found in offline dictionary.",
            allDefinitions = emptyList(),
            contextSentence = sentence,
            isFound = false,
            source = LookupSource.NOT_FOUND,
            note = "Tap 'Online Lookup' to find this word"
        )
    }

    /**
     * Perform online lookup and cache to Room database
     */
    suspend fun lookupOnline(rawWord: String): WordLookupResult = withContext(Dispatchers.IO) {
        val result = OnlineLookupClient.lookupWordOnline(rawWord)
        if (result.isFound) {
            // Save to cache
            database.dictionaryCacheDao().insertCache(
                DictionaryCacheEntity(
                    word = rawWord.lowercase(Locale.ROOT).trim(),
                    romanUrduMeaning = result.matchedMeaning,
                    englishDefinition = result.allDefinitions.firstOrNull()?.english ?: "",
                    pos = result.detectedPos ?: "word",
                    source = "ONLINE"
                )
            )
        }
        result
    }

    /**
     * Search dictionary locally for autocompletion or dictionary tab
     */
    suspend fun searchLocal(query: String): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        if (!isInitialized) initialize()

        val q = query.lowercase(Locale.ROOT).trim()
        if (q.isBlank()) return@withContext emptyList()

        val matches = mutableListOf<Pair<String, String>>()
        for ((word, entry) in localDictionary) {
            if (word.startsWith(q) || word.contains(q) || entry.defaultMeaning.lowercase(Locale.ROOT).contains(q)) {
                matches.add(word to entry.defaultMeaning)
                if (matches.size >= 50) break
            }
        }
        matches.sortedBy { if (it.first.startsWith(q)) 0 else 1 }
    }
}
