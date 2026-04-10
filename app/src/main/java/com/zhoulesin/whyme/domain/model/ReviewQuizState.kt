import com.zhoulesin.whyme.domain.model.QuestionType
import com.zhoulesin.whyme.domain.model.QuizOption
import com.zhoulesin.whyme.domain.model.Word

/**
 * 复习状态
 */
sealed class ReviewState {
    data object Idle : ReviewState()

    data class Reviewing(
        val currentWord: Word,
        val index: Int,
        val total: Int,
        val startTime: Long = System.currentTimeMillis()
    ) : ReviewState()

    data class Completed(
        val reviewed: Int,
        val correct: Int,
        val accuracy: Float
    ) : ReviewState()

    data class Error(val message: String) : ReviewState()
}

/**
 * 测试状态
 */
sealed class QuizState {
    data object Idle : QuizState()

    data class Testing(
        val currentWord: Word,
        val questionType: QuestionType,
        val index: Int,
        val total: Int,
        val options: List<QuizOption> = emptyList()
    ) : QuizState()

    data class Result(
        val correctCount: Int,
        val totalCount: Int,
        val accuracy: Float
    ) : QuizState()

    data class Error(val message: String) : QuizState()
}