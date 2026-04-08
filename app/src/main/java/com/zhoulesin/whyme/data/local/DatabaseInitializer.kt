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

    // 词库文件列表。
    // 同一个单词允许同时存在于多个级别中，因此这里不做跨词库去重。
    private val wordFiles = listOf(
        WordSource("words/GaoZhong.json", "高中词汇", WordLevel.GAOZHONG),
        WordSource("words/cet4.json", "CET4", WordLevel.CET4),
        WordSource("words/CET6.json", "CET6", WordLevel.CET6),
        WordSource("words/KaoYan.json", "考研词汇", WordLevel.KAOYAN),
        WordSource("words/TOEFL.json", "托福词汇", WordLevel.TOEFL),
        WordSource("words/GRE.json", "GRE词汇", WordLevel.GRE)
    )

    /**
     * 检查是否需要初始化词库
     */
    suspend fun shouldInitializeWordCount(context: Context, wordDao: WordDao): Boolean {
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
            wordDao.insertWords(words)
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
                    wordJsonList.map { it.toWordEntity(source) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 将WordJson转换为WordEntity
     */
    private fun WordJson.toWordEntity(source: WordSource): WordEntity {
        // 合并所有翻译
        val definition = translations.joinToString("; ") {
            if (it.type.isNotEmpty()) "${it.type}. ${it.translation}" else it.translation
        }

        // 取第一个例句
        val example = sentences.firstOrNull()?.let {
            "\"${it.sentence}\"\n${it.translation}"
        } ?: ""

        // 合并短语
        val phraseText = phrases.take(3).joinToString("; ") {
            "${it.phrase} - ${it.translation}"
        }

        // 音标（优先美式）
        val phonetic = if (usPhonetic.isNotEmpty()) usPhonetic else ukPhonetic

        return WordEntity(
            word = word.trim(),
            phonetic = phonetic,
            definition = definition,
            example = example,
            translation = translations.firstOrNull()?.translation ?: "",
            wordBank = source.wordBank,
            level = source.level.name
        )
    }
}
