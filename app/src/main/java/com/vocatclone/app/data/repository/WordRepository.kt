package com.vocatclone.app.data.repository

import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.local.entity.WordEntity
import com.vocatclone.app.ui.bookcase.WordItemUi
import com.vocatclone.app.ui.word.WordDetailUi
import com.vocatclone.app.ui.word.WordFormValues
import com.vocatclone.app.ui.word.canSubmit
import com.vocatclone.app.ui.word.cleanMeanings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Từ vựng và các nghĩa của nó. Mọi thao tác ghi đi qua [WordDao] (có transaction cho từ + nghĩa). */
class WordRepository(db: AppDatabase) {

    private val dao = db.wordDao()

    // ---------- Đọc ----------

    fun observeWords(bookcaseId: Long): Flow<List<WordItemUi>> =
        dao.getByBookcase(bookcaseId).map { list -> list.map { it.toItemUi() } }

    fun observeDetail(wordId: Long): Flow<WordDetailUi?> =
        dao.observeWithMeanings(wordId).map { it?.toDetailUi() }

    /** Id bộ từ vựng đang chứa từ này (để màn chi tiết biết tên bộ và đích chuyển). */
    suspend fun bookcaseIdOf(wordId: Long): Long? = dao.getWithMeanings(wordId)?.word?.bookcaseId

    /** Giá trị điền sẵn cho form Sửa từ. null nếu từ không còn tồn tại. */
    suspend fun getFormValues(wordId: Long): WordFormValues? = dao.getWithMeanings(wordId)?.toFormValues()

    // ---------- Ghi ----------

    /** Thêm từ mới vào bộ [bookcaseId] kèm các nghĩa (đã lọc ô trống / trùng). Trả về id từ. */
    suspend fun add(bookcaseId: Long, values: WordFormValues): Long {
        require(values.canSubmit) { "Cần nhập từ và ít nhất một nghĩa" }
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

    suspend fun toggleMastered(wordId: Long) = dao.toggleMastered(wordId)

    suspend fun moveToBookcase(wordId: Long, targetBookcaseId: Long) =
        dao.moveToBookcase(wordId, targetBookcaseId)

    suspend fun changeDateTime(wordId: Long, newMillis: Long) = dao.setCreatedAt(wordId, newMillis)

    suspend fun delete(wordId: Long) = dao.deleteById(wordId)
}
