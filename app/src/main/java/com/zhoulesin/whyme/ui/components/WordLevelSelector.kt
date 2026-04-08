package com.zhoulesin.whyme.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zhoulesin.whyme.domain.model.LevelProgress
import com.zhoulesin.whyme.domain.model.WordLevel
import com.zhoulesin.whyme.ui.theme.*

/**
 * 词库级别选择器
 * 用于在首页/学习中心切换当前学习的词库级别
 */
@Composable
fun WordLevelSelector(
    currentLevel: WordLevel,
    onLevelSelected: (WordLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // 当前级别显示按钮
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = Level3Surface,
            border = BorderStroke(1.dp, BorderStandard)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "当前词库",
                        style = MaterialTheme.typography.labelMedium,
                        color = TertiaryText
                    )
                    Text(
                        text = currentLevel.displayName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight(590),
                        color = PrimaryText
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "展开",
                    tint = SecondaryText
                )
            }
        }
        
        // 下拉菜单
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f).background(Level3Surface)
        ) {
            WordLevel.entries.forEach { level ->
                val isSelected = level == currentLevel
                
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = level.displayName,
                                    fontWeight = if (isSelected) FontWeight(590) else FontWeight(400),
                                    color = PrimaryText
                                )
                                Text(
                                    text = level.description,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TertiaryText
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已选择",
                                    tint = AccentViolet
                                )
                            }
                        }
                    },
                    onClick = {
                        onLevelSelected(level)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * 词库级别卡片列表
 * 用于设置页面展示所有级别及进度
 */
@Composable
fun WordLevelCardList(
    currentLevel: WordLevel,
    enabledLevels: Set<WordLevel>,
    levelProgressMap: Map<WordLevel, LevelProgress>,
    onLevelSelected: (WordLevel) -> Unit,
    onLevelEnabledChanged: (WordLevel, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = Level3Surface,
        border = BorderStroke(1.dp, BorderStandard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "词库级别",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight(510),
                color = PrimaryText
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            WordLevel.entries.forEach { level ->
                val progress = levelProgressMap[level]
                val isCurrentLevel = level == currentLevel
                val isEnabled = level in enabledLevels
                
                WordLevelItem(
                    level = level,
                    progress = progress,
                    isCurrentLevel = isCurrentLevel,
                    isEnabled = isEnabled,
                    onSelect = { onLevelSelected(level) },
                    onEnabledChanged = { onLevelEnabledChanged(level, it) }
                )
                
                if (level != WordLevel.entries.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = BorderStandard
                    )
                }
            }
        }
    }
}

@Composable
private fun WordLevelItem(
    level: WordLevel,
    progress: LevelProgress?,
    isCurrentLevel: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit,
    onEnabledChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 级别信息
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = level.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrentLevel) FontWeight(590) else FontWeight(400),
                    color = if (isCurrentLevel) AccentViolet else PrimaryText
                )
                if (isCurrentLevel) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BrandIndigo
                    ) {
                        Text(
                            text = "当前",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryText,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = level.description,
                style = MaterialTheme.typography.labelMedium,
                color = TertiaryText
            )
            
            // 进度条
            if (progress != null && progress.totalWords > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progress.learningProgress },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = AccentViolet,
                        trackColor = BorderStandard
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${progress.learnedWords}/${progress.totalWords}",
                        style = MaterialTheme.typography.labelMedium,
                        color = TertiaryText
                    )
                }
            }
        }
        
        // 启用开关
        Switch(
            checked = isEnabled,
            onCheckedChange = onEnabledChanged,
            colors = SwitchDefaults.colors(
                checkedTrackColor = AccentViolet,
                uncheckedTrackColor = BorderStandard,
                checkedThumbColor = PrimaryText,
                uncheckedThumbColor = SecondaryText
            )
        )
    }
}

/**
 * 简洁的词库级别标签
 */
@Composable
fun WordLevelChip(
    level: WordLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) BrandIndigo
                      else Level3Surface,
        label = "background"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryText
                      else SecondaryText,
        label = "text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) BrandIndigo
                      else BorderStandard,
        label = "border"
    )
    
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = level.shortName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight(590) else FontWeight(510),
            color = textColor,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

/**
 * 词库级别横向滚动选择器
 */
@Composable
fun WordLevelHorizontalSelector(
    currentLevel: WordLevel,
    enabledLevels: Set<WordLevel>,
    onLevelSelected: (WordLevel) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        enabledLevels.forEach { level ->
            WordLevelChip(
                level = level,
                isSelected = level == currentLevel,
                onClick = { onLevelSelected(level) }
            )
        }
    }
}
