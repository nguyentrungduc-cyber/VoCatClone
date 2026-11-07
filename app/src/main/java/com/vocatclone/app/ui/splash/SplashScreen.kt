package com.vocatclone.app.ui.splash

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocatclone.app.ui.components.VoCatLogo
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Màn hình chào lúc mở app: logo ở giữa, dòng bản quyền ở dưới cùng.
 * Chỉ vẽ giao diện; việc hiện bao lâu và chuyển sang đâu do MainActivity quyết định.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            VoCatLogo()
            Text(
                text = "VoCat Clone",
                color = colors.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Text(
            text = "© 2026 Nhóm 18 · NT118.R14",
            color = colors.onSurfaceVariant,
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp)
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun SplashScreenDarkPreview() {
    VoCatCloneTheme { SplashScreen() }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 390, heightDp = 844)
@Composable
private fun SplashScreenLightPreview() {
    VoCatCloneTheme { SplashScreen() }
}
