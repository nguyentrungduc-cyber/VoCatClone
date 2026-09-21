package com.vocatclone.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.vocatclone.app.data.local.entity.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    @Query("SELECT * FROM words WHERE bookcaseId = :bookcaseId ORDER BY createdAt DESC")
    fun getByBookcase(bookcaseId: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Long): WordEntity?

    // Dùng cho chế độ ôn tập: lấy các từ chưa thuộc để ưu tiên luyện tập
    @Query("SELECT * FROM words WHERE bookcaseId = :bookcaseId AND isMastered = 0")
    suspend fun getUnmastered(bookcaseId: Long): List<WordEntity>

    @Insert
    suspend fun insert(word: WordEntity): Long

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)
}
