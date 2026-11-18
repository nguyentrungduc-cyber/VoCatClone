package com.vocatclone.app.data.repository

import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.local.entity.WordEntity
import com.vocatclone.app.ui.bookcase.WordItemUi
import com.vocatclone.app.ui.word.WordDetailUi
import com.vocatclone.app.ui.word.WordFormValues
import com.vocatclone.app.ui.word.canSubmit
import com.vocatclone.app.ui.word.cleanMeanings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Từ vựng và các nghĩa của nó. Mọi thao tác ghi đi qua [WordDao] (có transaction cho từ + nghĩa). */
class WordRepository(
    db: AppDatabase,
    private val guard: OwnershipGuard = OwnershipGuard(db)
) {

    private val dao = db.wordDao()

    // ---------- Đọc ----------

    fun observeWords(bookcaseId: Long): Flow<List<WordItemUi>> =
        dao.getByBookcase(bookcaseId).map { list -> list.map { it.toItemUi() } }

    /**
     * Tìm từ theo [query], khớp cả ở từ lẫn ở nghĩa, chỉ trong dữ liệu của [uid].
     * [query] rỗng trả về danh sách rỗng (ô tìm kiếm chưa gõ gì thì không hiện kết quả).
     */
    fun searchWords(uid: String, query: String, limit: Int = SEARCH_LIMIT): Flow<List<WordWithMeanings>> {
        val clean = query.trim()
        if (clean.isEmpty()) return flowOf(emptyList())
        return dao.searchByUser(uid, "%${clean.escapeLike()}%", limit)
    }

    fun observeDetail(wordId: Long): Flow<WordDetailUi?> =
        dao.observeWithMeanings(wordId).map { it?.toDetailUi() }

    /** Id bộ từ vựng đang chứa từ này (để màn chi tiết biết tên bộ và đích chuyển). */
    suspend fun bookcaseIdOf(wordId: Long): Long? =
        if (guard.ownsWord(wordId)) dao.getWithMeanings(wordId)?.word?.bookcaseId else null

    /** Giá trị điền sẵn cho form Sửa từ. null nếu từ không còn tồn tại. */
    suspend fun getFormValues(wordId: Long): WordFormValues? =
        if (guard.ownsWord(wordId)) dao.getWithMeanings(wordId)?.toFormValues() else null

    // ---------- Ghi ----------

    /** Thêm từ mới vào bộ [bookcaseId] kèm các nghĩa (đã lọc ô trống / trùng). Trả về id từ. */
    suspend fun add(bookcaseId: Long, values: WordFormValues): Long {
        require(values.canSubmit) { "Cần nhập từ và ít nhất một nghĩa" }
        guard.requireBookcase(bookcaseId)
        val word = WordEntity(
            bookcaseId = bookcaseId,
            term = values.term.trim(),
            pronunciation = values.pronunciation.nullIfBlank(),
            partOfSpeech = values.partOfSpeech.nullIfBlank(),
            exampleEn = values.exampleEn.nullIfBlank(),
            exampleVi = values.exampleVi.nullIfBlank(),
            synonym = values.synonym.nullIfBlank(),
            description = values.description.nullIfBlank(),
            imageUri = values.imageUri.nullIfBlank()
        )
        return dao.insertWithMeanings(word, values.cleanMeanings())
    }

    /** Sửa từ. Giữ nguyên bộ từ vựng, trạng thái đã thuộc, số lần ôn và ngày tạo. */
    suspend fun update(wordId: Long, values: WordFormValues) {
        require(values.canSubmit) { "Cần nhập từ và ít nhất một nghĩa" }
        guard.requireWord(wordId)
        val old = dao.getWithMeanings(wordId)?.word ?: return
        val word = old.copy(
            term = values.term.trim(),
            pronunciation = values.pronunciation.nullIfBlank(),
            partOfSpeech = values.partOfSpeech.nullIfBlank(),
            exampleEn = values.exampleEn.nullIfBlank(),
            exampleVi = values.exampleVi.nullIfBlank(),
            synonym = values.synonym.nullIfBlank(),
            description = values.description.nullIfBlank(),
            imageUri = values.imageUri.nullIfBlank()
        )
        dao.updateWithMeanings(word, values.cleanMeanings())
    }

    suspend fun toggleMastered(wordId: Long) {
        guard.requireWord(wordId)
        dao.toggleMastered(wordId)
    }

    /** Chuyển từ sang bộ khác. Cả từ và bộ đích đều phải thuộc tài khoản đang đăng nhập. */
    suspend fun moveToBookcase(wordId: Long, targetBookcaseId: Long) {
        guard.requireWord(wordId)
        guard.requireBookcase(targetBookcaseId)
        dao.moveToBookcase(wordId, targetBookcaseId)
    }

    suspend fun changeDateTime(wordId: Long, newMillis: Long) {
        guard.requireWord(wordId)
        dao.setCreatedAt(wordId, newMillis)
    }

    suspend fun delete(wordId: Long) {
        guard.requireWord(wordId)
        dao.deleteById(wordId)
    }

    private companion object {
        /** Số kết quả tìm kiếm tối đa; đủ dùng cho ô tìm kiếm mà không phải vẽ cả nghìn dòng. */
        const val SEARCH_LIMIT = 50
    }
}

/** Thoát ký tự đại diện của LIKE, để gõ % hay _ thì tìm đúng ký tự đó (khớp với ESCAPE trong câu @Query). */
private fun String.escapeLike(): String =
    replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
