package com.vocatclone.app.ui.word

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vocatclone.app.R
import com.vocatclone.app.data.image.WordImageStore
import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.remote.LookupError
import com.vocatclone.app.data.remote.LookupException
import com.vocatclone.app.data.repository.BookcaseRepository
import com.vocatclone.app.data.repository.TranslationRepository
import com.vocatclone.app.data.repository.WordRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WordFormState(
    val bookcaseName: String = "",
    val wordLanguage: String = "English (US)",
    val meaningLanguage: String = "Vietnamese",
    val values: WordFormValues = WordFormValues(),
    val visibleFields: Set<WordField> = DEFAULT_VISIBLE_FIELDS,
    val isEditing: Boolean = false,
    /** Nghĩa do API dịch gợi ý; bấm vào một gợi ý là thêm vào danh sách nghĩa. */
    val suggestions: List<String> = emptyList(),
    val isLookingUp: Boolean = false,
    /** Câu nhắc khi tra nghĩa không xong. Người dùng vẫn gõ nghĩa tay được như thường. */
    val lookupError: String? = null,
    val isLoading: Boolean = true,
    /** true sau khi lưu xong và KHÔNG chọn "thêm tiếp" -> màn hình đóng lại. */
    val isSaved: Boolean = false,
    /** Tăng mỗi lần lưu xong ở chế độ "thêm tiếp" (UI dùng để hiện thông báo / đưa con trỏ về ô Từ). */
    val addedCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * State cho [WordFormScreen] (thêm hoặc sửa từ). Gọi [start] một lần khi mở màn.
 * Gợi ý nghĩa từ API dịch (suggestions / lookup) chưa có: là việc riêng trong Timeline.
 */
class WordFormViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val bookcaseRepo = BookcaseRepository(db)
    private val wordRepo = WordRepository(db)
    private val translationRepo = TranslationRepository()

    private val _state = MutableStateFlow(WordFormState())
    val state: StateFlow<WordFormState> = _state.asStateFlow()

    /**
     * Những file ảnh do CHÍNH lần mở form này tạo ra. Chỉ xóa file trong tập này khi bị thay hoặc bỏ,
     * để không bao giờ xóa nhầm ảnh cũ của một từ mà người dùng có thể bấm Back không lưu.
     */
    private val imagesAddedHere = mutableSetOf<String>()

    /** Lần tra nghĩa đang chạy, để bấm tra lần nữa thì bỏ kết quả cũ. */
    private var lookupJob: Job? = null

    private var bookcaseId = -1L
    private var editingWordId: Long? = null
    private var started = false

    /** [wordId] = null: thêm từ mới vào [bookcaseId]. Khác null: sửa từ đó (bộ từ vựng lấy theo từ). */
    fun start(bookcaseId: Long, wordId: Long? = null) {
        if (started) return
        started = true
        this.bookcaseId = bookcaseId
        editingWordId = wordId
        viewModelScope.launch {
            val old = wordId?.let { wordRepo.getFormValues(it) }
            val bookcase = bookcaseRepo.get(bookcaseId)
            _state.update {
                it.copy(
                    bookcaseName = bookcase?.name.orEmpty(),
                    wordLanguage = bookcase?.sourceLang ?: it.wordLanguage,
                    meaningLanguage = bookcase?.targetLang ?: it.meaningLanguage,
                    values = old ?: WordFormValues(),
                    isEditing = wordId != null,
                    isLoading = false,
                    errorMessage = if (wordId != null && old == null) "Không tìm thấy từ vựng" else null
                )
            }
        }
    }

    fun onValuesChange(values: WordFormValues) {
        val old = _state.value.values.imageUri
        // Người dùng bỏ ảnh (nút x): dọn file nếu ảnh đó vừa được thêm trong lần mở form này.
        if (old != null && old != values.imageUri && imagesAddedHere.remove(old)) {
            WordImageStore.delete(old)
        }
        _state.update { it.copy(values = values) }
    }

    /** Người dùng vừa chụp hoặc chọn xong một ảnh; [path] đã nằm trong bộ nhớ riêng của app. */
    fun onImagePicked(path: String) {
        val old = _state.value.values.imageUri
        if (old != null && imagesAddedHere.remove(old)) WordImageStore.delete(old)
        imagesAddedHere.add(path)
        _state.update { it.copy(values = it.values.copy(imageUri = path)) }
    }

    fun onVisibleFieldsChange(fields: Set<WordField>) = _state.update { it.copy(visibleFields = fields) }

    /** [addAnother] = true: lưu rồi xóa form để nhập từ tiếp theo (chỉ có ý nghĩa khi thêm mới). */
    fun submit(addAnother: Boolean) {
        val values = _state.value.values
        if (!values.canSubmit) return
        viewModelScope.launch {
            runCatching {
                val id = editingWordId
                if (id == null) wordRepo.add(bookcaseId, values) else wordRepo.update(id, values)
            }.onSuccess {
                _state.update {
                    if (addAnother && editingWordId == null) {
                        it.copy(values = WordFormValues(), addedCount = it.addedCount + 1, errorMessage = null)
                    } else {
                        it.copy(isSaved = true, errorMessage = null)
                    }
                }
            }.onFailure { e ->
                _state.update { it.copy(errorMessage = e.message ?: "Không lưu được") }
            }
        }
    }

    /**
     * Tra nghĩa của [term] bằng API dịch. Chạy nền nên không chặn giao diện; lỗi mạng chỉ hiện
     * một câu nhắc, ô nhập nghĩa vẫn dùng bình thường (fallback nhập tay).
     */
    fun lookup(term: String) {
        val clean = term.trim()
        if (clean.isEmpty()) return
        lookupJob?.cancel()
        _state.update { it.copy(isLookingUp = true, lookupError = null, suggestions = emptyList()) }
        lookupJob = viewModelScope.launch {
            val current = _state.value
            val result = translationRepo.suggestMeanings(clean, current.wordLanguage, current.meaningLanguage)
            _state.update { s ->
                result.fold(
                    onSuccess = { list -> s.copy(isLookingUp = false, suggestions = list) },
                    onFailure = { e -> s.copy(isLookingUp = false, lookupError = messageFor(e)) }
                )
            }
        }
    }

    /** Đổi lỗi tra nghĩa thành câu tiếng Việt cho người dùng. */
    private fun messageFor(e: Throwable): String {
        val reason = (e as? LookupException)?.reason ?: LookupError.SERVER
        return getApplication<Application>().getString(
            when (reason) {
                LookupError.NO_NETWORK -> R.string.wf_lookup_no_network
                LookupError.TIMEOUT -> R.string.wf_lookup_timeout
                LookupError.RATE_LIMITED -> R.string.wf_lookup_rate_limited
                LookupError.NO_RESULT -> R.string.wf_lookup_no_result
                LookupError.UNSUPPORTED_LANGUAGE -> R.string.wf_lookup_unsupported
                LookupError.SERVER -> R.string.wf_lookup_server
            }
        )
    }

    fun clearError() = _state.update { it.copy(errorMessage = null) }
}
