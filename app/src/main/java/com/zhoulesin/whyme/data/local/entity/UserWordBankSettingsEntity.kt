package com.zhoulesin.whyme.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_word_bank_settings")
data class UserWordBankSettingsEntity(
    @PrimaryKey
    val userId: String = "",
    val currentLevel: String = "GAOZHONG",
    val enabledLevels: String = "[\"GAOZHONG\"]"
)
