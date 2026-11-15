package com.vocatclone.app.ui.bookcase

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Nối [BookcaseDetailViewModel] vào [BookcaseDetailScreen]: danh sách từ của một bộ từ vựng (LazyColumn),
 * tự cập nhật khi thêm/sửa/xóa từ.
 *
 * @param onAddWord thêm từ vào bộ này (truyền id bộ).
 * @param onOpenWord mở chi tiết một từ.
 * Menu trên từng từ: Sửa, Chuyển sang bộ khác, Đổi ngày giờ và Xóa đã nối.
 * Đánh dấu đã thuộc chưa nối (thuộc task riêng).
 * @param onEditWord mở form sửa một từ.
 */
@Composable
fun BookcaseDetailRoute(
    bookcaseId: Long,
    onBack: () -> Unit,
    onAddWord: (bookcaseId: Long) -> Unit,
    onOpenWord: (wordId: Long) -> Unit,
    onEditWord: (wordId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookcaseDetailViewModel = viewModel()
) {
    LaunchedEffect(bookcaseId) { viewModel.start(bookcaseId) }
    val state by viewModel.state.collectAsState()

    // Bộ từ vựng đã bị xóa (ở màn khác) thì không còn gì để xem.
    LaunchedEffect(state.exists) { if (!state.exists) onBack() }

    BookcaseDetailScreen(
        bookcaseName = state.bookcaseName,
        words = state.words,
        modifier = modifier,
        onBack = onBack,
        onAddWord = { onAddWord(bookcaseId) },
        onWordClick = onOpenWord,
        onEditWord = onEditWord,
        currentBookcaseId = bookcaseId,
        moveTargets = state.moveTargets,
        onMoveWord = viewModel::moveWord,
        onChangeDateTime = viewModel::changeDateTime,
        onDeleteWord = viewModel::deleteWord
    )
}
