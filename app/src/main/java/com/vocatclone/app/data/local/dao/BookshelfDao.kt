package com.vocatclone.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vocatclone.app.data.local.entity.BookshelfEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookshelfDao {

    /** Lấy tất cả tủ sách của user hiện tại — firebaseUid từ FirebaseAuth.currentUser?.uid */
    @Query("SELECT * FROM bookshelves WHERE firebaseUid = :uid ORDER BY createdAt DESC")
    fun getByUser(uid: String): Flow<List<BookshelfEntity>>

    @Query("SELECT * FROM bookshelves WHERE id = :id")
    suspend fun getById(id: Long): BookshelfEntity?

    @Insert
    suspend fun insert(bookshelf: BookshelfEntity): Long

    @Update
    suspend fun update(bookshelf: BookshelfEntity)

    @Delete
    suspend fun delete(bookshelf: BookshelfEntity)
}
