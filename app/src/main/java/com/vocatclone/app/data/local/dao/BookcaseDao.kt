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

    @Query("SELECT * FROM bookcases ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BookcaseEntity>>

    @Query("SELECT * FROM bookcases WHERE id = :id")
    suspend fun getById(id: Long): BookcaseEntity?

    @Insert
    suspend fun insert(bookcase: BookcaseEntity): Long

    @Update
    suspend fun update(bookcase: BookcaseEntity)

    @Delete
    suspend fun delete(bookcase: BookcaseEntity)
}
