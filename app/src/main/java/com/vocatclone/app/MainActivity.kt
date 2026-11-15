package com.vocatclone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.vocatclone.app.ui.components.MainTab
import com.vocatclone.app.ui.components.VoCatBottomBar
import com.vocatclone.app.ui.home.HomeScreen
import com.vocatclone.app.ui.navigation.AppNavHost
import com.vocatclone.app.ui.profile.ProfileScreen
import com.vocatclone.app.ui.theme.VoCatCloneTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoCatCloneTheme {
                // Toàn bộ điều hướng (Splash, Welcome, Login, Register, Home, các màn chi tiết) nằm trong AppNavHost.
                AppNavHost()
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
    onRegisterClick: () -> Unit = {},
    /** Nội dung tab Vocabulary. Mặc định là Home rỗng để Preview chạy được mà không cần database. */
    vocabulary: @Composable (Modifier) -> Unit = { m -> HomeScreen(modifier = m) }
) {
    var selectedTab by remember { mutableStateOf(MainTab.VOCAB) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            VoCatBottomBar(selected = selectedTab, onSelect = { selectedTab = it })
        }
    ) { innerPadding ->
        when (selectedTab) {
            MainTab.VOCAB -> vocabulary(Modifier.padding(innerPadding))
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
