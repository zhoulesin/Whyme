package com.zhoulesin.whyme.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 圆形进度环组件
 */
@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    centerContent: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sweepAngle = 360 * animatedProgress
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

            // Background circle
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth.toPx(), size.toPx() - strokeWidth.toPx()),
                topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)
            )

            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke,
                size = androidx.compose.ui.geometry.Size(size.toPx() - strokeWidth.toPx(), size.toPx() - strokeWidth.toPx()),
                topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)
            )
        }

        centerContent()
    }
}

/**
 * 学习进度卡片
 */
@Composable
fun LearningProgressCard(
    learned: Int,
    total: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) learned.toFloat() / total else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        CircularProgressRing(
            progress = progress,
            size = 100.dp,
            strokeWidth = 10.dp,
            progressColor = MaterialTheme.colorScheme.primary,
            centerContent = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$learned",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ $total",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 统计数据项
 */
@Composable
fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
