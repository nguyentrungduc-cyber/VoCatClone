package com.vocatclone.app.ui.bookcase

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocatclone.app.R
import com.vocatclone.app.ui.home.BookshelfUi
import com.vocatclone.app.ui.theme.VoCatCloneTheme
import com.vocatclone.app.ui.word.NO_BOOKCASE
import com.vocatclone.app.ui.word.WordActionsMenuButton
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Màn hình danh sách từ của một bộ từ vựng (Vocabulary Detail).
 * Người dùng vào đây khi bấm một thẻ bộ từ vựng ở màn Home.
 *
 * Gồm: thanh trên (quay lại, tên bộ, nút sắp xếp/lọc, nút thêm từ), ô tìm kiếm, dòng "Total N",
 * và danh sách thẻ từ. Mỗi thẻ có: số thứ tự và ngày giờ tạo, nút nghe phát âm, nút đã thuộc (chân mèo),
 * menu ba chấm (WordActionsMenuButton: sửa, chuyển bộ, đổi ngày giờ, xóa), từ, phiên âm, loại từ, các nghĩa, mô tả.
 *
 * Màn hình CHỈ vẽ giao diện: không gọi database. Dữ liệu và sự kiện truyền từ ngoài vào (state hoisting).
 * Tìm kiếm lọc ngay trên danh sách đang có, theo từ hoặc theo nghĩa.
 *
 * Lưu ý về ảnh: dự án chưa có thư viện tải ảnh (Coil...). Từ có [WordItemUi.imageUri] hiện chỉ
 * hiện khung ảnh giữ chỗ; khi thêm thư viện thì thay khung đó bằng ảnh thật.
 *
 * Màn này là một màn đầy đủ (có Scaffold riêng). Nếu đặt vào trong MainApp (đã có thanh tab dưới)
 * thì cần xử lý lề (inset) giống cách HomeScreen đang làm.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookcaseDetailScreen(
    bookcaseName: String,
    words: List<WordItemUi>,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onAddWord: () -> Unit = {},
    onFilterClick: () -> Unit = {},
    onWordClick: (Long) -> Unit = {},
    onSpeak: (Long) -> Unit = {},
    onToggleMastered: (Long) -> Unit = {},
    currentBookcaseId: Long = NO_BOOKCASE,
    moveTargets: List<BookshelfUi> = emptyList(),
    onEditWord: (wordId: Long) -> Unit = {},
    onMoveWord: (wordId: Long, targetBookcaseId: Long) -> Unit = { _, _ -> },
    onChangeDateTime: (wordId: Long, newMillis: Long) -> Unit = { _, _ -> },
    onDeleteWord: (wordId: Long) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var query by rememberSaveable { mutableStateOf("") }
    val visibleWords = remember(words, query) { filterWords(words, query) }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = bookcaseName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
                    }
                },
                actions = {
                    IconButton(onClick = onFilterClick) {
                        Icon(Icons.Outlined.Tune, contentDescription = stringResource(R.string.detail_filter))
                    }
                    IconButton(onClick = onAddWord) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.detail_add_word))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                    navigationIconContentColor = colors.onBackground,
                    actionIconContentColor = colors.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.detail_search_hint)) },
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Text(
                text = stringResource(R.string.detail_total, visibleWords.size),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            when {
                words.isEmpty() -> CenteredMessage(
                    title = stringResource(R.string.detail_empty_title),
                    message = stringResource(R.string.detail_empty_message)
                )
                visibleWords.isEmpty() -> CenteredMessage(
                    title = null,
                    message = stringResource(R.string.detail_no_results)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(items = visibleWords, key = { _, word -> word.id }) { index, word ->
                        WordCard(
                            number = index + 1,
                            word = word,
                            onClick = { onWordClick(word.id) },
                            onSpeak = { onSpeak(word.id) },
                            onToggleMastered = { onToggleMastered(word.id) },
                            currentBookcaseId = currentBookcaseId,
                            moveTargets = moveTargets,
                            onEdit = { onEditWord(word.id) },
                            onMove = { targetId -> onMoveWord(word.id, targetId) },
                            onChangeDateTime = { millis -> onChangeDateTime(word.id, millis) },
                            onDelete = { onDeleteWord(word.id) }
                        )
                    }
                }
            }
        }
    }
}

/** Giữ lại các từ có từ hoặc một nghĩa chứa [query] (không phân biệt hoa/thường). */
private fun filterWords(words: List<WordItemUi>, query: String): List<WordItemUi> {
    val q = query.trim()
    if (q.isEmpty()) return words
    return words.filter { word ->
        word.term.contains(q, ignoreCase = true) || word.meanings.any { it.contains(q, ignoreCase = true) }
    }
}

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault()).format(Date(millis))

/** Thẻ một từ. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordCard(
    number: Int,
    word: WordItemUi,
    onClick: () -> Unit,
    onSpeak: () -> Unit,
    onToggleMastered: () -> Unit,
    currentBookcaseId: Long,
    moveTargets: List<BookshelfUi>,
    onEdit: () -> Unit,
    onMove: (targetBookcaseId: Long) -> Unit,
    onChangeDateTime: (newMillis: Long) -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val dateText = remember(word.createdAt) { formatDateTime(word.createdAt) }

    Surface(
        shape = shape,
        color = colors.background,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 16.dp)) {
            // Hàng đầu: số thứ tự, ngày giờ tạo và ba nút
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$number · $dateText",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSpeak) {
                    Icon(Icons.AutoMirrored.Outlined.VolumeUp, contentDescription = stringResource(R.string.detail_speak))
                }
                IconButton(onClick = onToggleMastered) {
                    Icon(
                        imageVector = Icons.Outlined.Pets,
                        contentDescription = stringResource(
                            if (word.isMastered) R.string.detail_mark_not_mastered else R.string.detail_mark_mastered
                        ),
                        tint = if (word.isMastered) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                WordActionsMenuButton(
                    wordTerm = word.term,
                    createdAtMillis = word.createdAt,
                    currentBookcaseId = currentBookcaseId,
                    moveTargets = moveTargets,
                    onEdit = onEdit,
                    onMoveTo = onMove,
                    onChangeDateTime = onChangeDateTime,
                    onDelete = onDelete
                )
            }

            // Ảnh (giữ chỗ), từ, phiên âm, loại từ
            Row(modifier = Modifier.padding(end = 12.dp), verticalAlignment = Alignment.Top) {
                if (word.imageUri != null) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Image, contentDescription = null, tint = colors.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.term,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!word.pronunciation.isNullOrBlank()) {
                            Text(
                                text = word.pronunciation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant
                            )
                        }
                        if (!word.partOfSpeech.isNullOrBlank()) {
                            PartOfSpeechChip(word.partOfSpeech)
                        }
                    }
                }
            }

            // Các nghĩa (xuống dòng khi hết chỗ)
            if (word.meanings.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 12.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    word.meanings.forEach { MeaningChip(it) }
                }
            }

            if (!word.description.isNullOrBlank()) {
                Text(
                    text = word.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 12.dp, end = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun MeaningChip(text: String) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(50), color = colors.surfaceVariant) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PartOfSpeechChip(text: String) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.Transparent,
        border = BorderStroke(1.dp, colors.primary)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
        )
    }
}

/** Thông báo ở giữa màn hình: chưa có từ nào hoặc tìm không thấy. */
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
            Icon(Icons.Outlined.Book, contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.size(56.dp))
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

private val sampleWords = listOf(
    WordItemUi(
        id = 1, term = "toothpaste", pronunciation = "/ˈtuːθ.peɪst/", partOfSpeech = "noun",
        meanings = listOf("kem đánh răng"),
        description = "a thick substance that you put on a toothbrush to clean your teeth",
        imageUri = "content://sample/1", isMastered = true, createdAt = 1_786_000_000_000
    ),
    WordItemUi(
        id = 2, term = "cook", pronunciation = "/kʊk/", partOfSpeech = "verb",
        meanings = listOf("đầu bếp", "nấu ăn", "chín", "đồ ăn chín"),
        description = "to prepare food for eating by heating it",
        createdAt = 1_786_100_000_000
    ),
    WordItemUi(
        id = 3, term = "extension cord", pronunciation = null, partOfSpeech = "noun",
        meanings = listOf("dây nối dài"),
        description = null, imageUri = "content://sample/3", createdAt = 1_786_200_000_000
    )
)

@Preview(showBackground = true, name = "Có từ (tối)", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun BookcaseDetailDarkPreview() {
    VoCatCloneTheme { BookcaseDetailScreen(bookcaseName = "Household items", words = sampleWords) }
}

@Preview(showBackground = true, name = "Có từ (sáng)", uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 390, heightDp = 844)
@Composable
private fun BookcaseDetailLightPreview() {
    VoCatCloneTheme { BookcaseDetailScreen(bookcaseName = "Household items", words = sampleWords) }
}

@Preview(showBackground = true, name = "Chưa có từ", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun BookcaseDetailEmptyPreview() {
    VoCatCloneTheme { BookcaseDetailScreen(bookcaseName = "Household items", words = emptyList()) }
}
