package com.vocatclone.app.data.repository

import com.vocatclone.app.data.local.AppDatabase

/** Chưa đăng nhập mà thao tác dữ liệu. */
class NotSignedInException : IllegalStateException("Bạn cần đăng nhập để thực hiện thao tác này")

/** Dữ liệu không thuộc tài khoản đang đăng nhập (hoặc đã bị xóa). */
class NotOwnerException(what: String) : IllegalStateException("Không tìm thấy $what của tài khoản này")

/**
 * Kiểm tra dữ liệu có thuộc user đang đăng nhập không (qua firebaseUid của tủ sách gốc).
 * Mọi thao tác GHI trong Repository phải qua đây, để một tài khoản không sửa/xóa/chuyển được dữ liệu
 * của tài khoản khác (ví dụ chuyển từ sang bộ từ vựng của người khác).
 */
class OwnershipGuard(
    db: AppDatabase,
    private val auth: AuthRepository = AuthRepository()
) {
    private val shelfDao = db.bookshelfDao()
    private val bookcaseDao = db.bookcaseDao()
    private val wordDao = db.wordDao()

    private fun uid(): String = auth.currentUser?.uid ?: throw NotSignedInException()

    suspend fun ownsShelf(shelfId: Long) = shelfDao.countOwned(shelfId, uid()) > 0
    suspend fun ownsBookcase(bookcaseId: Long) = bookcaseDao.countOwned(bookcaseId, uid()) > 0
    suspend fun ownsWord(wordId: Long) = wordDao.countOwned(wordId, uid()) > 0

    suspend fun requireShelf(shelfId: Long) {
        if (!ownsShelf(shelfId)) throw NotOwnerException("tủ sách")
    }

    suspend fun requireBookcase(bookcaseId: Long) {
        if (!ownsBookcase(bookcaseId)) throw NotOwnerException("bộ từ vựng")
    }

    suspend fun requireWord(wordId: Long) {
        if (!ownsWord(wordId)) throw NotOwnerException("từ vựng")
    }
}
