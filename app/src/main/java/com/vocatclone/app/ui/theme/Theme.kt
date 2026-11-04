package com.vocatclone.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Theme "Xanh Đen": nền xanh đen thay vì đen thuần.
private val DarkColorScheme = darkColorScheme(
    primary = NavyPrimary,
    onPrimary = NavyOnPrimary,
    secondary = NavySecondary,
    onSecondary = NavyOnSecondary,
    tertiary = NavyTertiary,
    background = NavyBackground,
    onBackground = NavyOnSurface,
    surface = NavySurface,
    onSurface = NavyOnSurface,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = NavyOnSurfaceVariant,
    outline = NavyOutline,
    outlineVariant = NavyOutlineVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun VoCatCloneTheme(
    // Mặc định luôn dùng theme Xanh Đen (giống VoCat gốc), không theo chế độ sáng/tối của hệ thống.
    // Muốn theo hệ thống: đổi thành isSystemInDarkTheme().
    darkTheme: Boolean = true,
    // Tắt dynamic color: nếu bật, Android 12+ sẽ lấy màu theo hình nền và đè mất bảng màu Xanh Đen.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
