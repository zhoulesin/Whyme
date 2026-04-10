package com.zhoulesin.whyme.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToWordDetail: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val resultsState by viewModel.searchResultsState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = MarketingBlack,
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = {
                            Text(
                                "搜索单词、释义...",
                                color = TertiaryText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = TertiaryText
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = "清除",
                                        tint = TertiaryText
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentViolet,
                            unfocusedBorderColor = BorderStandard,
                            cursorColor = AccentViolet,
                            focusedContainerColor = Level3Surface,
                            unfocusedContainerColor = Level3Surface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = TertiaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Level3Surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MarketingBlack)
        ) {
            when {
                !resultsState.hasSearched -> {
                    SearchHintContent()
                }
                resultsState.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BrandIndigo)
                    }
                }
                resultsState.results.isEmpty() -> {
                    EmptyResultContent(query = searchQuery)
                }
                else -> {
                    Text(
                        text = "找到 ${resultsState.results.size} 个结果",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TertiaryText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = resultsState.results,
                            key = { it.id }
                        ) { word ->
                            SearchResultItem(
                                word = word,
                                onClick = { onNavigateToWordDetail(word.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(word.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    word: Word,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.word,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight(510),
                        color = PrimaryText
                    )
                    if (word.phonetic.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = word.phonetic,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = TertiaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = word.translation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText,
                    maxLines = 2
                )

                if (word.masteryLevel > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MasteryBadge(level = word.masteryLevel)
                        if (word.wordBank != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = word.wordBank!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = TertiaryText
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                IconButton(
                    onClick = { /* TODO: TTS */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "发音",
                        tint = TertiaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (word.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (word.isFavorite) Error else TertiaryText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MasteryBadge(level: Int) {
    val (text, color) = when (level) {
        1 -> "学习中" to MasteryLevel1
        2 -> "熟悉" to MasteryLevel2
        3 -> "理解" to MasteryLevel3
        4 -> "掌握" to MasteryLevel4
        5 -> "精通" to MasteryLevel5
        else -> "陌生" to MasteryLevel0
    }
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.3f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryText,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SearchHintContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TertiaryText.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "搜索单词",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "输入英文单词或中文释义进行搜索",
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyResultContent(query: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TertiaryText.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "未找到结果",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "没有找到与 \"$query\" 相关的单词",
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryText.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
