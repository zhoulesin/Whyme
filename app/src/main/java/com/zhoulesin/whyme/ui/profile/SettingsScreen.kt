package com.zhoulesin.whyme.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zhoulesin.whyme.ui.theme.MarketingBlack
import com.zhoulesin.whyme.ui.theme.PanelDark
import com.zhoulesin.whyme.ui.theme.PrimaryText
import com.zhoulesin.whyme.ui.theme.SecondaryText
import com.zhoulesin.whyme.ui.theme.TertiaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MarketingBlack,
        contentColor = PrimaryText,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "学习设置",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight(510),
                        color = PrimaryText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = SecondaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PanelDark,
                    titleContentColor = PrimaryText,
                    navigationIconContentColor = SecondaryText
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(MarketingBlack)
        ) {
            Text(
                text = "每日目标、提醒等将在此配置。",
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )
            Text(
                text = "当前产品固定为六级词域，不提供多级切换。",
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryText,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}
