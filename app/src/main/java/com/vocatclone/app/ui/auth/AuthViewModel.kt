package com.vocatclone.app.ui.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Trạng thái chung của màn Đăng nhập / Đăng ký. */
data class AuthUiState(
    val isLoading: Boolean = false,
    /** Lỗi từ Firebase, đã dịch sang tiếng Việt. null = không có lỗi. */
    val errorMessage: String? = null,
    /** Thông báo thành công không làm đóng màn hình (vd: đã gửi email đặt lại mật khẩu). */
    val infoMessage: String? = null,
    /** true khi đăng nhập/đăng ký thành công → Activity gọi finish(). */
    val isSuccess: Boolean = false
)

/**
 * Gói các lời gọi Firebase Auth. Dùng ViewModel để trạng thái (đang tải, lỗi)
 * không bị mất khi xoay màn hình, và để Composable không gọi Firebase trực tiếp.
 */
class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun signIn(email: String, password: String) {
        if (_uiState.value.isLoading) return            // chặn bấm nhiều lần
        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
            .addOnFailureListener { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toVietnamese()) } }
    }

    fun register(email: String, password: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        // Tạo tài khoản thành công thì Firebase tự đăng nhập luôn.
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { _uiState.update { it.copy(isLoading = false, isSuccess = true) } }
            .addOnFailureListener { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toVietnamese()) } }
    }

    fun sendPasswordReset(email: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
        auth.sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(isLoading = false, infoMessage = "Nếu email đã đăng ký, bạn sẽ nhận được link đặt lại mật khẩu.")
                }
            }
            .addOnFailureListener { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.toVietnamese()) } }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }
}

/** Dịch lỗi Firebase sang câu tiếng Việt dễ hiểu cho người dùng. */
private fun Exception.toVietnamese(): String = when (this) {
    is FirebaseAuthWeakPasswordException -> "Mật khẩu quá yếu, cần ít nhất 6 ký tự."
    is FirebaseAuthUserCollisionException -> "Email này đã được đăng ký."
    is FirebaseAuthInvalidUserException -> "Tài khoản không tồn tại hoặc đã bị vô hiệu hóa."
    is FirebaseAuthInvalidCredentialsException -> "Email hoặc mật khẩu không đúng."
    is FirebaseNetworkException -> "Không có kết nối mạng. Vui lòng thử lại."
    is FirebaseTooManyRequestsException -> "Bạn thử quá nhiều lần. Vui lòng đợi một lúc rồi thử lại."
    else -> "Đã có lỗi xảy ra: ${localizedMessage ?: "không rõ nguyên nhân"}"
}
