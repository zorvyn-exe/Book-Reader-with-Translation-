package com.romanurdu.pdfreader.dictionary

import java.util.Locale

/**
 * Lightweight English Lemmatizer to reduce inflected word forms
 * (e.g., 'running' -> 'run', 'books' -> 'book', 'cried' -> 'cry')
 */
object Lemmatizer {

    private val IRREGULAR_VERBS = mapOf(
        "went" to "go", "gone" to "go",
        "came" to "come",
        "saw" to "see", "seen" to "see",
        "took" to "take", "taken" to "take",
        "gave" to "give", "given" to "give",
        "knew" to "know", "known" to "know",
        "thought" to "think",
        "felt" to "feel",
        "held" to "hold",
        "wrote" to "write", "written" to "write",
        "spoke" to "speak", "spoken" to "speak",
        "broke" to "break", "broken" to "break",
        "chose" to "choose", "chosen" to "choose",
        "brought" to "bring",
        "bought" to "buy",
        "taught" to "teach",
        "caught" to "catch",
        "built" to "build",
        "sent" to "send",
        "spent" to "spend",
        "led" to "lead",
        "read" to "read",
        "understood" to "understand",
        "lost" to "lose",
        "won" to "win",
        "paid" to "pay",
        "met" to "meet",
        "stood" to "stand",
        "sat" to "sit",
        "ran" to "run",
        "heard" to "hear"
    )

    private val IRREGULAR_NOUNS = mapOf(
        "children" to "child",
        "men" to "man",
        "women" to "woman",
        "feet" to "foot",
        "teeth" to "tooth",
        "mice" to "mouse",
        "people" to "person"
    )

    /**
     * Returns a prioritized list of candidate root forms for a word
     */
    fun getCandidateLemmas(rawWord: String): List<String> {
        val word = rawWord.lowercase(Locale.ROOT).trim()
        val candidates = linkedSetOf<String>()
        candidates.add(word)

        // Check irregular forms first
        IRREGULAR_VERBS[word]?.let { candidates.add(it) }
        IRREGULAR_NOUNS[word]?.let { candidates.add(it) }

        // Plural / 3rd person singular -ies (e.g. 'cries' -> 'cry', 'cities' -> 'city')
        if (word.endsWith("ies") && word.length > 4) {
            candidates.add(word.substring(0, word.length - 3) + "y")
        }

        // Past tense -ied (e.g. 'cried' -> 'cry')
        if (word.endsWith("ied") && word.length > 4) {
            candidates.add(word.substring(0, word.length - 3) + "y")
        }

        // Continuous form -ing
        if (word.endsWith("ing") && word.length > 4) {
            val base = word.substring(0, word.length - 3)
            candidates.add(base) // e.g. 'asking' -> 'ask'
            candidates.add(base + "e") // e.g. 'making' -> 'make'
            // Double consonant: 'running' -> 'run'
            if (base.length >= 3 && base[base.length - 1] == base[base.length - 2]) {
                candidates.add(base.substring(0, base.length - 1))
            }
        }

        // Past tense -ed
        if (word.endsWith("ed") && word.length > 3) {
            val base = word.substring(0, word.length - 2)
            candidates.add(base) // e.g. 'walked' -> 'walk'
            candidates.add(word.substring(0, word.length - 1)) // e.g. 'loved' -> 'love'
            // Double consonant: 'stopped' -> 'stop'
            if (base.length >= 3 && base[base.length - 1] == base[base.length - 2]) {
                candidates.add(base.substring(0, base.length - 1))
            }
        }

        // Plural -es
        if (word.endsWith("es") && word.length > 3) {
            candidates.add(word.substring(0, word.length - 2)) // e.g. 'watches' -> 'watch'
            candidates.add(word.substring(0, word.length - 1)) // e.g. 'clothes' -> 'clothe'
        }

        // Plural -s
        if (word.endsWith("s") && !word.endsWith("ss") && word.length > 2) {
            candidates.add(word.substring(0, word.length - 1)) // e.g. 'books' -> 'book'
        }

        // Adverb -ly (e.g. 'quickly' -> 'quick')
        if (word.endsWith("ly") && word.length > 4) {
            candidates.add(word.substring(0, word.length - 2))
            if (word.endsWith("ily")) {
                candidates.add(word.substring(0, word.length - 3) + "y") // e.g. 'happily' -> 'happy'
            }
        }

        return candidates.toList()
    }
}
