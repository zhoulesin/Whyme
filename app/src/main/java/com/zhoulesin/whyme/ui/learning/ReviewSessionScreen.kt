package com.zhoulesin.whyme.ui.learning

import androidx.compose.runtime.Composable

@Composable
fun ReviewSessionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit
) {
    LearningSessionScreen(
        mode = "REVIEW",
        onNavigateBack = onNavigateBack,
        onNavigateToWordDetail = onNavigateToWordDetail
    )
}
