package com.vocatclone.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** 5 tab chính của app, giống thanh điều hướng dưới cùng của VoCat. */
enum class MainTab(val label: String, val icon: ImageVector) {
    ADD("Thêm vào", Icons.Filled.Edit),
    VOCAB("Từ Vựng", Icons.Filled.Book),
    QUIZ("Câu đố", Icons.Filled.Quiz),
    CALENDAR("Lịch", Icons.Filled.CalendarMonth),
    PROFILE("Của tôi", Icons.Filled.Pets)
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
                label = { Text(tab.label) },
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
