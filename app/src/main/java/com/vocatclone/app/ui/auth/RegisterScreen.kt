package com.vocatclone.app.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/** Màn Đăng ký: Email + Mật khẩu + Nhập lại mật khẩu, kiểm tra ngay khi gõ. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    uiState: AuthUiState,
    onBack: () -> Unit,
    onRegister: (email: String, password: String) -> Unit,
    onLoginClick: () -> Unit,
    onDismissMessage: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }

    val emailError = when {
        email.isBlank() -> if (submitted) "Vui lòng nhập email" else null
        !isValidEmail(email) -> "Email không hợp lệ"
        else -> null
    }
    val passwordError = when {
        password.isEmpty() -> if (submitted) "Vui lòng nhập mật khẩu" else null
        password.length < MIN_PASSWORD_LENGTH -> "Mật khẩu cần ít nhất $MIN_PASSWORD_LENGTH ký tự"
        else -> null
    }
    val confirmError = when {
        confirm.isEmpty() -> if (submitted) "Vui lòng nhập lại mật khẩu" else null
        confirm != password -> "Mật khẩu nhập lại không khớp"
        else -> null
    }
    val formValid = isValidEmail(email) &&
        password.length >= MIN_PASSWORD_LENGTH &&
        confirm == password

    val submit = {
        submitted = true
        if (formValid) onRegister(email, password)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo tài khoản") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuthHeader(
                title = "Bắt đầu học cùng VoCat",
                subtitle = "Tạo tài khoản để lưu tiến độ và đồng bộ giữa các thiết bị"
            )
            Spacer(Modifier.height(12.dp))

            EmailField(
                value = email,
                onValueChange = { email = it; onDismissMessage() },
                errorText = emailError,
                enabled = !uiState.isLoading
            )
            PasswordField(
                value = password,
                onValueChange = { password = it; onDismissMessage() },
                label = "Mật khẩu",
                errorText = passwordError,
                enabled = !uiState.isLoading,
                isLast = false
            )
            PasswordField(
                value = confirm,
                onValueChange = { confirm = it; onDismissMessage() },
                label = "Nhập lại mật khẩu",
                errorText = confirmError,
                enabled = !uiState.isLoading,
                isLast = true,
                onDone = submit
            )

            Spacer(Modifier.height(4.dp))
            AuthErrorText(uiState.errorMessage)
            AuthPrimaryButton(text = "Đăng ký", isLoading = uiState.isLoading, onClick = submit)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Đã có tài khoản?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onLoginClick, enabled = !uiState.isLoading) { Text("Đăng nhập") }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 860)
@Composable
private fun RegisterScreenPreview() {
    VoCatCloneTheme {
        RegisterScreen(
            uiState = AuthUiState(),
            onBack = {}, onRegister = { _, _ -> }, onLoginClick = {}, onDismissMessage = {}
        )
    }
}
