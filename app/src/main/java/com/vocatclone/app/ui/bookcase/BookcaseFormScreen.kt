package com.vocatclone.app.ui.bookcase

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vocatclone.app.R
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Màn hình Tạo / Sửa bộ từ vựng (VoCat gốc gọi là "Vocabulary").
 *
 * Gồm: ô chọn Tủ sách, ô nhập tên (tối đa [BOOKCASE_NAME_MAX] ký tự, có bộ đếm),
 * cặp ngôn ngữ Word / Meaning (có nút đổi chỗ), và nút tạo (hoặc lưu) ở đáy màn hình.
 * Nút chỉ bấm được khi đã chọn tủ sách và nhập tên.
 *
 * Màn hình CHỈ vẽ giao diện: không gọi database. Danh sách tủ sách truyền vào qua [shelves],
 * kết quả trả ra qua [onSubmit]. Khi nối dữ liệu thật, ViewModel cấp [shelves] và lưu giá trị nhận được.
 *
 * @param initial giá trị ban đầu; dùng khi sửa một bộ từ vựng có sẵn.
 * @param isEditing true: tiêu đề "Edit Vocabulary" và nút "Save changes".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookcaseFormScreen(
    shelves: List<ShelfOption>,
    modifier: Modifier = Modifier,
    initial: BookcaseFormValues = BookcaseFormValues(),
    isEditing: Boolean = false,
    languages: List<String> = DEFAULT_LANGUAGES,
    onBack: () -> Unit = {},
    onSubmit: (BookcaseFormValues) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var shelfId by rememberSaveable { mutableStateOf(initial.shelfId) }
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var sourceLang by rememberSaveable { mutableStateOf(initial.sourceLang) }
    var targetLang by rememberSaveable { mutableStateOf(initial.targetLang) }

    val canSubmit = shelfId != null && name.isNotBlank()
    val selectedShelfName = shelves.firstOrNull { it.id == shelfId }?.name.orEmpty()

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(if (isEditing) R.string.form_title_edit else R.string.form_title_create))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.form_back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                    navigationIconContentColor = colors.onBackground
                )
            )
        },
        bottomBar = {
            // navigationBarsPadding + imePadding: nút không bị thanh điều hướng hay bàn phím che
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = { onSubmit(BookcaseFormValues(shelfId, name.trim(), sourceLang, targetLang)) },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(Icons.Outlined.Pets, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(if (isEditing) R.string.form_submit_save else R.string.form_submit_create),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // ----- Chọn Tủ sách -----
            DropdownField(
                label = stringResource(R.string.form_bookcase_label),
                value = selectedShelfName,
                options = shelves.map { it.name },
                enabled = shelves.isNotEmpty(),
                onSelect = { index -> shelfId = shelves[index].id }
            )

            Spacer(Modifier.height(32.dp))

            // ----- Tên bộ từ vựng (tối đa 40 ký tự) -----
            SectionHeader(icon = Icons.Outlined.Book, title = stringResource(R.string.form_name_header))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= BOOKCASE_NAME_MAX) name = it },
                placeholder = { Text(stringResource(R.string.form_name_hint)) },
                singleLine = true,
                supportingText = {
                    Text(
                        text = "${name.length} / $BOOKCASE_NAME_MAX",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // ----- Cặp ngôn ngữ: Word (từ) và Meaning (nghĩa) -----
            SectionHeader(icon = Icons.Outlined.Translate, title = stringResource(R.string.form_language_header))
            Spacer(Modifier.height(8.dp))
            LanguageRow(
                label = stringResource(R.string.form_word_language),
                value = sourceLang,
                options = languages,
                onSelect = { sourceLang = it }
            )
            IconButton(
                onClick = {
                    val old = sourceLang
                    sourceLang = targetLang
                    targetLang = old
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.SwapVert,
                    contentDescription = stringResource(R.string.form_swap),
                    tint = colors.primary
                )
            }
            LanguageRow(
                label = stringResource(R.string.form_meaning_language),
                value = targetLang,
                options = languages,
                onSelect = { targetLang = it }
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Tiêu đề một nhóm: biểu tượng và chữ lớn. */
@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Ô chọn một giá trị trong danh sách: khung viền, bấm vào mở danh sách thả xuống. */
@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    enabled: Boolean,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            trailingIcon = { Icon(Icons.Outlined.ExpandMore, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        // Lớp trong suốt phủ lên ô để bắt thao tác bấm (ô chỉ-đọc không tự mở danh sách)
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(index)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** Một dòng chọn ngôn ngữ: nhãn bên trái, giá trị (màu nhấn) và mũi tên bên phải. */
@Composable
private fun LanguageRow(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = colors.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(text = value, style = MaterialTheme.typography.titleMedium, color = colors.primary)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.ExpandMore, contentDescription = null, tint = colors.primary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------
// Preview — dữ liệu mẫu, không dùng database
// ---------------------------------------------------------------------------------------

private val sampleShelves = listOf(
    ShelfOption(1, "English vlog-home"),
    ShelfOption(2, "FULL ENGLISH")
)

@Preview(showBackground = true, name = "Tạo mới (tối)", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun BookcaseFormCreateDarkPreview() {
    VoCatCloneTheme { BookcaseFormScreen(shelves = sampleShelves) }
}

@Preview(showBackground = true, name = "Tạo mới (sáng)", uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 390, heightDp = 844)
@Composable
private fun BookcaseFormCreateLightPreview() {
    VoCatCloneTheme { BookcaseFormScreen(shelves = sampleShelves) }
}

@Preview(showBackground = true, name = "Sửa (đã nhập sẵn)", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun BookcaseFormEditPreview() {
    VoCatCloneTheme {
        BookcaseFormScreen(
            shelves = sampleShelves,
            isEditing = true,
            initial = BookcaseFormValues(shelfId = 1, name = "Household items", sourceLang = "English (US)", targetLang = "Vietnamese")
        )
    }
}
