package com.vocatclone.app.ui.profile

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocatclone.app.R
import com.vocatclone.app.ui.components.MainTab
import com.vocatclone.app.ui.components.VoCatBottomBar
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Màn hình "Tôi" (tab "Của tôi") — bám theo màn hình cùng tên của app VoCat gốc.
 *
 * Màn hình này CHỈ vẽ giao diện: không gọi Firebase hay database. Dữ liệu và sự kiện
 * được truyền từ ngoài vào (state hoisting) nên xem trước (Preview) được mà không cần đăng nhập.
 *
 * @param userEmail email đang đăng nhập; null nghĩa là chưa đăng nhập.
 */
@OptIn(ExperimentalMaterial3Api::class) // cam kết chấp nhận de sai Material3 do dang thu nghiem

@Composable
fun ProfileScreen(
    userEmail: String?,
    modifier: Modifier = Modifier,
    appVersion: String = "v0.1.0",
    vocabNotificationOn: Boolean = true,
    themeName: String = "Nero / White",
    onAnnouncementClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onAccountClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onUpgradeClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onInviteClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxSize().background(colors.background)) {
        CenterAlignedTopAppBar(
            title = { Text("My") },
            navigationIcon = {
                IconButton(onClick = onAnnouncementClick) {
                    Icon(Icons.Outlined.Campaign, contentDescription = "Developer announcement")
                }
            },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = colors.background,
                titleContentColor = colors.onBackground,
                navigationIconContentColor = colors.onBackground,
                actionIconContentColor = colors.onBackground
            )
        )

        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            AnnouncementCard(
                title = "$appVersion Update",
                subtitle = "Added Widget & Search Fields Options",
                page = 1,
                pageCount = 3,
                onClick = onAnnouncementClick
            )

            BackupNotice(onClick = onBackupClick)
            HorizontalDivider(color = colors.outlineVariant)

            // ----- Group 1: Account & Subscription -----
            ProfileRow(
                icon = Icons.Outlined.Person,
                title = "Account",
                value = if (userEmail != null) "GOOGLE" else "Login Required",
                onClick = onAccountClick
            )
            ProfileRow(
                icon = Icons.Outlined.StarOutline,
                title = "Subscription",
                value = "Upgrade",
                iconTint = Color(0xFFFF6FB5),
                onClick = onUpgradeClick
            )

            GroupGap()

            // ----- Group 2: Settings -----
            ProfileRow(
                icon = Icons.Outlined.Notifications,
                title = "Word Notification",
                value = if (vocabNotificationOn) "ON" else "OFF",
                onClick = onNotificationClick
            )
            ProfileRow(
                icon = Icons.Outlined.Palette,
                title = "Theme",
                value = themeName,
                onClick = onThemeClick
            )
            ProfileRow(
                icon = Icons.Outlined.Group,
                title = "Invite friends",
                value = "0",
                onClick = onInviteClick
            )

            if (userEmail != null) {
                GroupGap()
                ProfileRow(
                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                    title = "Sign out",
                    value = "",
                    onClick = onSignOutClick
                )
            }
        }
    }
}

/** Thẻ thông báo phiên bản có viền bo tròn, 3 chấm phân trang ở góc phải dưới. */
@Composable
private fun AnnouncementCard(
    title: String,
    subtitle: String,
    page: Int,
    pageCount: Int,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
            .clip(shape)
            .border(BorderStroke(1.dp, colors.outline), shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(end = 32.dp, bottom = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = colors.primary)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSurface,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (index == page) colors.secondary else colors.outline)
                )
            }
        }
    }
}

/** Nhắc người dùng sao lưu. Đoạn cảnh báo được tô màu nhấn như app gốc. */
@Composable
private fun BackupNotice(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val message = buildAnnotatedString {
        append("All data is stored on the device, ")
        withStyle(SpanStyle(color = colors.primary)) {
            append("so data can be lost when deleting an app or app data. Please back up regularly through Google Drive Sync.")
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_google_drive_logo),
            contentDescription = "Google Drive",
            tint = Color.Unspecified,
            modifier = Modifier.size(28.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = "Google Drive Data Sync",
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
    }
}

/** Một dòng: icon - tiêu đề - giá trị (mờ, nằm bên phải) - mũi tên. */
@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(text = value, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.onSurface
        )
    }
    HorizontalDivider(color = colors.outlineVariant)
}

/** Dải ngăn giữa hai nhóm dòng. */
@Composable
private fun GroupGap() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 400, heightDp = 860)
@Composable
private fun ProfileScreenPreviewLoggedOut() {
    VoCatCloneTheme {
        Scaffold(bottomBar = { VoCatBottomBar(selected = MainTab.PROFILE, onSelect = {}) }) { padding ->
            ProfileScreen(userEmail = null, modifier = Modifier.padding(padding))
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 400, heightDp = 860)
@Composable
private fun ProfileScreenPreviewLoggedIn() {
    VoCatCloneTheme {
        Scaffold(bottomBar = { VoCatBottomBar(selected = MainTab.PROFILE, onSelect = {}) }) { padding ->
            ProfileScreen(userEmail = "test@gmail.com", modifier = Modifier.padding(padding))
        }
    }
}
