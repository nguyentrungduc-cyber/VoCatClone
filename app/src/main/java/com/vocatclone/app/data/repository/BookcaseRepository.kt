package com.vocatclone.app.data.repository

import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.local.entity.BookcaseEntity
import com.vocatclone.app.ui.bookcase.BOOKCASE_NAME_MAX
import com.vocatclone.app.ui.bookcase.BookcaseFormValues
import kotlinx.coroutines.flow.Flow

/** Bộ từ vựng (Bookcase trong code, "Vocabulary" trên giao diện VoCat gốc). */
class BookcaseRepository(db: AppDatabase) {

    private val dao = db.bookcaseDao()

    fun observe(bookcaseId: Long): Flow<BookcaseEntity?> = dao.observeById(bookcaseId)

    suspend fun get(bookcaseId: Long): BookcaseEntity? = dao.getById(bookcaseId)

    /** Tạo bộ từ vựng mới. [BookcaseFormValues.shelfId] phải khác null (ViewModel lo chuyện này). */
    suspend fun create(values: BookcaseFormValues): Long {
        val shelfId = requireNotNull(values.shelfId) { "Chưa chọn tủ sách" }
        return dao.insert(
            BookcaseEntity(
                bookshelfId = shelfId,
                name = values.cleanName(),
                sourceLang = values.sourceLang,
                targetLang = values.targetLang
            )
        )
    }

    /** Sửa tên, ngôn ngữ và tủ sách chứa bộ từ vựng. Giữ nguyên createdAt. */
    suspend fun update(bookcaseId: Long, values: BookcaseFormValues) {
        val old = dao.getById(bookcaseId) ?: return
        dao.update(
            old.copy(
                bookshelfId = values.shelfId ?: old.bookshelfId,
                name = values.cleanName(),
                sourceLang = values.sourceLang,
                targetLang = values.targetLang
            )
        )
    }

    /** Xóa bộ từ vựng cùng toàn bộ từ bên trong (CASCADE). */
    suspend fun delete(bookcaseId: Long) {
        dao.getById(bookcaseId)?.let { dao.delete(it) }
    }

    private fun BookcaseFormValues.cleanName(): String {
        val clean = name.trim().take(BOOKCASE_NAME_MAX)
        require(clean.isNotEmpty()) { "Tên bộ từ vựng không được để trống" }
        return clean
    }
}
