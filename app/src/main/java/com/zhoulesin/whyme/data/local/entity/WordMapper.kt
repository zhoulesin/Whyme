package com.zhoulesin.whyme.data.local.entity

import com.zhoulesin.whyme.domain.model.Word
import java.time.LocalDate

/**
 * Word 实体与模型转换
 */
fun WordEntity.toDomain(): Word = Word(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    translation = translation,
    masteryLevel = masteryLevel,
    isFavorite = isFavorite,
    isLearned = isLearned,
    isNew = isNew,
    nextReviewDate = nextReviewDate?.let { LocalDate.ofEpochDay(it) },
    reviewCount = reviewCount,
    correctCount = correctCount,
    wordBank = wordBank
)

fun Word.toEntity(): WordEntity = WordEntity(
    id = id,
    word = word,
    phonetic = phonetic,
    definition = definition,
    example = example,
    translation = translation,
    masteryLevel = masteryLevel,
    isFavorite = isFavorite,
    isLearned = isLearned,
    isNew = isNew,
    nextReviewDate = nextReviewDate?.toEpochDay(),
    reviewCount = reviewCount,
    correctCount = correctCount,
    wordBank = wordBank
)

fun List<WordEntity>.toDomainList(): List<Word> = map { it.toDomain() }
