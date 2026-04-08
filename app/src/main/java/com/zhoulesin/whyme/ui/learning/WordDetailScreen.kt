package com.zhoulesin.whyme.ui.learning

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.ui.components.WordCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    wordId: Long,
    onNavigateBack: () -> Unit,
    viewModel: LearningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isFlipped by remember { mutableStateOf(false) }

    val word = remember(uiState.wordsToLearn, uiState.wordsForReview, wordId) {
        (uiState.wordsToLearn + uiState.wordsForReview).find { it.id == wordId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("单词详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    word?.let {
                        IconButton(onClick = { viewModel.toggleFavorite(wordId) }) {
                            Icon(
                                imageVector = if (it.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "收藏",
                                tint = if (it.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            word?.let {
                WordCard(
                    word = it,
                    isFlipped = isFlipped,
                    onFlip = { isFlipped = !isFlipped },
                    onFavoriteClick = { viewModel.toggleFavorite(wordId) },
                    onSpeakClick = { /* TODO: TTS */ }
                )
            } ?: run {
                Text(
                    text = "单词不存在",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
