package com.vocatclone.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vocatclone.app.data.local.entity.WordEntity
import com.vocatclone.app.data.local.entity.WordMeaningEntity
import com.vocatclone.app.data.local.entity.WordWithMeanings
import kotlinx.coroutines.flow.Flow

/**
 * abstract class (không phải interface) để viết được hàm @Transaction có logic
 * (insertWithMeanings, updateWithMeanings — lưu từ + nghĩa trong cùng 1 transaction).
 */
@Dao
abstract class WordDao {

    // ---------- Đọc ----------

    @Transaction
    @Query("SELECT * FROM words WHERE bookcaseId = :bookcaseId ORDER BY createdAt DESC")
    abstract fun getByBookcase(bookcaseId: Long): Flow<List<WordWithMeanings>>

    @Transaction
    @Query("SELECT * FROM words WHERE id = :id")
    abstract suspend fun getWithMeanings(id: Long): WordWithMeanings?

    /** Dùng cho ôn tập — chỉ lấy các từ chưa thuộc. */
    @Transaction
    @Query("SELECT * FROM words WHERE bookcaseId = :bookcaseId AND isMastered = 0")
    abstract suspend fun getUnmastered(bookcaseId: Long): List<WordWithMeanings>

    // ---------- Ghi (từng bảng) ----------

    @Insert
    abstract suspend fun insertWord(word: WordEntity): Long

    @Insert
    abstract suspend fun insertMeanings(meanings: List<WordMeaningEntity>)

    @Update
    abstract suspend fun updateWord(word: WordEntity)

    @Query("DELETE FROM word_meanings WHERE wordId = :wordId")
    abstract suspend fun deleteMeaningsOf(wordId: Long)

    @Delete
    abstract suspend fun delete(word: WordEntity)

    // ---------- Ghi (gộp — dùng cái này ở tầng trên) ----------

    /**
     * Thêm 1 từ kèm danh sách nghĩa trong 1 transaction.
     * Nếu lưu từ ok nhưng lưu nghĩa thất bại → cả 2 đều rollback.
     */
    @Transaction
    open suspend fun insertWithMeanings(word: WordEntity, meanings: List<String>): Long {
        val wordId = insertWord(word)
        insertMeanings(meanings.toEntities(wordId))
        return wordId
    }

    /** Sửa từ và thay toàn bộ danh sách nghĩa bằng danh sách mới. */
    @Transaction
    open suspend fun updateWithMeanings(word: WordEntity, meanings: List<String>) {
        updateWord(word)
        deleteMeaningsOf(word.id)
        insertMeanings(meanings.toEntities(word.id))
    }

    private fun List<String>.toEntities(wordId: Long): List<WordMeaningEntity> =
        filter { it.isNotBlank() }
            .mapIndexed { index, text ->
                WordMeaningEntity(wordId = wordId, meaning = text.trim(), orderIndex = index)
            }
}
