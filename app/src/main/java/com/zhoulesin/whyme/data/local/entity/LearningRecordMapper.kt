package com.zhoulesin.whyme.data.local.entity

import com.zhoulesin.whyme.domain.model.LearningRecord
import java.time.LocalDate

/**
 * LearningRecord 实体与模型转换
 */
fun LearningRecordEntity.toDomain(): LearningRecord = LearningRecord(
    id = 0,
    date = LocalDate.ofEpochDay(date),
    wordsLearned = wordsLearned,
    wordsReviewed = wordsReviewed,
    correctCount = correctCount,
    durationSeconds = durationSeconds
)

fun LearningRecord.toEntity(): LearningRecordEntity = LearningRecordEntity(
    date = date.toEpochDay(),
    wordsLearned = wordsLearned,
    wordsReviewed = wordsReviewed,
    correctCount = correctCount,
    durationSeconds = durationSeconds
)

fun List<LearningRecordEntity>.toRecordDomainList(): List<LearningRecord> = map { it.toDomain() }
