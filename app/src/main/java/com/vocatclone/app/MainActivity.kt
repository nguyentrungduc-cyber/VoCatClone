package com.vocatclone.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import com.vocatclone.app.ui.auth.LoginActivity
import com.vocatclone.app.ui.auth.RegisterActivity
import com.vocatclone.app.ui.components.MainTab
import com.vocatclone.app.ui.components.VoCatBottomBar
import com.vocatclone.app.ui.home.HomeScreen
import com.vocatclone.app.ui.profile.ProfileScreen
import com.vocatclone.app.ui.splash.SplashScreen
import com.vocatclone.app.ui.theme.VoCatCloneTheme
import com.vocatclone.app.ui.welcome.WelcomeScreen
import kotlinx.coroutines.delay

/** Thời gian hiện màn Splash (mili giây). */
private const val SPLASH_DURATION_MS = 1200L
private const val PREFS_NAME = "vocat_prefs"
private const val KEY_SEEN_WELCOME = "seen_welcome"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(    )
        setContent {
            VoCatCloneTheme {
                val auth = remember { FirebaseAuth.getInstance() }
                // Lưu email vào state + lắng nghe AuthStateListener để UI tự cập nhật
                // khi đăng nhập/đăng xuất (đọc thẳng auth.currentUser sẽ không recompose).
                var userEmail by remember { mutableStateOf(auth.currentUser?.email) }
                DisposableEffect(auth) {
                    val listener = FirebaseAuth.AuthStateListener { userEmail = it.currentUser?.email }
                    auth.addAuthStateListener(listener)
                    onDispose { auth.removeAuthStateListener(listener) }
                }

                // Splash hiện ~1,2 giây khi mở app. rememberSaveable: xoay màn hình không chiếu lại.
                var showSplash by rememberSaveable { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(SPLASH_DURATION_MS)
                    showSplash = false
                }

                // Màn chào mừng chỉ hiện ở lần mở app đầu tiên (nhớ bằng SharedPreferences).
                val prefs = remember { getSharedPreferences(PREFS_NAME, MODE_PRIVATE) }
                var seenWelcome by remember { mutableStateOf(prefs.getBoolean(KEY_SEEN_WELCOME, false)) }

                when {
                    showSplash -> SplashScreen()
                    !seenWelcome -> WelcomeScreen(
                        onStartClick = {
                            prefs.edit().putBoolean(KEY_SEEN_WELCOME, true).apply()
                            seenWelcome = true
                            // TODO: khi có màn "Tạo bộ từ vựng" thì mở màn đó thay vì vào thẳng MainApp.
                        }
                    )
                    else -> MainApp(
                        userEmail = userEmail,
                        onSignOut = { auth.signOut() },
                        // Chưa đăng nhập → mở màn Đăng nhập. Đã đăng nhập → (sau này) màn thông tin tài khoản.
                        onAccountClick = {
                            if (userEmail == null) {
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                        },
                        onRegisterClick = {
                            startActivity(Intent(this, RegisterActivity::class.java))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Khung chính của app. Không gọi Firebase trực tiếp — nhận dữ liệu/sự kiện từ ngoài
 * (state hoisting) để Preview chạy được mà không cần khởi tạo Firebase.
 */
@Composable
fun MainApp(
    userEmail: String? = null,
    onSignOut: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(MainTab.VOCAB) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            VoCatBottomBar(selected = selectedTab, onSelect = { selectedTab = it })
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.VOCAB -> HomeScreen(modifier = Modifier.padding(innerPadding))
            MainTab.PROFILE -> ProfileScreen(
                userEmail = userEmail,
                modifier = Modifier.padding(innerPadding),
                onAccountClick = onAccountClick,
                onRegisterClick = onRegisterClick,
                onSignOutClick = onSignOut
            )
            else -> {
                // Các tab chưa triển khai — placeholder
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Tính năng đang phát triển")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppPreview() {
    VoCatCloneTheme {
        MainApp()
    }
}
