package com.vocatclone.app.ui.word

import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vocatclone.app.R
import com.vocatclone.app.ui.home.BookcaseUi
import com.vocatclone.app.ui.home.BookshelfUi
import com.vocatclone.app.ui.theme.VoCatCloneTheme
import java.util.Calendar
import java.util.TimeZone

/** Hộp thoại đang mở (mỗi lúc chỉ có một). */
private enum class ActionDialog { NONE, MOVE, DATE_TIME, DELETE }

/**
 * Nút ba chấm kèm menu thao tác trên MỘT từ, dùng chung cho màn danh sách từ và màn chi tiết từ.
 *
 * Menu có 4 mục: Chỉnh sửa, Chuyển sang bộ từ vựng khác, Đổi ngày giờ, Xóa. Ba mục sau mở hộp thoại riêng:
 * - Chuyển: danh sách các bộ từ vựng (nhóm theo Tủ sách), có cảnh báo nếu cặp ngôn ngữ khác bộ hiện tại
 * - Đổi ngày giờ: chọn ngày rồi chọn giờ
 * - Xóa: luôn hỏi xác nhận (xóa từ cũng xóa các nghĩa của nó và không hoàn tác được)
 *
 * Chỉ vẽ giao diện: kết quả cuối cùng trả ra qua [onMoveTo], [onChangeDateTime], [onDelete] và chỉ được gọi
 * SAU khi người dùng xác nhận. Việc ghi database do ViewModel làm.
 *
 * @param wordTerm từ đang thao tác (hiện trong câu hỏi xác nhận xóa).
 * @param createdAtMillis ngày giờ hiện tại của từ, dùng làm giá trị mở sẵn cho hộp thoại đổi ngày giờ.
 * @param currentBookcaseId bộ từ vựng đang chứa từ, để loại khỏi danh sách đích chuyển.
 * @param moveTargets toàn bộ Tủ sách và bộ từ vựng có thể chuyển tới.
 */
@Composable
fun WordActionsMenuButton(
    wordTerm: String,
    createdAtMillis: Long,
    modifier: Modifier = Modifier,
    currentBookcaseId: Long = NO_BOOKCASE,
    moveTargets: List<BookshelfUi> = emptyList(),
    onEdit: () -> Unit = {},
    onMoveTo: (targetBookcaseId: Long) -> Unit = {},
    onChangeDateTime: (newMillis: Long) -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var menuOpen by remember { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf(ActionDialog.NONE) }

    Box(modifier = modifier) {
        IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.wa_more_options))
        }
        WordActionsMenu(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            onEdit = { menuOpen = false; onEdit() },
            onMove = { menuOpen = false; dialog = ActionDialog.MOVE },
            onChangeDateTime = { menuOpen = false; dialog = ActionDialog.DATE_TIME },
            onDelete = { menuOpen = false; dialog = ActionDialog.DELETE }
        )
    }

    when (dialog) {
        ActionDialog.MOVE -> MoveWordDialog(
            currentBookcaseId = currentBookcaseId,
            shelves = moveTargets,
            onMove = { targetId -> dialog = ActionDialog.NONE; onMoveTo(targetId) },
            onDismiss = { dialog = ActionDialog.NONE }
        )
        ActionDialog.DATE_TIME -> ChangeDateTimeDialog(
            initialMillis = createdAtMillis,
            onConfirm = { millis -> dialog = ActionDialog.NONE; onChangeDateTime(millis) },
            onDismiss = { dialog = ActionDialog.NONE }
        )
        ActionDialog.DELETE -> DeleteWordDialog(
            wordTerm = wordTerm,
            onConfirm = { dialog = ActionDialog.NONE; onDelete() },
            onDismiss = { dialog = ActionDialog.NONE }
        )
        ActionDialog.NONE -> Unit
    }
}

/** Danh sách thả xuống 4 mục. Mục Xóa tô màu đỏ và tách riêng bằng đường kẻ. */
@Composable
fun WordActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onMove: () -> Unit,
    onChangeDateTime: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.wa_edit)) },
            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
            onClick = onEdit
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.wa_move)) },
            leadingIcon = { Icon(Icons.Outlined.SwapHoriz, contentDescription = null) },
            onClick = onMove
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.wa_change_date_time)) },
            leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
            onClick = onChangeDateTime
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(R.string.wa_delete), color = colors.error) },
            leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null, tint = colors.error) },
            onClick = onDelete
        )
    }
}

/** Chọn bộ từ vựng đích. Bộ hiện tại không có trong danh sách; nút Move chỉ bấm được khi đã chọn. */
@Composable
private fun MoveWordDialog(
    currentBookcaseId: Long,
    shelves: List<BookshelfUi>,
    onMove: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    var selected by rememberSaveable { mutableStateOf<Long?>(null) }
    val current: BookcaseUi? = remember(shelves, currentBookcaseId) {
        shelves.flatMap { it.bookcases }.firstOrNull { it.id == currentBookcaseId }
    }
    val candidates = shelves.filter { shelf -> shelf.bookcases.any { it.id != currentBookcaseId } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wa_move_title)) },
        text = {
            if (candidates.isEmpty()) {
                Text(
                    text = stringResource(R.string.wa_move_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            } else {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    candidates.forEach { shelf ->
                        Text(
                            text = shelf.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        shelf.bookcases.filter { it.id != currentBookcaseId }.forEach { bookcase ->
                            val differs = current != null &&
                                (bookcase.sourceLang != current.sourceLang || bookcase.targetLang != current.targetLang)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = selected == bookcase.id,
                                        onClick = { selected = bookcase.id },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected == bookcase.id, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = bookcase.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = colors.onSurface
                                    )
                                    Text(
                                        text = "${bookcase.sourceLang} / ${bookcase.targetLang}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.onSurfaceVariant
                                    )
                                    if (differs) {
                                        Text(
                                            text = stringResource(R.string.wa_move_language_differs),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { selected?.let(onMove) }, enabled = selected != null) {
                Text(stringResource(R.string.wa_move_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.wa_cancel)) }
        }
    )
}

/**
 * Đổi ngày giờ theo 2 bước: chọn ngày (Next), rồi chọn giờ (OK).
 * Chuyển đổi múi giờ do [localMillisToPickerUtcDate] và [combineDateAndTime] lo (xem giải thích ở file đó).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeDateTimeDialog(
    initialMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val zone = remember { TimeZone.getDefault() }
    var step by rememberSaveable { mutableStateOf(0) }
    var pickedDate by rememberSaveable { mutableStateOf<Long?>(null) }

    if (step == 0) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = pickedDate ?: localMillisToPickerUtcDate(initialMillis, zone)
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = { pickedDate = dateState.selectedDateMillis; step = 1 },
                    enabled = dateState.selectedDateMillis != null
                ) { Text(stringResource(R.string.wa_next)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.wa_cancel)) }
            }
        ) {
            DatePicker(state = dateState)
        }
    } else {
        val initial = remember(initialMillis) { Calendar.getInstance(zone).apply { timeInMillis = initialMillis } }
        val timeState = rememberTimePickerState(
            initialHour = initial.get(Calendar.HOUR_OF_DAY),
            initialMinute = initial.get(Calendar.MINUTE),
            is24Hour = DateFormat.is24HourFormat(LocalContext.current)
        )
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                modifier = Modifier.width(IntrinsicSize.Min)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.wa_time_title),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    TimePicker(state = timeState)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { step = 0 }) { Text(stringResource(R.string.wa_back)) }
                        TextButton(
                            onClick = {
                                pickedDate?.let { date ->
                                    onConfirm(combineDateAndTime(date, timeState.hour, timeState.minute, zone))
                                }
                            }
                        ) { Text(stringResource(R.string.wa_ok)) }
                    }
                }
            }
        }
    }
}

/** Xác nhận xóa: nêu rõ từ nào và không hoàn tác được. */
@Composable
private fun DeleteWordDialog(wordTerm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.wa_delete_title)) },
        text = { Text(stringResource(R.string.wa_delete_message, wordTerm)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.wa_delete_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.wa_cancel)) }
        }
    )
}

// ---------------------------------------------------------------------------------------
// Preview. Menu và các hộp thoại chỉ mở khi bấm, nên hãy bật "Interactive Preview" (biểu tượng bàn tay)
// ở góc Preview rồi bấm nút ba chấm.
// ---------------------------------------------------------------------------------------

private val sampleTargets = listOf(
    BookshelfUi(
        id = 1, name = "English vlog-home",
        bookcases = listOf(
            BookcaseUi(1, "Household items", "English (US)", "Vietnamese", 80, 78),
            BookcaseUi(2, "Supermarket", "English (US)", "Vietnamese", 67, 61),
            BookcaseUi(3, "Hotel", "English (US)", "Vietnamese", 86, 86)
        )
    ),
    BookshelfUi(
        id = 2, name = "FULL ENGLISH",
        bookcases = listOf(BookcaseUi(4, "Korean basics", "English (US)", "Korean", 36, 0))
    )
)

@Preview(showBackground = true, name = "Nút ba chấm (bật Interactive Preview)", uiMode = Configuration.UI_MODE_NIGHT_YES, widthDp = 390, heightDp = 400)
@Composable
private fun WordActionsMenuButtonPreview() {
    VoCatCloneTheme {
        Surface {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.End) {
                WordActionsMenuButton(
                    wordTerm = "cook",
                    createdAtMillis = 1_786_000_000_000,
                    currentBookcaseId = 1,
                    moveTargets = sampleTargets
                )
            }
        }
    }
}
