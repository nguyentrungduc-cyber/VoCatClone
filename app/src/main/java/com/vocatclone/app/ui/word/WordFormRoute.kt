package com.vocatclone.app.ui.word

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocatclone.app.R

/**
 * Nối [WordFormViewModel] vào [WordFormScreen]: thêm từ mới vào [bookcaseId] hoặc sửa từ [wordId].
 * Lưu từ cùng danh sách nghĩa trong một transaction (xem WordDao.insertWithMeanings / updateWithMeanings).
 *
 * - Nút Add/Save: lưu rồi đóng màn.
 * - Nút "thêm rồi nhập tiếp": lưu, xóa form, ở lại màn để nhập từ kế tiếp.
 * Gợi ý nghĩa từ API dịch, chọn ảnh (Camera/Gallery) là các task riêng nên chưa nối.
 *
 * @param wordId null: thêm mới. Khác null: sửa từ đó.
 */
@Composable
fun WordFormRoute(
    bookcaseId: Long,
    wordId: Long?,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WordFormViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val imagePicker = rememberWordImagePicker(onPicked = viewModel::onImagePicked)
    var showImageSourceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bookcaseId, wordId) { viewModel.start(bookcaseId, wordId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onDone() }

    // Mỗi lần "thêm rồi nhập tiếp" xong thì báo nhẹ để người dùng biết từ đã được lưu (form đã xóa trắng).
    val addedMessage = stringResource(R.string.wf_added)
    LaunchedEffect(state.addedCount) {
        if (state.addedCount > 0) Toast.makeText(context, addedMessage, Toast.LENGTH_SHORT).show()
    }

    if (state.isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        WordFormScreen(
            bookcaseName = state.bookcaseName,
            values = state.values,
            onValuesChange = viewModel::onValuesChange,
            visibleFields = state.visibleFields,
            onVisibleFieldsChange = viewModel::onVisibleFieldsChange,
            modifier = modifier,
            wordLanguage = state.wordLanguage,
            meaningLanguage = state.meaningLanguage,
            isEditing = state.isEditing,
            onBack = onDone,
            onSubmit = viewModel::submit,
            onPickImage = { showImageSourceDialog = true }
        )
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text(stringResource(R.string.wf_image_source_title)) },
            text = {
                Column {
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        imagePicker.takePhoto()
                    }) { Text(stringResource(R.string.wf_image_camera)) }
                    TextButton(onClick = {
                        showImageSourceDialog = false
                        imagePicker.pickFromGallery()
                    }) { Text(stringResource(R.string.wf_image_gallery)) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text(stringResource(R.string.mg_cancel))
                }
            }
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
