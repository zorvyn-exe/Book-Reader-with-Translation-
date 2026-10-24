package com.romanurdu.pdfreader.data.model

import com.google.gson.annotations.SerializedName

/**
 * Single definition branch with Part of Speech and Roman Urdu meaning
 */
data class DefinitionEntry(
    @SerializedName("pos")
    val pos: String,
    @SerializedName("roman")
    val romanUrdu: String,
    @SerializedName("english")
    val english: String = "",
    @SerializedName("keywords")
    val keywords: List<String> = emptyList()
)

/**
 * Root dictionary word entry from JSON
 */
data class DictionaryEntry(
    @SerializedName("default")
    val defaultMeaning: String,
    @SerializedName("definitions")
    val definitions: List<DefinitionEntry> = emptyList()
)
