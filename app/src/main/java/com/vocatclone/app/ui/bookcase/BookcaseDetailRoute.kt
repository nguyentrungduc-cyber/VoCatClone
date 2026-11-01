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
 * Menu trên từng từ (sửa, chuyển bộ, đổi ngày, xóa) và đánh dấu đã thuộc được nối ở các PR sau.
 */
@Composable
fun BookcaseDetailRoute(
    bookcaseId: Long,
    onBack: () -> Unit,
    onAddWord: (bookcaseId: Long) -> Unit,
    onOpenWord: (wordId: Long) -> Unit,
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
        onWordClick = onOpenWord
    )
}
