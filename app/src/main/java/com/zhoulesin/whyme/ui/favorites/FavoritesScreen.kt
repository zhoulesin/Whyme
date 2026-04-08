package com.zhoulesin.whyme.ui.favorites

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.ui.components.WordCard
import com.zhoulesin.whyme.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MarketingBlack)
        ) {
            // 搜索栏
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("搜索收藏的单词", color = TertiaryText) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TertiaryText)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearch() }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除", tint = TertiaryText)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = AccentViolet,
                    unfocusedBorderColor = BorderStandard,
                    cursorColor = AccentViolet
                )
            )

            // 单词列表
            if (uiState.favoriteWords.isEmpty() && !uiState.isLoading) {
                // 空状态
                EmptyFavoritesContent(
                    hasSearchQuery = uiState.searchQuery.isNotEmpty()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                        text = "共 ${uiState.favoriteWords.size} 个收藏",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TertiaryText,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    }

                    items(
                        items = uiState.favoriteWords,
                        key = { it.id }
                    ) { word ->
                        FavoriteWordItem(
                            word = word,
                            onToggleFavorite = { viewModel.toggleFavorite(word.id) },
                            onClick = { onNavigateToWordDetail(word.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteWordItem(
    word: Word,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryText
                    )
                    if (word.phonetic.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = word.phonetic,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TertiaryText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = word.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TertiaryText
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "取消收藏",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyFavoritesContent(
    hasSearchQuery: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = if (hasSearchQuery) Icons.Default.SearchOff else Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TertiaryText.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasSearchQuery) "未找到匹配的收藏" else "暂无收藏",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasSearchQuery) {
                    "尝试其他关键词搜索"
                } else {
                    "在学习过程中点击心形图标添加收藏"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
