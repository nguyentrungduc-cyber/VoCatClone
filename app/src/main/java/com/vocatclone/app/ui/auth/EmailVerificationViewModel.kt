package com.vocatclone.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vocatclone.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Kết quả của lần bấm "Gửi lại" gần nhất, để màn hình chọn câu thông báo. */
enum class ResendResult { SENT, FAILED }

data class EmailVerificationUiState(
    /** true khi đang đăng nhập bằng một email chưa xác thực. */
    val show: Boolean = false,
    val email: String? = null,
    val isSending: Boolean = false,
    val resendResult: ResendResult? = null
)

/**
 * Trạng thái của dải nhắc xác thực email trên màn Home.
 *
 * Firebase lưu đệm cờ đã xác thực trong máy, nên sau khi người dùng bấm link trong hộp thư
 * phải gọi [refresh] (tải lại tài khoản) thì dải nhắc mới biến mất.
 */
class EmailVerificationViewModel : ViewModel() {

    private val auth = AuthRepository()

    private val _state = MutableStateFlow(EmailVerificationUiState())
    val state: StateFlow<EmailVerificationUiState> = _state.asStateFlow()

    /** Hỏi lại Firebase xem email đã xác thực chưa. Gọi mỗi khi người dùng quay lại app. */
    fun refresh() = viewModelScope.launch {
        if (auth.currentUser?.isEmailVerified != false) {
            // Chưa đăng nhập, hoặc đã xác thực rồi: không cần hỏi lại Firebase.
            _state.update { it.copy(show = false, resendResult = null) }
            return@launch
        }
        // Không có mạng thì giữ nguyên trạng thái đang có, vẫn hiện dải nhắc.
        runCatching { auth.reloadUser() }
        val user = auth.currentUser
        _state.update { it.copy(show = user?.isEmailVerified == false, email = user?.email) }
    }

    fun resend() {
        if (_state.value.isSending) return
        _state.update { it.copy(isSending = true, resendResult = null) }
        viewModelScope.launch {
            val ok = runCatching { auth.sendEmailVerification() }.isSuccess
            _state.update {
                it.copy(isSending = false, resendResult = if (ok) ResendResult.SENT else ResendResult.FAILED)
            }
        }
    }
}
