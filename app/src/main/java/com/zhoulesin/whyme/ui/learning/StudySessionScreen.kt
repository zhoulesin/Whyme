package com.zhoulesin.whyme.ui.learning

import androidx.compose.runtime.Composable

@Composable
fun StudySessionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit
) {
    LearningSessionScreen(
        mode = "LEARN",
        onNavigateBack = onNavigateBack,
        onNavigateToWordDetail = onNavigateToWordDetail
    )
}
