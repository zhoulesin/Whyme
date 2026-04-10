package com.zhoulesin.whyme.domain.model

enum class WordLevel(
    val displayName: String,
    val shortName: String,
    val description: String,
    val order: Int
) {
    GAOZHONG("高中词汇", "高中", "高中阶段必考词汇", 1),
    CET4("四级词汇", "四级", "大学英语四级词汇", 2),
    CET6("六级词汇", "六级", "大学英语六级词汇", 3),
    KAOYAN("考研词汇", "考研", "研究生入学考试词汇", 4),
    TOEFL("托福词汇", "托福", "托福高频词汇", 5),
    GRE("GRE词汇", "GRE", "GRE 词汇", 6);

    companion object {
        val DEFAULT = GAOZHONG

        fun fromName(name: String): WordLevel {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: DEFAULT
        }
    }
}

data class UserWordBankSettings(
    val id: Long = 0,
    val currentLevel: WordLevel = WordLevel.DEFAULT,
    val enabledLevels: Set<WordLevel> = setOf(WordLevel.DEFAULT)
)

data class LevelProgress(
    val level: WordLevel,
    val totalWords: Int = 0,
    val learnedWords: Int = 0,
    val masteredWords: Int = 0,
    val lastStudyDate: java.time.LocalDate? = null
) {
    val learningProgress: Float
        get() = if (totalWords > 0) learnedWords.toFloat() / totalWords else 0f

    val masteryProgress: Float
        get() = if (totalWords > 0) masteredWords.toFloat() / totalWords else 0f

    val isCompleted: Boolean
        get() = totalWords > 0 && learnedWords >= totalWords
}
