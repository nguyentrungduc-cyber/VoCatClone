package com.vocatclone.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocatclone.app.R
import com.vocatclone.app.ui.theme.VoCatCloneTheme
import kotlin.math.roundToInt

/**
 * Màn hình tab "Vocabulary" (Từ Vựng): danh sách Tủ sách, mỗi tủ chứa các bộ từ vựng.
 *
 * Màn hình CHỈ vẽ giao diện: dữ liệu và sự kiện được truyền từ ngoài vào (state hoisting),
 * nên xem trước được bằng Preview mà không cần database. Khi nối dữ liệu thật,
 * ViewModel/Repository chỉ cần cấp [shelves] và xử lý các sự kiện `on...Click`.
 *
 * Tìm kiếm ở đây lọc theo TÊN bộ từ vựng ngay trên danh sách đang có.
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    shelves: List<BookshelfUi> = emptyList(),
    onAddClick: () -> Unit = {},
    onBookcaseClick: (Long) -> Unit = {},
    onBookcaseMenuClick: (Long) -> Unit = {},
    onShelfMenuClick: (Long) -> Unit = {},
    onFilterClick: () -> Unit = {},
    onManageShelvesClick: () -> Unit = {}
) {
    var query by rememberSaveable { mutableStateOf("") }
    // Các tủ sách đang thu gọn (mặc định mở hết). Lưu theo id để xoay màn hình không mất.
    var collapsedShelfIds by rememberSaveable { mutableStateOf<Set<Long>>(emptySet()) }

    val visibleShelves = remember(shelves, query) { filterShelves(shelves, query) }

    // Scaffold này nằm TRONG Scaffold của MainApp (đã có bottomBar):
    // - nhận modifier (chứa innerPadding) để nút + không bị thanh điều hướng dưới che mất
    // - contentWindowInsets = 0 để không cộng thêm padding status/navigation bar lần thứ 2
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.home_add_vocabulary))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchRow(
                query = query,
                onQueryChange = { query = it },
                onFilterClick = onFilterClick,
                onManageShelvesClick = onManageShelvesClick
            )

            when {
                shelves.isEmpty() -> CenteredMessage(
                    title = stringResource(R.string.home_empty_title),
                    message = stringResource(R.string.home_empty_message)
                )
                visibleShelves.isEmpty() -> CenteredMessage(
                    title = null,
                    message = stringResource(R.string.home_no_results)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 88.dp)   // chừa chỗ cho nút +
                ) {
                    visibleShelves.forEach { shelf ->
                        val expanded = shelf.id !in collapsedShelfIds
                        item(key = "shelf-${shelf.id}") {
                            ShelfHeader(
                                shelf = shelf,
                                expanded = expanded,
                                onToggle = {
                                    collapsedShelfIds =
                                        if (expanded) collapsedShelfIds + shelf.id else collapsedShelfIds - shelf.id
                                },
                                onMenuClick = { onShelfMenuClick(shelf.id) }
                            )
                        }
                        if (expanded) {
                            items(items = shelf.bookcases, key = { "bookcase-${it.id}" }) { bookcase ->
                                BookcaseCard(
                                    bookcase = bookcase,
                                    onClick = { onBookcaseClick(bookcase.id) },
                                    onMenuClick = { onBookcaseMenuClick(bookcase.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Giữ lại các bộ từ vựng có tên chứa [query] (không phân biệt hoa/thường); tủ không còn bộ nào thì ẩn. */
private fun filterShelves(shelves: List<BookshelfUi>, query: String): List<BookshelfUi> {
    val q = query.trim()
    if (q.isEmpty()) return shelves
    return shelves.mapNotNull { shelf ->
        val matches = shelf.bookcases.filter { it.name.contains(q, ignoreCase = true) }
        if (matches.isEmpty()) null else shelf.copy(bookcases = matches)
    }
}

/** Hàng trên cùng: ô tìm kiếm bo tròn, nút lọc/sắp xếp, nút quản lý tủ sách. */
@Composable
private fun SearchRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    onManageShelvesClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()   // app vẽ tràn viền, tránh bị thanh trạng thái đè lên
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.home_search_hint)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colors.surfaceVariant,
                unfocusedContainerColor = colors.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onFilterClick) {
            Icon(Icons.Outlined.Tune, contentDescription = stringResource(R.string.home_filter))
        }
        IconButton(onClick = onManageShelvesClick) {
            Icon(Icons.Outlined.Inventory2, contentDescription = stringResource(R.string.home_manage_bookcases))
        }
    }
}

/** Tiêu đề một tủ sách: biểu tượng, tên, (tổng số từ), menu ba chấm, mũi tên thu gọn/mở rộng. */
@Composable
private fun ShelfHeader(
    shelf: BookshelfUi,
    expanded: Boolean,
    onToggle: () -> Unit,
    onMenuClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Inventory2, contentDescription = null, tint = colors.onSurface, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = shelf.name,
            style = MaterialTheme.typography.titleLarge,
            color = colors.onSurface,
            maxLines = 1
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "(${shelf.wordCount})",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.home_more_options))
        }
        Icon(
            imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
            contentDescription = stringResource(if (expanded) R.string.home_collapse else R.string.home_expand),
            tint = colors.onSurface,
            modifier = Modifier.padding(end = 12.dp)
        )
    }
    HorizontalDivider(color = colors.outlineVariant)
}

/** Thẻ một bộ từ vựng: cặp ngôn ngữ, tên, số từ / số từ đã thuộc, vòng tròn phần trăm. */
@Composable
private fun BookcaseCard(
    bookcase: BookcaseUi,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)

    Surface(
        shape = shape,
        color = colors.background,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${bookcase.sourceLang} / ${bookcase.targetLang}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.home_more_options))
                }
            }
            Text(
                text = bookcase.name,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                maxLines = 2,
                modifier = Modifier.padding(end = 8.dp)
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Stat(
                    icon = Icons.Outlined.Description,
                    value = bookcase.wordCount,
                    description = stringResource(R.string.home_total_words),
                    tint = colors.onSurfaceVariant
                )
                Spacer(Modifier.width(20.dp))
                Stat(
                    icon = Icons.Outlined.Pets,
                    value = bookcase.masteredCount,
                    description = stringResource(R.string.home_mastered_words),
                    tint = colors.primary
                )
                Spacer(Modifier.weight(1f))
                ProgressRing(progress = bookcase.progress)
            }
        }
    }
}

/** Một con số kèm biểu tượng (tổng số từ, số từ đã thuộc). */
@Composable
private fun Stat(icon: ImageVector, value: Int, description: String, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(imageVector = icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
        Text(text = "$value", style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

/** Vòng tròn tiến độ có chữ phần trăm ở giữa. */
@Composable
private fun ProgressRing(progress: Float) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = colors.primary,
            trackColor = colors.outlineVariant,
            strokeWidth = 3.dp
        )
        Text(
            text = "${(progress * 100).roundToInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onSurface
        )
    }
}

/** Thông báo ở giữa màn hình: danh sách trống hoặc tìm không thấy. */
@Composable
private fun CenteredMessage(title: String?, message: String) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (title != null) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
        }
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

// ---------------------------------------------------------------------------------------
// Preview — dữ liệu mẫu, không dùng database
// ---------------------------------------------------------------------------------------

private val sampleShelves = listOf(
    BookshelfUi(
        id = 1, name = "English vlog-home",
        bookcases = listOf(
            BookcaseUi(1, "Household items", "English (US)", "Vietnamese", wordCount = 80, masteredCount = 78),
            BookcaseUi(2, "Supermarket", "English (US)", "Vietnamese", wordCount = 67, masteredCount = 61),
            BookcaseUi(3, "Hotel", "English (US)", "Vietnamese", wordCount = 86, masteredCount = 86)
        )
    ),
    BookshelfUi(
        id = 2, name = "FULL ENGLISH",
        bookcases = listOf(
            BookcaseUi(4, "Listening A2", "English (US)", "Vietnamese", wordCount = 36, masteredCount = 0)
        )
    )
)

// Xem trước giao diện khi bộ sưu tập trống trơn
@Preview(showBackground = true, name = "Màn hình Trống (0 tủ sách)")
@Composable
fun HomeScreenEmptyPreview() {
    VoCatCloneTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true, name = "Có dữ liệu (tối)", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenDataDarkPreview() {
    VoCatCloneTheme {
        HomeScreen(shelves = sampleShelves)
    }
}

@Preview(showBackground = true, name = "Có dữ liệu (sáng)", uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenDataLightPreview() {
    VoCatCloneTheme {
        HomeScreen(shelves = sampleShelves)
    }
}
