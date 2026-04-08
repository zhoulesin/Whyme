package com.zhoulesin.whyme.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zhoulesin.whyme.data.local.dao.WordDao
import com.zhoulesin.whyme.data.local.entity.WordEntity
import com.zhoulesin.whyme.data.local.entity.WordJson
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

    // 词库文件列表
    private val wordFiles = listOf(
        "words/cet4.json",
        "words/CET6.json",
        "words/GaoZhong.json",
        "words/KaoYan.json",
        "words/TOEFL.json",
        "words/GRE.json"
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

            for (fileName in wordFiles) {
                try {
                    val wordsFromFile = loadWordsFromAsset(fileName)
                    words.addAll(wordsFromFile)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 去重（根据单词）
            val uniqueWords = words.distinctBy { it.word.lowercase() }

            // 批量插入
            wordDao.insertWords(uniqueWords)
        }
    }

    /**
     * 从assets加载词库文件
     */
    private fun loadWordsFromAsset(fileName: String): List<WordEntity> {
        return try {
            context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val type = object : TypeToken<List<WordJson>>() {}.type
                    val wordJsonList: List<WordJson> = gson.fromJson(reader, type)
                    wordJsonList.map { it.toWordEntity() }
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
    private fun WordJson.toWordEntity(): WordEntity {
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
            translation = translations.firstOrNull()?.translation ?: ""
        )
    }
}
