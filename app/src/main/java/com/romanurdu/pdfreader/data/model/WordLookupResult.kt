package com.romanurdu.pdfreader.data.model

enum class LookupSource {
    LOCAL_DICTIONARY,
    LEMMA_MATCH,
    DATABASE_CACHE,
    ONLINE_LOOKUP,
    NOT_FOUND
}

/**
 * Result returned by the DictionaryEngine when looking up a word in sentence context
 */
data class WordLookupResult(
    val word: String,
    val matchedMeaning: String,
    val allDefinitions: List<DefinitionEntry> = emptyList(),
    val detectedPos: String? = null,
    val contextSentence: String? = null,
    val isFound: Boolean = true,
    val source: LookupSource = LookupSource.LOCAL_DICTIONARY,
    val note: String? = null
)
