package com.zhoulesin.whyme.data.local.entity

import com.google.gson.annotations.SerializedName

/**
 * 词库JSON数据模型
 */
data class WordJson(
    @SerializedName("word")
    val word: String,
    @SerializedName("us")
    val usPhonetic: String = "",
    @SerializedName("uk")
    val ukPhonetic: String = "",
    @SerializedName("translations")
    val translations: List<Translation> = emptyList(),
    @SerializedName("phrases")
    val phrases: List<Phrase> = emptyList(),
    @SerializedName("sentences")
    val sentences: List<Sentence> = emptyList()
)

data class Translation(
    @SerializedName("translation")
    val translation: String,
    @SerializedName("type")
    val type: String = ""
)

data class Phrase(
    @SerializedName("phrase")
    val phrase: String,
    @SerializedName("translation")
    val translation: String
)

data class Sentence(
    @SerializedName("sentence")
    val sentence: String,
    @SerializedName("translation")
    val translation: String
)
