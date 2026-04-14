package com.zhoulesin.whyme.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhoulesin.whyme.ui.theme.BrandIndigo
import com.zhoulesin.whyme.ui.theme.Level3Surface
import com.zhoulesin.whyme.ui.theme.MarketingBlack
import com.zhoulesin.whyme.ui.theme.PanelDark
import com.zhoulesin.whyme.ui.theme.PrimaryText
import com.zhoulesin.whyme.ui.theme.SecondaryText
import com.zhoulesin.whyme.ui.theme.TertiaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("保存成功")
            viewModel.consumeSaveSuccess()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MarketingBlack,
        contentColor = PrimaryText,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                .background(MarketingBlack),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "学习计划",
                style = MaterialTheme.typography.bodyLarge,
                color = SecondaryText
            )
            Text(
                text = "设置每天计划数量（学习/复习/测试）",
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryText
            )

            Spacer(modifier = Modifier.height(8.dp))

            PlanInputField(
                label = "学习词数/天",
                value = uiState.learnPerDayInput,
                onValueChange = viewModel::onLearnInputChanged
            )
            PlanInputField(
                label = "复习词数/天",
                value = uiState.reviewPerDayInput,
                onValueChange = viewModel::onReviewInputChanged
            )
            PlanInputField(
                label = "测试词数/天",
                value = uiState.testPerDayInput,
                onValueChange = viewModel::onTestInputChanged
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = { viewModel.save() },
                enabled = !uiState.isLoading && !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigo,
                    contentColor = PrimaryText
                )
            ) {
                Text(if (uiState.isSaving) "保存中..." else "保存设置")
            }

            Text(
                text = "当前产品固定为六级词域，不提供多级切换。",
                style = MaterialTheme.typography.bodyMedium,
                color = TertiaryText,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

@Composable
private fun PlanInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = PrimaryText,
            unfocusedTextColor = PrimaryText,
            focusedLabelColor = TertiaryText,
            unfocusedLabelColor = TertiaryText,
            focusedBorderColor = TertiaryText,
            unfocusedBorderColor = TertiaryText.copy(alpha = 0.5f),
            focusedContainerColor = Level3Surface,
            unfocusedContainerColor = Level3Surface
        )
    )
}
