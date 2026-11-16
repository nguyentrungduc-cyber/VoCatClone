package com.vocatclone.app.ui.home

import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vocatclone.app.R
import com.vocatclone.app.ui.auth.EmailVerificationBanner

/**
 * Nối [HomeViewModel] vào [HomeScreen]: danh sách tủ sách và bộ từ vựng của tài khoản đang đăng nhập,
 * tự cập nhật khi database đổi. [HomeScreen] vẫn chỉ vẽ giao diện (xem được bằng Preview).
 *
 * @param modifier chứa innerPadding của Scaffold ngoài (để không bị thanh điều hướng dưới che).
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onAddBookcase: () -> Unit = {},
    onOpenBookcase: (bookcaseId: Long) -> Unit = {},
    onEditBookcase: (bookcaseId: Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val shelves by viewModel.shelves.collectAsState()

    // Nút seed chỉ hiện ở bản debug và khi chưa có dữ liệu.
    val context = LocalContext.current
    val isDebuggable = remember { (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0 }

    // Hộp thoại đang mở (mỗi lúc chỉ một). Lưu id, không lưu cả đối tượng, để dữ liệu luôn mới nhất.
    var dialog by remember { mutableStateOf<HomeDialog>(HomeDialog.None) }

    Column(modifier = modifier) {
        // Nhắc xác thực email, chỉ hiện khi tài khoản chưa xác thực. Không chặn dùng app.
        EmailVerificationBanner()

        Box(modifier = Modifier.weight(1f)) {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                shelves = shelves,
                onAddClick = onAddBookcase,
                onBookcaseClick = onOpenBookcase,
                onBookcaseMenuClick = { dialog = HomeDialog.BookcaseActions(it) },
                onShelfMenuClick = { dialog = HomeDialog.ShelfActions(it) },
                onManageShelvesClick = { dialog = HomeDialog.NewShelf }
            )
            if (isDebuggable && shelves.isEmpty()) {
                OutlinedButton(
                    onClick = viewModel::seedSampleData,
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 24.dp)
                ) {
                    Text("Thêm dữ liệu mẫu (debug)")
                }
            }
        }
    }

    HomeDialogHost(
        dialog = dialog,
        shelves = shelves,
        onChange = { dialog = it },
        onEditBookcase = onEditBookcase,
        viewModel = viewModel
    )
}

/** Các hộp thoại của Home. Chỉ lưu id; tên lấy lại từ danh sách mới nhất khi vẽ. */
sealed interface HomeDialog {
    data object None : HomeDialog
    data object NewShelf : HomeDialog
    data class ShelfActions(val shelfId: Long) : HomeDialog
    data class RenameShelf(val shelfId: Long) : HomeDialog
    data class DeleteShelf(val shelfId: Long) : HomeDialog
    data class BookcaseActions(val bookcaseId: Long) : HomeDialog
    data class DeleteBookcase(val bookcaseId: Long) : HomeDialog
}

@Composable
private fun HomeDialogHost(
    dialog: HomeDialog,
    shelves: List<BookshelfUi>,
    onChange: (HomeDialog) -> Unit,
    onEditBookcase: (Long) -> Unit,
    viewModel: HomeViewModel
) {
    val close = { onChange(HomeDialog.None) }
    val shelfOf = { id: Long -> shelves.firstOrNull { it.id == id } }
    val bookcaseOf = { id: Long -> shelves.flatMap { it.bookcases }.firstOrNull { it.id == id } }

    when (dialog) {
        HomeDialog.None -> Unit

        HomeDialog.NewShelf -> NameInputDialog(
            title = stringResource(R.string.mg_new_shelf_title),
            hint = stringResource(R.string.mg_shelf_name_hint),
            confirmLabel = stringResource(R.string.mg_create),
            onConfirm = { viewModel.createShelf(it); close() },
            onDismiss = close
        )

        is HomeDialog.ShelfActions -> shelfOf(dialog.shelfId)?.let { shelf ->
            ItemActionsDialog(
                title = shelf.name,
                primaryLabel = stringResource(R.string.mg_rename),
                onPrimary = { onChange(HomeDialog.RenameShelf(shelf.id)) },
                onDelete = { onChange(HomeDialog.DeleteShelf(shelf.id)) },
                onDismiss = close
            )
        } ?: close()

        is HomeDialog.RenameShelf -> shelfOf(dialog.shelfId)?.let { shelf ->
            NameInputDialog(
                title = stringResource(R.string.mg_rename_shelf_title),
                hint = stringResource(R.string.mg_shelf_name_hint),
                confirmLabel = stringResource(R.string.mg_save),
                initial = shelf.name,
                onConfirm = { viewModel.renameShelf(shelf.id, it); close() },
                onDismiss = close
            )
        } ?: close()

        is HomeDialog.DeleteShelf -> shelfOf(dialog.shelfId)?.let { shelf ->
            ConfirmDeleteDialog(
                title = stringResource(R.string.mg_delete_shelf_title),
                message = stringResource(R.string.mg_delete_shelf_message, shelf.name),
                onConfirm = { viewModel.deleteShelf(shelf.id); close() },
                onDismiss = close
            )
        } ?: close()

        is HomeDialog.BookcaseActions -> bookcaseOf(dialog.bookcaseId)?.let { bookcase ->
            ItemActionsDialog(
                title = bookcase.name,
                primaryLabel = stringResource(R.string.mg_edit),
                onPrimary = { close(); onEditBookcase(bookcase.id) },
                onDelete = { onChange(HomeDialog.DeleteBookcase(bookcase.id)) },
                onDismiss = close
            )
        } ?: close()

        is HomeDialog.DeleteBookcase -> bookcaseOf(dialog.bookcaseId)?.let { bookcase ->
            ConfirmDeleteDialog(
                title = stringResource(R.string.mg_delete_set_title),
                message = stringResource(R.string.mg_delete_set_message, bookcase.name),
                onConfirm = { viewModel.deleteBookcase(bookcase.id); close() },
                onDismiss = close
            )
        } ?: close()
    }
}
