package com.zhoulesin.whyme.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zhoulesin.whyme.domain.model.Word
import com.zhoulesin.whyme.ui.theme.*

/**
 * 单词卡片组件（可翻转）
 */
@Composable
fun WordCard(
    word: Word,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onFavoriteClick: (Long) -> Unit,
    onSpeakClick: () -> Unit,
    onSpeakExampleClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardHeight: Dp = 360.dp,
    cornerRadius: Dp = 8.dp,
    animationDuration: Int = 400,
    showFavoriteButton: Boolean = true,
    showSpeakButton: Boolean = true,
    frontHintText: String = "点击卡片查看释义",
    backHintText: String = "点击卡片返回"
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "card_flip"
    )

    val backgroundColor by animateColorAsState(
        targetValue = MasteryLevel0,
        label = "card_color"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() },
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation <= 90f) {
                // 正面 - 英文单词
                WordFrontContent(
                    word = word,
                    onFavoriteClick = onFavoriteClick,
                    onSpeakClick = onSpeakClick,
                    showFavoriteButton = showFavoriteButton,
                    showSpeakButton = showSpeakButton,
                    hintText = frontHintText
                )
            } else {
                // 背面 - 中文释义
                WordBackContent(
                    word = word,
                    onSpeakExampleClick = onSpeakExampleClick,
                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                    hintText = backHintText
                )
            }
        }
    }
}

@Composable
private fun WordFrontContent(
    word: Word,
    onFavoriteClick: (Long) -> Unit,
    onSpeakClick: () -> Unit,
    showFavoriteButton: Boolean,
    showSpeakButton: Boolean,
    hintText: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // 收藏按钮
        if (showFavoriteButton) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onFavoriteClick(word.id) }) {
                    Icon(
                        imageVector = if (word.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (word.isFavorite) Error else TertiaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 单词
        Text(
            text = word.word,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight(510),
            color = PrimaryText
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 音标
        Text(
            text = word.phonetic,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = TertiaryText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 发音按钮
        if (showSpeakButton) {
            IconButton(
                onClick = onSpeakClick,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = BrandIndigo,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "发音",
                    tint = PrimaryText,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 提示
        Text(
            text = hintText,
            style = MaterialTheme.typography.labelMedium,
            color = TertiaryText
        )
    }
}

@Composable
private fun WordBackContent(
    word: Word,
    onSpeakExampleClick: () -> Unit,
    modifier: Modifier = Modifier,
    hintText: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // 单词（参考）
        Text(
            text = word.word,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight(510),
            color = PrimaryText
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 释义
        Text(
            text = word.translation,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight(590),
            color = AccentViolet,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 例句
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 英文原句
            Text(
                text = word.example,
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = TertiaryText,
                textAlign = TextAlign.Center
            )
            
            // 中文译文
            if (word.exampleTranslation.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = word.exampleTranslation,
                    style = MaterialTheme.typography.bodySmall,
                    color = TertiaryText,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 例句发音按钮
            IconButton(
                onClick = onSpeakExampleClick,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = BrandIndigo,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "例句发音",
                    tint = PrimaryText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 提示
        Text(
            text = hintText,
            style = MaterialTheme.typography.labelMedium,
            color = TertiaryText
        )
    }
}

/**
 * 记忆程度选择按钮组
 */
@Composable
fun MasteryButtons(
    onAgain: () -> Unit,
    onHard: () -> Unit,
    onGood: () -> Unit,
    onEasy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MasteryButton(
            text = "不认识",
            color = MasteryLevel1,
            onClick = onAgain
        )
        MasteryButton(
            text = "模糊",
            color = MasteryLevel2,
            onClick = onHard
        )
        MasteryButton(
            text = "认识",
            color = MasteryLevel4,
            onClick = onGood
        )
        MasteryButton(
            text = "太简单",
            color = MasteryLevel5,
            onClick = onEasy
        )
    }
}

@Composable
private fun MasteryButton(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = color,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight(510),
            color = PrimaryText
        )
    }
}
