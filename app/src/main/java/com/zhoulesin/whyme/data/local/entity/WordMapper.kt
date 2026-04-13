package com.zhoulesin.whyme.data.local.entity

import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.domain.model.WordLevel
import java.time.LocalDate

fun WordEntity.toDomain(): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    exampleTranslation = exampleTranslation,
    translation = translation,
    wordBank = wordBank,
    level = WordLevel.fromName(level)
)

fun WordEntity.toDomain(progress: UserWordProgressEntity?): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    exampleTranslation = exampleTranslation,
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

fun WordEntity.toDomain(
    progress: UserWordProgressEntity?,
    isFavorite: Boolean
): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    exampleTranslation = exampleTranslation,
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

fun Word.toEntity(): WordEntity = WordEntity(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    exampleTranslation = exampleTranslation,
    translation = translation,
    wordBank = wordBank,
    level = level.name
)

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
