package com.vocatclone.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Người dùng đang đăng nhập. [uid] chính là cột firebaseUid để lọc dữ liệu trong Room. */
data class AuthUser(val uid: String, val email: String?)

/**
 * Cửa duy nhất để phần còn lại của app biết "ai đang đăng nhập".
 * Việc đăng nhập/đăng ký vẫn do [com.vocatclone.app.ui.auth.AuthViewModel] làm.
 */
class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentUser: AuthUser?
        get() = auth.currentUser?.let { AuthUser(it.uid, it.email) }

    /** Phát giá trị hiện tại ngay khi lắng nghe, rồi phát lại mỗi khi đăng nhập/đăng xuất. */
    val userFlow: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fa ->
            trySend(fa.currentUser?.let { AuthUser(it.uid, it.email) })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    val uidFlow: Flow<String?> = userFlow.map { it?.uid }.distinctUntilChanged()

    fun signOut() = auth.signOut()
}
