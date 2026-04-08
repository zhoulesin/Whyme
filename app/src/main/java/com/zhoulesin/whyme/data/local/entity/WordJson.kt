package com.zhoulesin.whyme.data.local.entity

import com.google.gson.annotations.SerializedName

/**
 * 词库JSON数据模型
 */
data class WordJson(
    @SerializedName("word")
    val word: String? = null,
    @SerializedName("us")
    val usPhonetic: String? = null,
    @SerializedName("uk")
    val ukPhonetic: String? = null,
    @SerializedName("translations")
    val translations: List<Translation>? = null,
    @SerializedName("phrases")
    val phrases: List<Phrase>? = null,
    @SerializedName("sentences")
    val sentences: List<Sentence>? = null
)

data class Translation(
    @SerializedName("translation")
    val translation: String? = null,
    @SerializedName("type")
    val type: String? = null
)

data class Phrase(
    @SerializedName("phrase")
    val phrase: String? = null,
    @SerializedName("translation")
    val translation: String? = null
)

data class Sentence(
    @SerializedName("sentence")
    val sentence: String? = null,
    @SerializedName("translation")
    val translation: String? = null
)
