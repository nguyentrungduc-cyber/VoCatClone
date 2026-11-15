package com.vocatclone.app.ui.bookcase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.repository.AuthRepository
import com.vocatclone.app.data.repository.BookcaseRepository
import com.vocatclone.app.data.repository.BookshelfRepository
import com.vocatclone.app.data.repository.WordRepository
import com.vocatclone.app.ui.home.BookshelfUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookcaseDetailState(
    val bookcaseId: Long = -1L,
    val bookcaseName: String = "",
    val words: List<WordItemUi> = emptyList(),
    /** Các tủ sách + bộ từ vựng để chọn khi "Chuyển sang bộ khác". */
    val moveTargets: List<BookshelfUi> = emptyList(),
    /** false khi bộ từ vựng không còn tồn tại (đã bị xóa) -> màn hình nên đóng. */
    val exists: Boolean = true
)

/** State cho [BookcaseDetailScreen]: danh sách từ của 1 bộ + các thao tác trên từng từ. */
@OptIn(ExperimentalCoroutinesApi::class)
class BookcaseDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val auth = AuthRepository()
    private val shelfRepo = BookshelfRepository(db)
    private val bookcaseRepo = BookcaseRepository(db)
    private val wordRepo = WordRepository(db)

    private val bookcaseId = MutableStateFlow<Long?>(null)

    /** Gọi một lần khi mở màn. */
    fun start(id: Long) {
        bookcaseId.value = id
    }

    private val shelves = auth.uidFlow
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList<BookshelfUi>()) else shelfRepo.observeShelves(uid) }

    val state: StateFlow<BookcaseDetailState> = bookcaseId.filterNotNull()
        .flatMapLatest { id ->
            combine(bookcaseRepo.observe(id), wordRepo.observeWords(id), shelves) { bookcase, words, shelves ->
                BookcaseDetailState(
                    bookcaseId = id,
                    bookcaseName = bookcase?.name.orEmpty(),
                    words = words,
                    moveTargets = shelves,
                    exists = bookcase != null
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BookcaseDetailState())

    fun toggleMastered(wordId: Long) = viewModelScope.launch { wordRepo.toggleMastered(wordId) }

    fun moveWord(wordId: Long, targetBookcaseId: Long) =
        viewModelScope.launch { wordRepo.moveToBookcase(wordId, targetBookcaseId) }

    fun changeDateTime(wordId: Long, newMillis: Long) =
        viewModelScope.launch { wordRepo.changeDateTime(wordId, newMillis) }

    fun deleteWord(wordId: Long) = viewModelScope.launch { wordRepo.delete(wordId) }
}
