package com.zhoulesin.whyme.ui.wordbank

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.ui.components.CircularProgressRing
import com.zhoulesin.whyme.ui.theme.AccentViolet
import com.zhoulesin.whyme.ui.theme.BorderPrimary
import com.zhoulesin.whyme.ui.theme.BorderStandard
import com.zhoulesin.whyme.ui.theme.Level3Surface
import com.zhoulesin.whyme.ui.theme.MarketingBlack
import com.zhoulesin.whyme.ui.theme.PrimaryText
import com.zhoulesin.whyme.ui.theme.SecondaryText
import com.zhoulesin.whyme.ui.theme.TertiaryText

/**
 * 「六级库」Tab：唯一词表浏览、搜索、单线进度（Linear 风格：MarketingBlack、8dp 卡片、半透边框）
 */
@Composable
fun WordBankLibraryScreen(
    onWordClick: (Long) -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    viewModel: WordBankLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MarketingBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "六级库",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight(510),
            color = PrimaryText
        )
        Text(
            text = "浏览与搜索 · 固定六级词域",
            style = MaterialTheme.typography.bodyLarge,
            color = TertiaryText,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "搜索单词或释义",
                    color = TertiaryText,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PrimaryText,
                unfocusedTextColor = PrimaryText,
                focusedBorderColor = BorderStandard,
                unfocusedBorderColor = BorderStandard,
                focusedContainerColor = Level3Surface,
                unfocusedContainerColor = Level3Surface,
                cursorColor = AccentViolet
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        ProgressCard(uiState)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onNavigateToReview,
                modifier = Modifier.weight(1f)
            ) {
                Text("去复习", color = SecondaryText, fontWeight = FontWeight(510))
            }
            TextButton(
                onClick = onNavigateToQuiz,
                modifier = Modifier.weight(1f)
            ) {
                Text("单词测验", color = SecondaryText, fontWeight = FontWeight(510))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "词条列表 · ${uiState.words.size}",
            style = MaterialTheme.typography.labelMedium,
            color = TertiaryText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            items(uiState.words, key = { it.id }) { word ->
                WordRow(word = word, onClick = { onWordClick(word.id) })
                HorizontalDivider(color = BorderStandard, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun ProgressCard(uiState: WordBankLibraryUiState) {
    val pct = if (uiState.totalWords > 0) {
        (uiState.learnedWords.toFloat() / uiState.totalWords).coerceIn(0f, 1f)
    } else {
        0f
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircularProgressRing(
                progress = pct,
                size = 96.dp,
                strokeWidth = 10.dp,
                progressColor = AccentViolet,
                backgroundColor = BorderPrimary,
                centerContent = {
                    Text(
                        text = "${(pct * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight(510),
                        color = PrimaryText
                    )
                }
            )
            Column(
                modifier = Modifier.padding(start = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "六级进度",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight(510),
                    color = PrimaryText
                )
                Text(
                    text = "已见 ${uiState.learnedWords} / ${uiState.totalWords}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SecondaryText
                )
                Text(
                    text = "已掌握 ${uiState.masteredWords}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TertiaryText
                )
            }
        }
    }
}

@Composable
private fun WordRow(
    word: Word,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MarketingBlack
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = word.word,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight(510),
                color = PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.4f)
            )
            Text(
                text = word.translation,
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.55f)
            )
        }
    }
}
