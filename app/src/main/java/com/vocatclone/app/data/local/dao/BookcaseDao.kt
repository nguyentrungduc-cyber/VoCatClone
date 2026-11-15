package com.vocatclone.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vocatclone.app.data.local.entity.BookcaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookcaseDao {

    @Query("SELECT * FROM bookcases WHERE bookshelfId = :bookshelfId ORDER BY createdAt DESC")
    fun getByBookshelf(bookshelfId: Long): Flow<List<BookcaseEntity>>

    /** Tất cả bộ từ vựng của user (qua tủ sách) — Home dùng để ghép với tủ sách. */
    @Query(
        "SELECT bc.* FROM bookcases bc " +
            "INNER JOIN bookshelves bs ON bc.bookshelfId = bs.id " +
            "WHERE bs.firebaseUid = :uid ORDER BY bc.createdAt DESC"
    )
    fun getAllByUser(uid: String): Flow<List<BookcaseEntity>>

    @Query("SELECT * FROM bookcases WHERE id = :id")
    suspend fun getById(id: Long): BookcaseEntity?

    @Query("SELECT * FROM bookcases WHERE id = :id")
    fun observeById(id: Long): Flow<BookcaseEntity?>

    @Insert
    suspend fun insert(bookcase: BookcaseEntity): Long

    @Update
    suspend fun update(bookcase: BookcaseEntity)

    @Delete
    suspend fun delete(bookcase: BookcaseEntity)
}
