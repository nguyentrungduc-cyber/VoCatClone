package com.vocatclone.app.ui.word

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

data class WordDetailState(
    /** null khi đang tải hoặc từ đã bị xóa (xem [isDeleted]). */
    val word: WordDetailUi? = null,
    val bookcaseId: Long = NO_BOOKCASE,
    val bookcaseName: String = "",
    val moveTargets: List<BookshelfUi> = emptyList(),
    /** true sau khi từ bị xóa -> màn hình nên đóng. */
    val isDeleted: Boolean = false
)

/** State cho [WordDetailScreen]: chi tiết 1 từ + toggle đã thuộc, chuyển bộ, đổi ngày giờ, xóa. */
@OptIn(ExperimentalCoroutinesApi::class)
class WordDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val auth = AuthRepository()
    private val shelfRepo = BookshelfRepository(db)
    private val bookcaseRepo = BookcaseRepository(db)
    private val wordRepo = WordRepository(db)

    private val wordId = MutableStateFlow<Long?>(null)

    /** Gọi một lần khi mở màn. */
    fun start(id: Long) {
        wordId.value = id
    }

    private val shelves = auth.uidFlow
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList<BookshelfUi>()) else shelfRepo.observeShelves(uid) }

    val state: StateFlow<WordDetailState> = wordId.filterNotNull()
        .flatMapLatest { id ->
            wordRepo.observeDetail(id).flatMapLatest { word ->
                if (word == null) {
                    flowOf(WordDetailState(isDeleted = true))
                } else {
                    // Vị trí (bộ từ vựng) của từ có thể đổi khi chuyển bộ, nên tra lại mỗi lần từ thay đổi.
                    val bookcaseId = wordRepo.bookcaseIdOf(id) ?: NO_BOOKCASE
                    combine(bookcaseRepo.observe(bookcaseId), shelves) { bookcase, shelves ->
                        WordDetailState(
                            word = word,
                            bookcaseId = bookcaseId,
                            bookcaseName = bookcase?.name.orEmpty(),
                            moveTargets = shelves
                        )
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WordDetailState())

    fun toggleMastered() = withWord { wordRepo.toggleMastered(it) }

    fun move(targetBookcaseId: Long) = withWord { wordRepo.moveToBookcase(it, targetBookcaseId) }

    fun changeDateTime(newMillis: Long) = withWord { wordRepo.changeDateTime(it, newMillis) }

    fun delete() = withWord { wordRepo.delete(it) }

    private fun withWord(block: suspend (Long) -> Unit) {
        val id = wordId.value ?: return
        viewModelScope.launch { runCatching { block(id) } }
    }
}
