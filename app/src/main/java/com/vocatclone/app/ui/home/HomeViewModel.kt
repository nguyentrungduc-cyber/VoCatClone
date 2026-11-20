package com.vocatclone.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.local.entity.WordWithMeanings
import com.vocatclone.app.data.repository.AuthRepository
import com.vocatclone.app.data.seed.SampleData
import com.vocatclone.app.data.repository.BookcaseRepository
import com.vocatclone.app.data.repository.BookshelfRepository
import com.vocatclone.app.data.repository.WordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State cho [HomeScreen]: danh sách tủ sách của người dùng đang đăng nhập.
 * Chưa đăng nhập thì danh sách rỗng. Đăng nhập/đăng xuất đổi tài khoản -> danh sách tự đổi theo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val auth = AuthRepository()
    private val shelfRepo = BookshelfRepository(db)
    private val bookcaseRepo = BookcaseRepository(db)
    private val wordRepo = WordRepository(db)

    val shelves: StateFlow<List<BookshelfUi>> = auth.uidFlow
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList<BookshelfUi>()) else shelfRepo.observeShelves(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Thêm dữ liệu mẫu cho tài khoản đang đăng nhập (chỉ khi chưa có tủ sách nào). Dùng khi phát triển. */
    fun seedSampleData() = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        runCatching { SampleData.insertFor(db, uid) }
    }

    /** Chữ đang gõ trong ô tìm kiếm của [HomeScreen]. */
    private val searchQuery = MutableStateFlow("")

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    /**
     * Các từ khớp ô tìm kiếm, kèm tên bộ từ vựng chứa nó.
     *
     * debounce để mỗi phím gõ không bắn một truy vấn; tên bộ lấy từ [shelves] đang có nên
     * không cần thêm truy vấn thứ hai.
     */
    val searchResults: StateFlow<List<WordSearchUi>> = auth.uidFlow
        .flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList<WordWithMeanings>())
            } else {
                searchQuery
                    .debounce(SEARCH_DEBOUNCE_MS)
                    .distinctUntilChanged()
                    .flatMapLatest { query -> wordRepo.searchWords(uid, query) }
            }
        }
        .combine(shelves) { words, shelves ->
            val nameById = shelves.flatMap { it.bookcases }.associate { it.id to it.name }
            words.map { w ->
                WordSearchUi(
                    id = w.word.id,
                    term = w.word.term,
                    meanings = w.sortedMeanings.map { it.meaning },
                    bookcaseId = w.word.bookcaseId,
                    bookcaseName = nameById[w.word.bookcaseId].orEmpty()
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createShelf(name: String) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        runCatching { shelfRepo.createShelf(uid, name) }
    }

    fun renameShelf(shelfId: Long, name: String) = viewModelScope.launch {
        runCatching { shelfRepo.renameShelf(shelfId, name) }
    }

    fun deleteShelf(shelfId: Long) = viewModelScope.launch { runCatching { shelfRepo.deleteShelf(shelfId) } }

    fun deleteBookcase(bookcaseId: Long) = viewModelScope.launch { runCatching { bookcaseRepo.delete(bookcaseId) } }

    private companion object {
        /** Chờ người dùng ngừng gõ rồi mới tìm, tránh bắn truy vấn theo từng phím. */
        const val SEARCH_DEBOUNCE_MS = 250L
    }
}
