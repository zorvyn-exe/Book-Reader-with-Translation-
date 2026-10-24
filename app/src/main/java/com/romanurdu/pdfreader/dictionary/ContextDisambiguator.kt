package com.romanurdu.pdfreader.dictionary

import com.romanurdu.pdfreader.data.model.DefinitionEntry
import com.romanurdu.pdfreader.data.model.DictionaryEntry
import java.util.Locale

/**
 * Uses surrounding sentence context, keywords, and part-of-speech heuristics
 * to select the most appropriate Roman Urdu meaning.
 */
object ContextDisambiguator {

    private val VERB_INDICATORS_BEFORE = setOf(
        "to", "can", "could", "will", "would", "shall", "should", "may", "might", "must",
        "did", "does", "do", "didn't", "doesn't", "don't", "please", "let's",
        "i", "he", "she", "we", "they", "you", "who"
    )

    private val NOUN_INDICATORS_BEFORE = setOf(
        "a", "an", "the", "this", "that", "these", "those",
        "my", "your", "his", "her", "our", "their", "its",
        "some", "any", "each", "every", "many", "much", "several"
    )

    private val PREPOSITIONS = setOf(
        "in", "on", "at", "by", "with", "about", "against", "between",
        "into", "through", "during", "before", "after", "above", "below",
        "from", "up", "down", "off", "over", "under"
    )

    data class DisambiguationResult(
        val primaryMeaning: String,
        val detectedPos: String?,
        val confidenceScore: Int,
        val explanation: String
    )

    /**
     * Disambiguate a dictionary entry using the surrounding sentence context
     */
    fun disambiguate(
        entry: DictionaryEntry,
        sentence: String?,
        targetWord: String
    ): DisambiguationResult {
        // If there's only 1 definition or no sentence context, return default
        if (entry.definitions.isEmpty() || sentence.isNullOrBlank()) {
            val first = entry.definitions.firstOrNull()
            return DisambiguationResult(
                primaryMeaning = entry.defaultMeaning,
                detectedPos = first?.pos,
                confidenceScore = 0,
                explanation = "Default dictionary meaning"
            )
        }

        if (entry.definitions.size == 1) {
            val def = entry.definitions[0]
            return DisambiguationResult(
                primaryMeaning = def.romanUrdu,
                detectedPos = def.pos,
                confidenceScore = 10,
                explanation = "Unique definition"
            )
        }

        val cleanSentence = sentence.lowercase(Locale.ROOT)
        val tokens = cleanSentence.split(Regex("[^a-zA-Z0-9'-]+")).filter { it.isNotBlank() }
        val targetLower = targetWord.lowercase(Locale.ROOT).trim()

        val targetIndex = tokens.indexOfFirst { it == targetLower || it.startsWith(targetLower) }

        // Score each candidate definition
        var bestDefinition: DefinitionEntry = entry.definitions[0]
        var highestScore = -1
        var bestExplanation = "Contextual match"

        for (def in entry.definitions) {
            var score = 0
            val matchedKeywords = mutableListOf<String>()

            // 1. Keyword semantic scoring (high weight)
            for (kw in def.keywords) {
                val kwLower = kw.lowercase(Locale.ROOT)
                if (cleanSentence.contains(kwLower)) {
                    score += 5
                    matchedKeywords.add(kw)
                }
            }

            // 2. Syntactic POS heuristics
            if (targetIndex >= 0) {
                val prevToken = if (targetIndex > 0) tokens[targetIndex - 1] else null
                val nextToken = if (targetIndex < tokens.size - 1) tokens[targetIndex + 1] else null

                when (def.pos.lowercase(Locale.ROOT)) {
                    "verb" -> {
                        if (prevToken != null && VERB_INDICATORS_BEFORE.contains(prevToken)) {
                            score += 4
                        }
                        if (prevToken == "to") {
                            score += 6 // Infinitive 'to book', 'to light'
                        }
                    }
                    "noun" -> {
                        if (prevToken != null && NOUN_INDICATORS_BEFORE.contains(prevToken)) {
                            // If followed by verb or at end, strong noun
                            score += 4
                        }
                        if (prevToken != null && PREPOSITIONS.contains(prevToken)) {
                            score += 3
                        }
                    }
                    "adjective" -> {
                        if (prevToken != null && NOUN_INDICATORS_BEFORE.contains(prevToken) && nextToken != null) {
                            // "the light bag" -> adjective
                            score += 5
                        }
                        if (nextToken != null && !VERB_INDICATORS_BEFORE.contains(nextToken)) {
                            score += 2
                        }
                    }
                }
            }

            if (score > highestScore) {
                highestScore = score
                bestDefinition = def
                bestExplanation = if (matchedKeywords.isNotEmpty()) {
                    "Context match: [${matchedKeywords.joinToString(", ")}]"
                } else if (highestScore > 0) {
                    "Grammar context: [${def.pos.replaceFirstChar { it.uppercase() }}]"
                } else {
                    "Common meaning"
                }
            }
        }

        val prefix = bestDefinition.pos.let { "[${it.replaceFirstChar { c -> c.uppercase() }}] " }
        return DisambiguationResult(
            primaryMeaning = prefix + bestDefinition.romanUrdu,
            detectedPos = bestDefinition.pos,
            confidenceScore = highestScore,
            explanation = bestExplanation
        )
    }
}
