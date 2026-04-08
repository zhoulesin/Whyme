package com.zhoulesin.whyme.data.local.entity

import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import java.time.LocalDate

/**
 * Word 实体与模型转换
 * 新的转换逻辑：从多个表组合数据
 */

/**
 * 基础单词信息转换（不包含学习状态和收藏状态）
 */
fun WordEntity.toDomain(): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    translation = translation,
    wordBank = wordBank,
    level = WordLevel.fromName(level)
)

/**
 * 带学习状态的转换
 */
fun WordEntity.toDomain(progress: UserWordProgressEntity?): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    translation = translation,
    masteryLevel = progress?.masteryLevel ?: 0,
    isLearned = progress?.isLearned ?: false,
    isNew = progress?.isNew ?: true,
    nextReviewDate = progress?.nextReviewDate?.let { LocalDate.ofEpochDay(it) },
    reviewCount = progress?.reviewCount ?: 0,
    correctCount = progress?.correctCount ?: 0,
    wordBank = wordBank,
    level = WordLevel.fromName(level)
)

/**
 * 带学习状态和收藏状态的完整转换
 */
fun WordEntity.toDomain(
    progress: UserWordProgressEntity?,
    isFavorite: Boolean
): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    translation = translation,
    masteryLevel = progress?.masteryLevel ?: 0,
    isFavorite = isFavorite,
    isLearned = progress?.isLearned ?: false,
    isNew = progress?.isNew ?: true,
    nextReviewDate = progress?.nextReviewDate?.let { LocalDate.ofEpochDay(it) },
    reviewCount = progress?.reviewCount ?: 0,
    correctCount = progress?.correctCount ?: 0,
    wordBank = wordBank,
    level = WordLevel.fromName(level)
)

/**
 * 从 Domain 模型转换为 Entity（仅基础信息）
 */
fun Word.toEntity(): WordEntity = WordEntity(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    translation = translation,
    wordBank = wordBank,
    level = level.name
)

/**
 * 从 Domain 模型转换为学习进度 Entity
 */
fun Word.toProgressEntity(): UserWordProgressEntity = UserWordProgressEntity(
    wordId = id,
    masteryLevel = masteryLevel,
    isLearned = isLearned,
    isNew = isNew,
    nextReviewDate = nextReviewDate?.toEpochDay(),
    reviewCount = reviewCount,
    correctCount = correctCount
)

fun List<WordEntity>.toDomainList(): List<Word> = map { it.toDomain() }

fun List<WordEntity>.toDomainList(
    progressMap: Map<Long, UserWordProgressEntity?>,
    favoriteIds: Set<Long>
): List<Word> = map { entity ->
    entity.toDomain(
        progress = progressMap[entity.id],
        isFavorite = favoriteIds.contains(entity.id)
    )
}
