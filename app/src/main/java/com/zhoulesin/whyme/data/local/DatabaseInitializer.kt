package com.zhoulesin.whyme.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.WordEntity
import com.zhoulesin.whyme.data.local.entity.WordJson
import com.zhoulesin.whyme.domain.model.WordLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

/**
 * 数据库初始化器 - 从assets加载词库
 * 新的初始化逻辑：只插入单词基础信息，不插入学习状态和收藏状态
 */
class DatabaseInitializer(
    private val context: Context
) {
    private val gson = Gson()

    private data class WordSource(
        val fileName: String,
        val wordBank: String,
        val level: WordLevel
    )

    // 词库文件列表（当前仅初始化 CET6 及以下）
    private val wordFiles = listOf(
        WordSource("words/GaoZhong.json", "高中词汇", WordLevel.GAOZHONG),
        WordSource("words/cet4.json", "CET4", WordLevel.CET4),
        WordSource("words/CET6.json", "CET6", WordLevel.CET6)
    )

    /**
     * 检查是否需要初始化词库
     */
    suspend fun shouldInitializeWordCount(wordDao: WordDao): Boolean {
        return withContext(Dispatchers.IO) {
            wordDao.getWordCount() == 0
        }
    }

    /**
     * 初始化词库数据
     */
    suspend fun initializeWordDatabase(wordDao: WordDao) {
        withContext(Dispatchers.IO) {
            val words = mutableListOf<WordEntity>()

            for (source in wordFiles) {
                try {
                    val wordsFromFile = loadWordsFromAsset(source)
                    words.addAll(wordsFromFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 批量插入
            if (words.isNotEmpty()) {
                wordDao.insertWords(words)
            }
        }
    }

    /**
     * 从assets加载词库文件
     */
    private fun loadWordsFromAsset(source: WordSource): List<WordEntity> {
        return try {
            context.assets.open(source.fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<List<WordJson>>() {}.type
                    val wordJsonList: List<WordJson> = gson.fromJson(reader, type)
                    println("DatabaseInitializer.loadWordsFromAsset: fileName=${source.fileName}, wordBank=${source.wordBank}, level=${source.level.name}, count=${wordJsonList.size}")
                    wordJsonList.mapNotNull { it.toWordEntityOrNull(source) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("DatabaseInitializer.loadWordsFromAsset: Error loading ${source.fileName}: ${e.message}")
            emptyList()
        }
    }

    /**
     * 将WordJson转换为WordEntity
     * 如果单词为空则返回null
     */
    private fun WordJson.toWordEntityOrNull(source: WordSource): WordEntity? {
        val wordText = word?.trim()
        if (wordText.isNullOrEmpty()) return null

        // 合并所有翻译
        val definition = translations?.joinToString("; ") {
            val type = it.type ?: ""
            val translation = it.translation ?: ""
            if (type.isNotEmpty()) "$type. $translation" else translation
        } ?: ""

        // 取第一个例句，拆分为英文原句和中文译文
        val example = sentences?.firstOrNull()?.sentence ?: ""
        val exampleTranslation = sentences?.firstOrNull()?.translation ?: ""

        // 合并短语
        val phraseText = phrases?.take(3)?.joinToString("; ") {
            val phrase = it.phrase ?: ""
            val translation = it.translation ?: ""
            "$phrase - $translation"
        } ?: ""

        // 音标（优先美式）
        val phonetic = if (usPhonetic?.isNotEmpty() == true) usPhonetic else ukPhonetic ?: ""

        // 获取第一个翻译
        val firstTranslation = translations?.firstOrNull()?.translation ?: ""

        return WordEntity(
            word = wordText,
            phonetic = phonetic,
            definition = definition,
            example = example,
            exampleTranslation = exampleTranslation,
            translation = firstTranslation,
            wordBank = source.wordBank,
            level = source.level.name
        )
    }
}
