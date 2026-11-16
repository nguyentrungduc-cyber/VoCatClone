package com.vocatclone.app.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Người dùng đang đăng nhập. [uid] chính là cột firebaseUid để lọc dữ liệu trong Room.
 *
 * [isEmailVerified] được Firebase lưu đệm trong máy: sau khi người dùng bấm link trong hộp thư,
 * giá trị này chỉ đổi sau khi gọi [AuthRepository.reloadUser].
 */
data class AuthUser(val uid: String, val email: String?, val isEmailVerified: Boolean)

/**
 * Cửa duy nhất để phần còn lại của app biết "ai đang đăng nhập".
 * Việc đăng nhập/đăng ký vẫn do [com.vocatclone.app.ui.auth.AuthViewModel] làm.
 */
class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUser: AuthUser?
        get() = auth.currentUser?.let { AuthUser(it.uid, it.email, it.isEmailVerified) }

    /** Phát giá trị hiện tại ngay khi lắng nghe, rồi phát lại mỗi khi đăng nhập/đăng xuất. */
    val userFlow: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.let { AuthUser(it.uid, it.email, it.isEmailVerified) })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    val uidFlow: Flow<String?> = userFlow.map { it?.uid }.distinctUntilChanged()

    fun signOut() = auth.signOut()

    /** Gửi (hoặc gửi lại) email xác thực cho tài khoản đang đăng nhập. Ném lỗi nếu thất bại. */
    suspend fun sendEmailVerification() {
        val user = auth.currentUser ?: throw IllegalStateException("Chưa đăng nhập")
        user.sendEmailVerification().awaitResult()
    }

    /**
     * Tải lại thông tin tài khoản từ Firebase, để [AuthUser.isEmailVerified] phản ánh
     * việc người dùng vừa bấm link trong hộp thư. Cần mạng.
     */
    suspend fun reloadUser() {
        val user = auth.currentUser ?: return
        user.reload().awaitResult()
    }
}

/** Chờ một Task của Firebase theo kiểu coroutine (project chưa có kotlinx-coroutines-play-services). */
private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { cont.resume(it) }
    addOnFailureListener { cont.resumeWithException(it) }
}
