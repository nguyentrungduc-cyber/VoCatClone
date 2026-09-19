package com.vocatclone.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vocatclone.app.data.local.entity.WordBookcaseCrossRef
import com.vocatclone.app.data.local.entity.WordEntity
import com.vocatclone.app.data.local.entity.WordWithBookcases
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {

    // Lấy các từ thuộc 1 bookcase — join qua bảng nối many-to-many
    @Transaction
    @Query(
        """
        SELECT words.* FROM words
        INNER JOIN word_bookcase_cross_ref ON words.id = word_bookcase_cross_ref.wordId
        WHERE word_bookcase_cross_ref.bookcaseId = :bookcaseId
        ORDER BY words.createdAt DESC
        """
    )
    fun getByBookcase(bookcaseId: Long): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getById(id: Long): WordEntity?

    // Lấy 1 từ kèm toàn bộ các bookcase mà nó đang thuộc về
    @Transaction
    @Query("SELECT * FROM words WHERE id = :id")
    suspend fun getWithBookcases(id: Long): WordWithBookcases?

    // Dùng cho chế độ ôn tập: lấy các từ chưa thuộc trong 1 bookcase để ưu tiên luyện tập
    @Query(
        """
        SELECT words.* FROM words
        INNER JOIN word_bookcase_cross_ref ON words.id = word_bookcase_cross_ref.wordId
        WHERE word_bookcase_cross_ref.bookcaseId = :bookcaseId AND words.isMastered = 0
        """
    )
    suspend fun getUnmastered(bookcaseId: Long): List<WordEntity>

    @Insert
    suspend fun insert(word: WordEntity): Long

    @Update
    suspend fun update(word: WordEntity)

    @Delete
    suspend fun delete(word: WordEntity)

    // --- Quản lý quan hệ many-to-many Word <-> Bookcase ---

    @Insert
    suspend fun assignToBookcase(crossRef: WordBookcaseCrossRef)

    @Query("DELETE FROM word_bookcase_cross_ref WHERE wordId = :wordId AND bookcaseId = :bookcaseId")
    suspend fun unassignFromBookcase(wordId: Long, bookcaseId: Long)

    // Tiện dụng khi tạo từ mới: insert từ + gán ngay vào 1 bookcase trong cùng 1 transaction
    @Transaction
    suspend fun insertWithBookcase(word: WordEntity, bookcaseId: Long): Long {
        val wordId = insert(word)
        assignToBookcase(WordBookcaseCrossRef(wordId = wordId, bookcaseId = bookcaseId))
        return wordId
    }
}
