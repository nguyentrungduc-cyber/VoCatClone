package com.vocatclone.app.ui.bookcase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.repository.AuthRepository
import com.vocatclone.app.data.repository.BookcaseRepository
import com.vocatclone.app.data.repository.BookshelfRepository
import com.vocatclone.app.data.repository.toFormValues
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BookcaseFormState(
    val shelves: List<ShelfOption> = emptyList(),
    /** Giá trị điền sẵn: mặc định khi tạo mới, dữ liệu cũ khi sửa. */
    val initial: BookcaseFormValues = BookcaseFormValues(),
    val isEditing: Boolean = false,
    /** Đang đọc dữ liệu cũ (chỉ khi sửa). Nên chờ xong rồi mới vẽ form để [initial] đúng. */
    val isLoading: Boolean = false,
    /** true sau khi lưu xong -> màn hình đóng lại. */
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)

/** State cho [BookcaseFormScreen] (tạo hoặc sửa bộ từ vựng). Gọi [start] một lần khi mở màn. */
@OptIn(ExperimentalCoroutinesApi::class)
class BookcaseFormViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val auth = AuthRepository()
    private val shelfRepo = BookshelfRepository(db)
    private val bookcaseRepo = BookcaseRepository(db)

    private val _state = MutableStateFlow(BookcaseFormState())
    val state: StateFlow<BookcaseFormState> = _state.asStateFlow()

    private var editingId: Long? = null
    private var started = false

    init {
        // Danh sách tủ sách luôn theo tài khoản hiện tại.
        viewModelScope.launch {
            auth.uidFlow
                .flatMapLatest { uid -> if (uid == null) flowOf(emptyList<ShelfOption>()) else shelfRepo.observeShelfOptions(uid) }
                .collect { options -> _state.update { it.copy(shelves = options) } }
        }
    }

    /** [bookcaseId] = null: tạo mới. Khác null: sửa bộ từ vựng đó. [defaultShelfId] chọn sẵn tủ khi tạo mới. */
    fun start(bookcaseId: Long?, defaultShelfId: Long? = null) {
        if (started) return
        started = true
        editingId = bookcaseId
        if (bookcaseId == null) {
            _state.update { it.copy(initial = BookcaseFormValues(shelfId = defaultShelfId), isEditing = false) }
            return
        }
        _state.update { it.copy(isEditing = true, isLoading = true) }
        viewModelScope.launch {
            val old = bookcaseRepo.get(bookcaseId)
            _state.update {
                if (old == null) it.copy(isLoading = false, errorMessage = "Không tìm thấy bộ từ vựng")
                else it.copy(isLoading = false, initial = old.toFormValues())
            }
        }
    }

    fun submit(values: BookcaseFormValues) {
        if (values.name.isBlank()) return
        viewModelScope.launch {
            runCatching {
                // Chưa có tủ sách nào / chưa chọn tủ: dùng tủ mặc định (tạo nếu cần).
                val withShelf = if (values.shelfId != null) values else {
                    val uid = auth.currentUser?.uid ?: error("Bạn cần đăng nhập để tạo bộ từ vựng")
                    values.copy(shelfId = shelfRepo.ensureDefaultShelf(uid))
                }
                val id = editingId
                if (id == null) bookcaseRepo.create(withShelf) else bookcaseRepo.update(id, withShelf)
            }.onSuccess {
                _state.update { it.copy(isSaved = true, errorMessage = null) }
            }.onFailure { e ->
                _state.update { it.copy(errorMessage = e.message ?: "Không lưu được") }
            }
        }
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}
