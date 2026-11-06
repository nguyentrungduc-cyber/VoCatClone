package com.vocatclone.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Mở từ dòng "Tài khoản" ở tab "Của tôi" khi chưa đăng nhập.
 * Đăng nhập thành công → finish(); MainActivity có AuthStateListener nên tự cập nhật email.
 */
class LoginActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

    /** Mở màn Đăng ký và chờ kết quả: đăng ký thành công thì đóng luôn màn Đăng nhập. */
    private val registerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoCatCloneTheme {
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(uiState.isSuccess) {
                    if (uiState.isSuccess) {
                        setResult(RESULT_OK)
                        finish()
                    }
                }

                LoginScreen(
                    uiState = uiState,
                    onBack = { finish() },
                    onLogin = viewModel::signIn,
                    onRegisterClick = {
                        viewModel.clearMessages()
                        registerLauncher.launch(Intent(this, RegisterActivity::class.java))
                    },
                    onSendPasswordReset = viewModel::sendPasswordReset,
                    onDismissMessage = viewModel::clearMessages
                )
            }
        }
    }
}
