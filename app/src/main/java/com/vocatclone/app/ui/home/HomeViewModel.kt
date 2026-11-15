package com.vocatclone.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.repository.AuthRepository
import com.vocatclone.app.data.repository.BookcaseRepository
import com.vocatclone.app.data.repository.BookshelfRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val shelves: StateFlow<List<BookshelfUi>> = auth.uidFlow
        .flatMapLatest { uid -> if (uid == null) flowOf(emptyList<BookshelfUi>()) else shelfRepo.observeShelves(uid) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createShelf(name: String) = viewModelScope.launch {
        val uid = auth.currentUser?.uid ?: return@launch
        runCatching { shelfRepo.createShelf(uid, name) }
    }

    fun renameShelf(shelfId: Long, name: String) = viewModelScope.launch {
        runCatching { shelfRepo.renameShelf(shelfId, name) }
    }

    fun deleteShelf(shelfId: Long) = viewModelScope.launch { shelfRepo.deleteShelf(shelfId) }

    fun deleteBookcase(bookcaseId: Long) = viewModelScope.launch { bookcaseRepo.delete(bookcaseId) }
}
