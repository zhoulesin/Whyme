package com.zhoulesin.whyme.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.zhoulesin.whyme.data.datastore.UserManager
import com.zhoulesin.whyme.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    userManager: UserManager,
    onLoginSuccess: () -> Unit
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MarketingBlack)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo 和标题
        Text(
            text = "WhyMe English",
            style = MaterialTheme.typography.displayLarge,
            color = BrandIndigo,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // 登录表单
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = Level3Surface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderStandard)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "登录",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PrimaryText,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // 错误提示
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = Error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // 账号输入
                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("账号") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    keyboardActions = KeyboardActions(onDone = {})
                )

                // 密码输入
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                tint = TertiaryText
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    keyboardActions = KeyboardActions(onDone = {})
                )

                // 登录按钮
                Button(
                    onClick = {
                        if (account.isBlank() || password.isBlank()) {
                            errorMessage = "请输入账号和密码"
                        } else {
                            errorMessage = ""
                            isLoading = true
                            coroutineScope.launch {
                                val success = userManager.login(account, password)
                                isLoading = false
                                if (success) {
                                    onLoginSuccess()
                                } else {
                                    errorMessage = "登录失败"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandIndigo,
                        contentColor = PrimaryText,
                        disabledContainerColor = BrandIndigo.copy(alpha = 0.6f),
                        disabledContentColor = PrimaryText.copy(alpha = 0.6f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = PrimaryText,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "登录",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }

                // 提示信息
                Text(
                    text = "暂时没有后端，默认密码正确",
                    style = MaterialTheme.typography.bodySmall,
                    color = TertiaryText,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
