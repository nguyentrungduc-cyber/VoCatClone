package com.vocatclone.app.ui.bookcase

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocatclone.app.R

/**
 * Nối [BookcaseFormViewModel] vào [BookcaseFormScreen] (tạo hoặc sửa bộ từ vựng).
 *
 * @param bookcaseId null: tạo mới. Khác null: sửa bộ từ vựng đó.
 * @param defaultShelfId chọn sẵn tủ sách khi tạo mới (null thì dùng tủ đầu tiên / tủ mặc định).
 * @param onDone gọi khi đã lưu xong hoặc người dùng bấm Back.
 */
@Composable
fun BookcaseFormRoute(
    bookcaseId: Long?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    defaultShelfId: Long? = null,
    viewModel: BookcaseFormViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(bookcaseId, defaultShelfId) { viewModel.start(bookcaseId, defaultShelfId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onDone() }

    // BookcaseFormScreen chỉ đọc `initial` lúc mới hiện, nên phải chờ đọc xong dữ liệu cũ mới vẽ form.
    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        BookcaseFormScreen(
            shelves = state.shelves,
            modifier = modifier,
            initial = state.initial,
            isEditing = state.isEditing,
            onBack = onDone,
            onSubmit = viewModel::submit
        )
    }

    state.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text(stringResource(R.string.mg_error_title)) },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = viewModel::clearError) { Text(stringResource(R.string.mg_ok)) } }
        )
    }
}
