package com.zhoulesin.whyme.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.domain.model.AchievementIcon
import com.zhoulesin.whyme.ui.theme.SkyBlue

@Composable
fun ProfileScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 用户信息卡片
        ProfileHeader()

        Spacer(modifier = Modifier.height(24.dp))

        // 学习数据卡片
        LearningDataCard(
            totalWords = uiState.userStats.totalWordsLearned + uiState.userStats.totalWordsReviewed,
            masteredWords = uiState.masteredWords,
            streak = uiState.userStats.currentStreak,
            totalMinutes = uiState.userStats.totalLearningMinutes
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 成就卡片
        AchievementsCard(achievements = uiState.achievements)

        Spacer(modifier = Modifier.height(24.dp))

        // 功能列表
        FunctionList(
            onNavigateToFavorites = onNavigateToFavorites,
            onNavigateToStatistics = onNavigateToStatistics,
            onNavigateToSettings = onNavigateToSettings
        )
    }
}

@Composable
private fun ProfileHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SkyBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "英语学习者",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "持续学习中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun LearningDataCard(
    totalWords: Int,
    masteredWords: Int,
    streak: Int,
    totalMinutes: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "学习数据",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LearningDataItem(
                    value = "$totalWords",
                    label = "已学单词",
                    icon = Icons.Default.School
                )
                LearningDataItem(
                    value = "$masteredWords",
                    label = "已掌握",
                    icon = Icons.Default.Star
                )
                LearningDataItem(
                    value = "$streak 天",
                    label = "连续打卡",
                    icon = Icons.Default.LocalFireDepartment
                )
                LearningDataItem(
                    value = "${totalMinutes} 分钟",
                    label = "总学习时长",
                    icon = Icons.Default.Timer
                )
            }
        }
    }
}

@Composable
private fun LearningDataItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AchievementsCard(achievements: List<com.zhoulesin.whyme.domain.model.Achievement>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "成就",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${achievements.count { it.isUnlocked }}/${achievements.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                achievements.take(4).forEach { achievement ->
                    AchievementBadge(
                        achievement = achievement,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    achievement: com.zhoulesin.whyme.domain.model.Achievement,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (achievement.isUnlocked)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (achievement.icon) {
                    AchievementIcon.STAR -> Icons.Default.Star
                    AchievementIcon.TROPHY -> Icons.Default.EmojiEvents
                    AchievementIcon.MEDAL -> Icons.Default.MilitaryTech
                    AchievementIcon.FIRE -> Icons.Default.LocalFireDepartment
                    AchievementIcon.DIAMOND -> Icons.Default.WorkspacePremium
                    AchievementIcon.CROWN -> Icons.Default.Star
                    AchievementIcon.BOOK -> Icons.Default.Star
                    AchievementIcon.LIGHTNING -> Icons.Default.Star
                },
                contentDescription = null,
                tint = if (achievement.isUnlocked)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = achievement.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (achievement.isUnlocked)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            maxLines = 1
        )
    }
}

@Composable
private fun FunctionList(
    onNavigateToFavorites: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            FunctionListItem(
                icon = Icons.Default.Favorite,
                title = "我的收藏",
                subtitle = "查看收藏的单词",
                onClick = onNavigateToFavorites
            )
            HorizontalDivider()
            FunctionListItem(
                icon = Icons.Default.BarChart,
                title = "学习统计",
                subtitle = "查看详细学习数据",
                onClick = onNavigateToStatistics
            )
            HorizontalDivider()
            FunctionListItem(
                icon = Icons.Default.Settings,
                title = "设置",
                subtitle = "每日目标、应用设置",
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun FunctionListItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
