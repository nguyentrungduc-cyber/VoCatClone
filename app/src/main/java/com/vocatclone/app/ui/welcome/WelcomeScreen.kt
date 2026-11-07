package com.vocatclone.app.ui.welcome

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocatclone.app.ui.components.CatMascot
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Màn chào mừng lần đầu mở app: bong bóng thoại + mèo mascot, nút lớn ở đáy màn hình.
 * @param onStartClick bấm "Tạo bộ từ vựng đầu tiên".
 */
@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            SpeechBubble(text = "Nice to meet you!\nI was waiting for you!")
            CatMascot()
        }

        Button(
            onClick = onStartClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 40.dp)
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(imageVector = Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Text(text = "Go to make a Vocabulary", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Bong bóng thoại: khung bo tròn kèm một mũi nhọn hướng xuống chỗ con mèo. */
@Composable
private fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val fill = colors.surface
    val border = colors.outline

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = fill,
            border = BorderStroke(1.5.dp, border),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = text,
                color = colors.onSurface,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
            )
        }
        // Mũi nhọn: tô cùng màu nền bong bóng, chỉ viền hai cạnh xiên để "liền" với khung phía trên.
        Canvas(modifier = Modifier.size(width = 22.dp, height = 11.dp).offset(y = (-1.5).dp)) {
            val w = size.width
            val h = size.height
            val tail = Path().apply {
                moveTo(0f, 0f)
                lineTo(w / 2f, h)
                lineTo(w, 0f)
                close()
            }
            drawPath(path = tail, color = fill)
            val strokeWidth = 1.5.dp.toPx()
            drawLine(color = border, start = Offset(0f, 0f), end = Offset(w / 2f, h), strokeWidth = strokeWidth, cap = StrokeCap.Round)
            drawLine(color = border, start = Offset(w / 2f, h), end = Offset(w, 0f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun WelcomeScreenDarkPreview() {
    VoCatCloneTheme { WelcomeScreen(onStartClick = {}) }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 390, heightDp = 844)
@Composable
private fun WelcomeScreenLightPreview() {
    VoCatCloneTheme { WelcomeScreen(onStartClick = {}) }
}
