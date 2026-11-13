package com.vocatclone.app.ui.word

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocatclone.app.R
import com.vocatclone.app.ui.home.BookshelfUi
import com.vocatclone.app.ui.theme.VoCatCloneTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Màn hình xem chi tiết MỘT từ vựng. Mở khi bấm vào thẻ từ ở màn danh sách từ (Vocabulary Detail).
 *
 * Hiện: ảnh, từ kèm nút nghe phát âm, phiên âm và loại từ, công tắc "Mastered", danh sách nghĩa
 * có đánh số, ví dụ kèm bản dịch, từ đồng nghĩa, mô tả, và thông tin học (số lần ôn, lần ôn gần nhất,
 * ngày thêm). Phần nào của từ chưa có dữ liệu thì không hiện.
 * Thanh trên có nút sửa và menu ba chấm ([WordActionsMenuButton]: chuyển bộ, đổi ngày giờ, xóa).
 * Xóa luôn hỏi xác nhận trước.
 *
 * Màn hình CHỈ vẽ giao diện, không gọi database; dữ liệu và sự kiện truyền từ ngoài (state hoisting).
 * Ảnh hiện là khung giữ chỗ vì dự án chưa có thư viện tải ảnh.
 *
 * @param bookcaseName tên bộ từ vựng chứa từ này (hiện trên thanh trên).
 * @param onDelete chỉ được gọi SAU khi người dùng bấm xác nhận trong hộp thoại.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    bookcaseName: String,
    word: WordDetailUi,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onSpeak: () -> Unit = {},
    onToggleMastered: () -> Unit = {},
    currentBookcaseId: Long = NO_BOOKCASE,
    moveTargets: List<BookshelfUi> = emptyList(),
    onMove: (targetBookcaseId: Long) -> Unit = {},
    onChangeDateTime: (newMillis: Long) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = bookcaseName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.wd_back))
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.wd_edit))
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // ----- Ảnh (giữ chỗ) -----
            if (word.imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = stringResource(R.string.wd_image),
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // ----- Từ, nút nghe, phiên âm, loại từ -----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = word.term,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onSpeak) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = stringResource(R.string.wd_speak),
                        tint = colors.primary
                    )
                }
            }
            if (!word.pronunciation.isNullOrBlank() || !word.partOfSpeech.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!word.pronunciation.isNullOrBlank()) {
                        Text(
                            text = word.pronunciation,
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.onSurfaceVariant
                        )
                    }
                    if (!word.partOfSpeech.isNullOrBlank()) {
                        PartOfSpeechChip(word.partOfSpeech)
                    }
                }
            }

            // ----- Công tắc đã thuộc -----
            Spacer(Modifier.height(16.dp))
            InfoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Pets,
                        contentDescription = null,
                        tint = if (word.isMastered) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.wd_mastered),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(checked = word.isMastered, onCheckedChange = { onToggleMastered() })
                }
            }

            // ----- Các nghĩa (đánh số) -----
            if (word.meanings.isNotEmpty()) {
                Section(stringResource(R.string.wd_meanings)) {
                    word.meanings.forEachIndexed { index, meaning ->
                        Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.primary,
                                modifier = Modifier.width(28.dp)
                            )
                            Text(
                                text = meaning,
                                style = MaterialTheme.typography.titleMedium,
                                color = colors.onSurface
                            )
                        }
                    }
                }
            }

            // ----- Ví dụ -----
            if (!word.exampleEn.isNullOrBlank() || !word.exampleVi.isNullOrBlank()) {
                Section(stringResource(R.string.wd_example)) {
                    InfoCard {
                        Column {
                            if (!word.exampleEn.isNullOrBlank()) {
                                Text(
                                    text = word.exampleEn,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.onSurface
                                )
                            }
                            if (!word.exampleVi.isNullOrBlank()) {
                                Text(
                                    text = word.exampleVi,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ----- Đồng nghĩa và mô tả -----
            if (!word.synonym.isNullOrBlank()) {
                Section(stringResource(R.string.wd_synonym)) {
                    Text(text = word.synonym, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                }
            }
            if (!word.description.isNullOrBlank()) {
                Section(stringResource(R.string.wd_description)) {
                    Text(text = word.description, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                }
            }

            // ----- Thông tin học -----
            Section(stringResource(R.string.wd_progress)) {
                InfoCard {
                    Column {
                        StatRow(stringResource(R.string.wd_times_reviewed), "${word.reviewCount}")
                        HorizontalDivider(color = colors.outlineVariant, modifier = Modifier.padding(vertical = 10.dp))
                        StatRow(
                            label = stringResource(R.string.wd_last_reviewed),
                            value = word.lastReviewedAt?.let { formatDateTime(it) } ?: stringResource(R.string.wd_never)
                        )
                        HorizontalDivider(color = colors.outlineVariant, modifier = Modifier.padding(vertical = 10.dp))
                        StatRow(stringResource(R.string.wd_added), formatDateTime(word.createdAt))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun formatDateTime(millis: Long): String =
    SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault()).format(Date(millis))

/** Một nhóm có tiêu đề (Meanings, Example...). */
@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

/** Khung nền có viền, dùng chung cho công tắc, ví dụ và thông tin học. */
@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
    }
}

@Composable
private fun PartOfSpeechChip(text: String) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = RoundedCornerShape(50), color = Color.Transparent, border = BorderStroke(1.dp, colors.primary)) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = colors.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
        )
    }
}

// ---------------------------------------------------------------------------------------
// Preview — dữ liệu mẫu, không dùng database
// ---------------------------------------------------------------------------------------

private val fullWord = WordDetailUi(
    id = 1, term = "toothpaste", pronunciation = "/ˈtuːθ.peɪst/", partOfSpeech = "noun",
    meanings = listOf("kem đánh răng"),
    exampleEn = "I need to buy more toothpaste.", exampleVi = "Tôi cần mua thêm kem đánh răng.",
    synonym = "dentifrice",
    description = "a thick substance that you put on a toothbrush to clean your teeth",
    imageUri = "content://sample/1", isMastered = true, reviewCount = 7,
    lastReviewedAt = 1_786_300_000_000, createdAt = 1_786_000_000_000
)

private val multiMeaningWord = WordDetailUi(
    id = 2, term = "cook", pronunciation = "/kʊk/", partOfSpeech = "verb",
    meanings = listOf("nấu ăn", "đầu bếp", "chín", "đồ ăn chín"),
    description = "to prepare food for eating by heating it",
    reviewCount = 2, lastReviewedAt = 1_786_250_000_000, createdAt = 1_786_100_000_000
)

private val minimalWord = WordDetailUi(
    id = 3, term = "extension cord", meanings = listOf("dây nối dài"), createdAt = 1_786_200_000_000
)

@Preview(showBackground = true, name = "Đầy đủ (tối)", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 1300)
@Composable
private fun WordDetailFullDarkPreview() {
    VoCatCloneTheme { WordDetailScreen(bookcaseName = "Household items", word = fullWord) }
}

@Preview(showBackground = true, name = "Nhiều nghĩa (sáng)", uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 390, heightDp = 844)
@Composable
private fun WordDetailMultiMeaningLightPreview() {
    VoCatCloneTheme { WordDetailScreen(bookcaseName = "Household items", word = multiMeaningWord) }
}

@Preview(showBackground = true, name = "Từ tối giản (chưa ôn lần nào)", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun WordDetailMinimalPreview() {
    VoCatCloneTheme { WordDetailScreen(bookcaseName = "Household items", word = minimalWord) }
}
