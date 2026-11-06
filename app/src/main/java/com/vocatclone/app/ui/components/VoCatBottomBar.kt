package com.vocatclone.app.ui.components

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/** 5 tab chính của app, giống thanh điều hướng dưới cùng của VoCat. */
enum class MainTab(val label: String, val icon: ImageVector) {
    ADD("Add", Icons.Outlined.Edit),
    VOCAB("Vocabulary", Icons.Outlined.Book),
    QUIZ("Quiz", Icons.Outlined.Quiz),
    CALENDAR("Calendar", Icons.Outlined.CalendarMonth),
    PROFILE("My", Icons.Outlined.Pets)
}

@Composable
fun VoCatBottomBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    NavigationBar(containerColor = colors.surface) {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = {
                    Text(
                        text = tab.label,
                        maxLines = 1,
                        softWrap = false
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.secondary,      // tab đang chọn: màu vàng
                    selectedTextColor = colors.secondary,
                    indicatorColor = Color.Transparent,        // không có nền tròn quanh icon
                    unselectedIconColor = colors.onSurfaceVariant,
                    unselectedTextColor = colors.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VoCatBottomBarPreview() {
    VoCatCloneTheme {
        VoCatBottomBar(
            selected = MainTab.VOCAB,
            onSelect = {}
        )
    }
}

