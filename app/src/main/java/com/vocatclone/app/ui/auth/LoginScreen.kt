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
import androidx.compose.material3.AlertDialog
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

/**
 * Màn Đăng nhập. Chỉ vẽ giao diện + kiểm tra dữ liệu nhập; việc gọi Firebase do
 * [AuthViewModel] làm, truyền vào qua [uiState] và các callback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: AuthUiState,
    onBack: () -> Unit,
    onLogin: (email: String, password: String) -> Unit,
    onRegisterClick: () -> Unit,
    onSendPasswordReset: (email: String) -> Unit,
    onDismissMessage: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    // Chỉ báo lỗi "để trống" sau lần bấm nút đầu tiên, tránh màn hình đỏ ngay khi vừa mở.
    var submitted by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    val emailError = when {
        email.isBlank() -> if (submitted) "Vui lòng nhập email" else null
        !isValidEmail(email) -> "Email không hợp lệ"
        else -> null
    }
    val passwordError = if (submitted && password.isEmpty()) "Vui lòng nhập mật khẩu" else null

    val submit = {
        submitted = true
        if (isValidEmail(email) && password.isNotEmpty()) onLogin(email, password)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
                .imePadding()                       // bàn phím không che ô nhập / nút
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AuthHeader(
                title = "Chào mừng trở lại",
                subtitle = "Đăng nhập để đồng bộ và sao lưu tủ sách từ vựng của bạn"
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
                isLast = true,
                onDone = submit
            )

            TextButton(
                onClick = { onDismissMessage(); showResetDialog = true },
                enabled = !uiState.isLoading,
                modifier = Modifier.align(Alignment.End)
            ) { Text("Quên mật khẩu?") }

            AuthErrorText(uiState.errorMessage)
            AuthPrimaryButton(text = "Đăng nhập", isLoading = uiState.isLoading, onClick = submit)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chưa có tài khoản?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onRegisterClick, enabled = !uiState.isLoading) { Text("Đăng ký") }
            }
        }
    }

    if (showResetDialog) {
        ForgotPasswordDialog(
            initialEmail = email,
            uiState = uiState,
            onSend = onSendPasswordReset,
            onDismiss = { showResetDialog = false; onDismissMessage() }
        )
    }
}

/** Dialog quên mật khẩu: nhập email → Firebase gửi link đặt lại mật khẩu. */
@Composable
private fun ForgotPasswordDialog(
    initialEmail: String,
    uiState: AuthUiState,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    val emailError = if (email.isNotBlank() && !isValidEmail(email)) "Email không hợp lệ" else null
    val sent = uiState.infoMessage != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quên mật khẩu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nhập email đã đăng ký, chúng tôi sẽ gửi link để bạn đặt lại mật khẩu.")
                EmailField(
                    value = email,
                    onValueChange = { email = it },
                    errorText = emailError,
                    enabled = !uiState.isLoading && !sent
                )
                uiState.infoMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
                AuthErrorText(uiState.errorMessage)
            }
        },
        confirmButton = {
            if (sent) {
                TextButton(onClick = onDismiss) { Text("Xong") }
            } else {
                TextButton(
                    onClick = { onSend(email) },
                    enabled = isValidEmail(email) && !uiState.isLoading
                ) { Text(if (uiState.isLoading) "Đang gửi..." else "Gửi") }
            }
        },
        dismissButton = {
            if (!sent) TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 860)
@Composable
private fun LoginScreenPreview() {
    VoCatCloneTheme {
        LoginScreen(
            uiState = AuthUiState(),
            onBack = {}, onLogin = { _, _ -> }, onRegisterClick = {},
            onSendPasswordReset = {}, onDismissMessage = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 860)
@Composable
private fun LoginScreenErrorPreview() {
    VoCatCloneTheme {
        LoginScreen(
            uiState = AuthUiState(errorMessage = "Email hoặc mật khẩu không đúng."),
            onBack = {}, onLogin = { _, _ -> }, onRegisterClick = {},
            onSendPasswordReset = {}, onDismissMessage = {}
        )
    }
}
