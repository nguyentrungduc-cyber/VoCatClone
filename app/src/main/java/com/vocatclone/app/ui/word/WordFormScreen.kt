package com.vocatclone.app.ui.word

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vocatclone.app.R
import com.vocatclone.app.ui.theme.VoCatCloneTheme

/**
 * Màn hình Thêm / Sửa từ vựng.
 *
 * Gồm: ảnh minh họa, ô nhập từ (có nút tra gợi ý nghĩa), danh sách gợi ý nghĩa, NHIỀU ô nghĩa
 * (thêm/xóa từng ô), và các trường tùy chọn (phát âm, loại từ, ví dụ, đồng nghĩa, mô tả).
 * Hộp thoại "Editor Settings" cho chọn trường nào được hiện.
 *
 * Màn hình CHỈ vẽ giao diện và KHÔNG tự giữ dữ liệu: [values] và [visibleFields] truyền từ ngoài vào,
 * mọi thay đổi trả ra qua [onValuesChange] và [onVisibleFieldsChange] (ViewModel giữ state).
 * Nhờ vậy kết quả chọn ảnh, kết quả tra nghĩa... do ViewModel cập nhật rồi truyền lại xuống.
 * Chưa gọi database, camera, thư viện ảnh hay API dịch: các việc đó nối qua callback
 * [onPickImage], [onLookup] (kèm [suggestions], [isLookingUp], [lookupError]) và [onOpenDictionary].
 *
 * Ghi chú: dự án chưa có thư viện tải ảnh nên ảnh hiện là khung giữ chỗ; Editor Settings chưa được
 * lưu giữa các lần mở app (cần DataStore hoặc SharedPreferences, ViewModel làm sau).
 *
 * @param bookcaseName tên bộ từ vựng đang thêm từ vào (hiện trên thanh trên).
 * @param wordLanguage, meaningLanguage cặp ngôn ngữ của bộ, dùng làm nhãn ô từ và ô nghĩa.
 * @param isEditing true: nút "Save", không có nút "thêm tiếp".
 * @param onSubmit bấm Add/Save; tham số true nếu bấm nút "thêm rồi nhập tiếp".
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordFormScreen(
    bookcaseName: String,
    values: WordFormValues,
    onValuesChange: (WordFormValues) -> Unit,
    visibleFields: Set<WordField>,
    onVisibleFieldsChange: (Set<WordField>) -> Unit,
    modifier: Modifier = Modifier,
    wordLanguage: String = "English (US)",
    meaningLanguage: String = "Vietnamese",
    isEditing: Boolean = false,
    suggestions: List<String> = emptyList(),
    isLookingUp: Boolean = false,
    lookupError: String? = null,
    onBack: () -> Unit = {},
    onSubmit: (addAnother: Boolean) -> Unit = {},
    onPickImage: () -> Unit = {},
    onLookup: (term: String) -> Unit = {},
    onOpenDictionary: (term: String) -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val canSubmit = values.canSubmit
    fun shown(field: WordField) = field in visibleFields || values.hasValue(field)

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.wf_set_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onSurfaceVariant
                        )
                        Text(
                            text = bookcaseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.wf_back))
                    }
                },
                actions = {
                    TextButton(onClick = { onSubmit(false) }, enabled = canSubmit) {
                        Text(stringResource(if (isEditing) R.string.wf_action_save else R.string.wf_action_add))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                    navigationIconContentColor = colors.onBackground,
                    actionIconContentColor = colors.onBackground
                )
            )
        },
        floatingActionButton = {
            // Nút tròn "thêm rồi nhập tiếp": chỉ hiện khi đang thêm mới và form đã hợp lệ
            if (!isEditing && canSubmit) {
                FloatingActionButton(onClick = { onSubmit(true) }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.wf_add_another))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ----- Ảnh minh họa -----
            if (shown(WordField.IMAGE)) {
                ImagePickerBox(
                    hasImage = values.imageUri != null,
                    onPick = onPickImage,
                    onClear = { onValuesChange(values.copy(imageUri = null)) }
                )
                Spacer(Modifier.height(16.dp))
            }

            // ----- Từ (bắt buộc) + nút tra gợi ý nghĩa -----
            OutlinedTextField(
                value = values.term,
                onValueChange = { onValuesChange(values.copy(term = it)) },
                label = { Text(wordLanguage) },
                placeholder = { Text(stringResource(R.string.wf_term_placeholder)) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { onLookup(values.term.trim()) }, enabled = values.term.isNotBlank()) {
                        Icon(Icons.Outlined.Search, contentDescription = stringResource(R.string.wf_lookup))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // ----- Gợi ý nghĩa: đang tra, lỗi, hoặc danh sách để bấm thêm -----
            SuggestionArea(
                isLookingUp = isLookingUp,
                error = lookupError,
                suggestions = suggestions,
                chosen = values.meanings,
                onPick = { onValuesChange(values.withMeaning(it)) }
            )

            Spacer(Modifier.height(16.dp))

            // ----- Nhiều nghĩa -----
            values.meanings.forEachIndexed { index, meaning ->
                OutlinedTextField(
                    value = meaning,
                    onValueChange = { text ->
                        onValuesChange(values.copy(meanings = values.meanings.mapIndexed { i, m -> if (i == index) text else m }))
                    },
                    label = { Text(meaningLanguage) },
                    placeholder = {
                        Text(stringResource(if (index == 0) R.string.wf_meaning_required else R.string.wf_meaning_optional))
                    },
                    singleLine = true,
                    trailingIcon = if (values.meanings.size > 1) {
                        {
                            IconButton(
                                onClick = { onValuesChange(values.copy(meanings = values.meanings.filterIndexed { i, _ -> i != index })) }
                            ) {
                                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.wf_meaning_remove))
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
            }
            TextButton(onClick = { onValuesChange(values.copy(meanings = values.meanings + "")) }) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.wf_meaning_add))
            }

            // ----- Các trường tùy chọn -----
            if (shown(WordField.PRONUNCIATION)) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = values.pronunciation,
                    onValueChange = { onValuesChange(values.copy(pronunciation = it)) },
                    label = { Text(stringResource(R.string.wf_pronunciation_label)) },
                    placeholder = { Text(stringResource(R.string.wf_pronunciation_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (shown(WordField.PART_OF_SPEECH)) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.wf_pos_label),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PART_OF_SPEECH_OPTIONS.forEach { option ->
                        FilterChip(
                            selected = values.partOfSpeech == option,
                            onClick = {
                                onValuesChange(values.copy(partOfSpeech = if (values.partOfSpeech == option) null else option))
                            },
                            label = { Text(option) }
                        )
                    }
                }
            }
            if (shown(WordField.EXAMPLE)) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = values.exampleEn,
                    onValueChange = { onValuesChange(values.copy(exampleEn = it)) },
                    label = { Text(stringResource(R.string.wf_example_label)) },
                    placeholder = { Text(stringResource(R.string.wf_example_hint)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = values.exampleVi,
                    onValueChange = { onValuesChange(values.copy(exampleVi = it)) },
                    label = { Text(stringResource(R.string.wf_example_translation_label)) },
                    placeholder = { Text(stringResource(R.string.wf_example_translation_hint)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (shown(WordField.SYNONYM)) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = values.synonym,
                    onValueChange = { onValuesChange(values.copy(synonym = it)) },
                    label = { Text(stringResource(R.string.wf_synonym_label)) },
                    placeholder = { Text(stringResource(R.string.wf_synonym_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (shown(WordField.DESCRIPTION)) {
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = values.description,
                    onValueChange = { onValuesChange(values.copy(description = it)) },
                    label = { Text(stringResource(R.string.wf_description_label)) },
                    placeholder = { Text(stringResource(R.string.wf_description_hint)) },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ----- Liên kết: Editor Settings và Dictionary -----
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { showSettings = true }) {
                    Text(stringResource(R.string.wf_editor_settings))
                }
                TextButton(onClick = { onOpenDictionary(values.term.trim()) }) {
                    Text(stringResource(R.string.wf_dictionary))
                }
            }
            Spacer(Modifier.height(96.dp))   // chừa chỗ cho nút tròn
        }
    }

    if (showSettings) {
        EditorSettingsDialog(
            values = values,
            visibleFields = visibleFields,
            onChange = onVisibleFieldsChange,
            onDismiss = { showSettings = false }
        )
    }
}

/** Ô thêm ảnh: bấm để chọn ảnh; khi đã có ảnh thì hiện khung ảnh (giữ chỗ) kèm nút xóa. */
@Composable
private fun ImagePickerBox(hasImage: Boolean, onPick: () -> Unit, onClear: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceVariant)
                .clickable(onClick = onPick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (hasImage) Icons.Outlined.Image else Icons.Outlined.AddPhotoAlternate,
                contentDescription = stringResource(R.string.wf_image_add),
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
        }
        if (hasImage) {
            IconButton(
                onClick = onClear,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.wf_image_remove), modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** Khu vực gợi ý nghĩa dưới ô nhập từ. Không hiện gì khi chưa tra. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionArea(
    isLookingUp: Boolean,
    error: String?,
    suggestions: List<String>,
    chosen: List<String>,
    onPick: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    when {
        isLookingUp -> Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.wf_looking_up),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant
            )
        }
        error != null -> Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.error,
            modifier = Modifier.padding(top = 12.dp)
        )
        suggestions.isNotEmpty() -> Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = stringResource(R.string.wf_suggestions_title),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { suggestion ->
                    val added = chosen.any { it.trim().equals(suggestion.trim(), ignoreCase = true) }
                    FilterChip(
                        selected = added,
                        onClick = { onPick(suggestion) },
                        label = { Text(suggestion) }
                    )
                }
            }
        }
    }
}

@StringRes
private fun fieldLabel(field: WordField): Int = when (field) {
    WordField.IMAGE -> R.string.wf_image_label
    WordField.PRONUNCIATION -> R.string.wf_pronunciation_label
    WordField.PART_OF_SPEECH -> R.string.wf_pos_label
    WordField.EXAMPLE -> R.string.wf_example_label
    WordField.SYNONYM -> R.string.wf_synonym_label
    WordField.DESCRIPTION -> R.string.wf_description_label
}

/** Hộp thoại chọn trường nào được hiện trong form. Trường đã có dữ liệu thì bị khóa ở trạng thái bật. */
@Composable
private fun EditorSettingsDialog(
    values: WordFormValues,
    visibleFields: Set<WordField>,
    onChange: (Set<WordField>) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wf_settings_title)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.wf_settings_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                WordField.entries.forEach { field ->
                    val locked = values.hasValue(field)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(fieldLabel(field)),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = field in visibleFields || locked,
                            enabled = !locked,
                            onCheckedChange = { on -> onChange(if (on) visibleFields + field else visibleFields - field) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.wf_settings_done)) }
        }
    )
}

// ---------------------------------------------------------------------------------------
// Preview — dữ liệu mẫu, không dùng database. Mỗi Preview tự giữ state như một ViewModel nhỏ.
// ---------------------------------------------------------------------------------------

@Composable
private fun WordFormPreviewHost(
    initial: WordFormValues,
    initialVisible: Set<WordField>,
    isEditing: Boolean = false,
    suggestions: List<String> = emptyList()
) {
    var values by remember { mutableStateOf(initial) }
    var visible by remember { mutableStateOf(initialVisible) }
    VoCatCloneTheme {
        WordFormScreen(
            bookcaseName = "Household items",
            values = values,
            onValuesChange = { values = it },
            visibleFields = visible,
            onVisibleFieldsChange = { visible = it },
            isEditing = isEditing,
            suggestions = suggestions
        )
    }
}

@Preview(showBackground = true, name = "Thêm mới, có gợi ý (tối)", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun WordFormAddDarkPreview() {
    WordFormPreviewHost(
        initial = WordFormValues(term = "cook", meanings = listOf("nấu ăn", "")),
        initialVisible = DEFAULT_VISIBLE_FIELDS,
        suggestions = listOf("nấu ăn", "đầu bếp", "chín", "đồ ăn chín")
    )
}

@Preview(showBackground = true, name = "Sửa, đủ trường (sáng)", uiMode = Configuration.UI_MODE_NIGHT_NO, widthDp = 390, heightDp = 1400)
@Composable
private fun WordFormEditLightPreview() {
    WordFormPreviewHost(
        initial = WordFormValues(
            term = "toothpaste", meanings = listOf("kem đánh răng"), pronunciation = "/ˈtuːθ.peɪst/",
            partOfSpeech = "noun", exampleEn = "I need to buy more toothpaste.",
            exampleVi = "Tôi cần mua thêm kem đánh răng.", synonym = "dentifrice",
            description = "a thick substance that you put on a toothbrush to clean your teeth",
            imageUri = "content://sample/1"
        ),
        initialVisible = WordField.entries.toSet(),
        isEditing = true
    )
}

@Preview(showBackground = true, name = "Form trống", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 844)
@Composable
private fun WordFormEmptyPreview() {
    WordFormPreviewHost(initial = WordFormValues(), initialVisible = DEFAULT_VISIBLE_FIELDS)
}
