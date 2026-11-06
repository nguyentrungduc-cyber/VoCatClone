package com.vocatclone.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Mở từ dòng "Đăng ký tài khoản" ở tab "Của tôi", hoặc từ link "Đăng ký" trong LoginActivity.
 * Đăng ký thành công → Firebase tự đăng nhập → setResult(RESULT_OK) + finish().
 */
class RegisterActivity : ComponentActivity() {

    private val viewModel: AuthViewModel by viewModels()

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

                RegisterScreen(
                    uiState = uiState,
                    onBack = { finish() },
                    onRegister = viewModel::register,
                    onLoginClick = ::goToLogin,
                    onDismissMessage = viewModel::clearMessages
                )
            }
        }
    }

    /**
     * Nếu được mở từ LoginActivity (bằng registerForActivityResult) thì chỉ cần quay lại;
     * nếu mở thẳng từ tab "Của tôi" thì mở LoginActivity rồi đóng màn này.
     */
    private fun goToLogin() {
        if (callingActivity == null) {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
