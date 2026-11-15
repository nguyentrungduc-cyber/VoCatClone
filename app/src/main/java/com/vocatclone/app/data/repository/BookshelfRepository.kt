package com.vocatclone.app.data.repository

import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.local.entity.BookshelfEntity
import com.vocatclone.app.ui.bookcase.ShelfOption
import com.vocatclone.app.ui.home.BookshelfUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** Tủ sách (Bookshelf) và dữ liệu cho màn Home: tủ sách -> bộ từ vựng -> số từ. */
class BookshelfRepository(
    db: AppDatabase,
    private val guard: OwnershipGuard = OwnershipGuard(db)
) {

    private val shelfDao = db.bookshelfDao()
    private val bookcaseDao = db.bookcaseDao()
    private val wordDao = db.wordDao()

    /** Danh sách tủ sách của [uid], mỗi tủ kèm các bộ từ vựng và số từ / số từ đã thuộc. Tự cập nhật khi DB đổi. */
    fun observeShelves(uid: String): Flow<List<BookshelfUi>> = combine(
        shelfDao.getByUser(uid),
        bookcaseDao.getAllByUser(uid),
        wordDao.observeCountsByUser(uid)
    ) { shelves, bookcases, counts ->
        val countById = counts.associateBy { it.bookcaseId }
        val bookcasesByShelf = bookcases.groupBy { it.bookshelfId }
        shelves.map { shelf ->
            shelf.toUi(
                bookcasesByShelf[shelf.id].orEmpty().map { bc ->
                    bc.toUi(
                        wordCount = countById[bc.id]?.total ?: 0,
                        masteredCount = countById[bc.id]?.mastered ?: 0
                    )
                }
            )
        }
    }

    /** Danh sách rút gọn cho ô chọn "Bookcase" trong form tạo bộ từ vựng. */
    fun observeShelfOptions(uid: String): Flow<List<ShelfOption>> =
        shelfDao.getByUser(uid).map { list -> list.map { ShelfOption(it.id, it.name) } }

    suspend fun createShelf(uid: String, name: String): Long {
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Tên tủ sách không được để trống" }
        return shelfDao.insert(BookshelfEntity(firebaseUid = uid, name = clean))
    }

    suspend fun renameShelf(shelfId: Long, name: String) {
        val clean = name.trim()
        require(clean.isNotEmpty()) { "Tên tủ sách không được để trống" }
        guard.requireShelf(shelfId)
        val shelf = shelfDao.getById(shelfId) ?: return
        shelfDao.update(shelf.copy(name = clean))
    }

    /** Xóa tủ sách; các bộ từ vựng, từ và nghĩa bên trong bị xóa theo (ON DELETE CASCADE). */
    suspend fun deleteShelf(shelfId: Long) {
        guard.requireShelf(shelfId)
        shelfDao.getById(shelfId)?.let { shelfDao.delete(it) }
    }

    /**
     * Trả về id tủ sách đầu tiên của user; chưa có thì tạo tủ [DEFAULT_SHELF_NAME].
     * Để user mới tạo được bộ từ vựng ngay mà không cần tạo tủ sách trước.
     */
    suspend fun ensureDefaultShelf(uid: String): Long {
        val existing = shelfDao.getFirstByUser(uid)
        return existing?.id ?: createShelf(uid, DEFAULT_SHELF_NAME)
    }

    companion object {
        const val DEFAULT_SHELF_NAME = "Tủ sách của tôi"
    }
}
