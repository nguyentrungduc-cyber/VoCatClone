package com.vocatclone.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.vocatclone.app.data.local.entity.BookcaseWordCount
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

    @Transaction
    @Query("SELECT * FROM words WHERE id = :id")
    abstract fun observeWithMeanings(id: Long): Flow<WordWithMeanings?>

    /** Số từ và số từ đã thuộc của từng bộ từ vựng thuộc user — Home hiện thanh tiến độ. */
    @Query(
        "SELECT w.bookcaseId AS bookcaseId, COUNT(*) AS total, SUM(w.isMastered) AS mastered " +
            "FROM words w " +
            "INNER JOIN bookcases bc ON w.bookcaseId = bc.id " +
            "INNER JOIN bookshelves bs ON bc.bookshelfId = bs.id " +
            "WHERE bs.firebaseUid = :uid GROUP BY w.bookcaseId"
    )
    abstract fun observeCountsByUser(uid: String): Flow<List<BookcaseWordCount>>

    /** 1 nếu từ [wordId] nằm trong bộ từ vựng và tủ sách của user [uid], ngược lại 0. */
    @Query(
        "SELECT COUNT(*) FROM words w " +
            "INNER JOIN bookcases bc ON w.bookcaseId = bc.id " +
            "INNER JOIN bookshelves bs ON bc.bookshelfId = bs.id " +
            "WHERE w.id = :wordId AND bs.firebaseUid = :uid"
    )
    abstract suspend fun countOwned(wordId: Long, uid: String): Int

    /** Dùng cho ôn tập — chỉ lấy các từ chưa thuộc. */
    /**
     * Tìm từ của user [uid] theo [pattern] (đã bọc sẵn dấu %), khớp cả Ở TỪ lẫn Ở NGHĨA.
     *
     * LEFT JOIN sang word_meanings để gõ tiếng Việt cũng ra kết quả; DISTINCT vì một từ nhiều nghĩa
     * sẽ sinh nhiều dòng. ESCAPE '\\' để người dùng gõ % hoặc _ thì tìm đúng ký tự đó.
     * LIMIT để ô tìm kiếm không phải vẽ hàng nghìn dòng.
     */
    @Transaction
    @Query(
        "SELECT DISTINCT w.* FROM words w " +
            "INNER JOIN bookcases bc ON w.bookcaseId = bc.id " +
            "INNER JOIN bookshelves bs ON bc.bookshelfId = bs.id " +
            "LEFT JOIN word_meanings m ON m.wordId = w.id " +
            "WHERE bs.firebaseUid = :uid " +
            "AND (w.term LIKE :pattern ESCAPE '\\' OR m.meaning LIKE :pattern ESCAPE '\\') " +
            "ORDER BY w.term COLLATE NOCASE ASC LIMIT :limit"
    )
    abstract fun searchByUser(uid: String, pattern: String, limit: Int): Flow<List<WordWithMeanings>>

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

    @Query("DELETE FROM words WHERE id = :wordId")
    abstract suspend fun deleteById(wordId: Long)

    @Query("UPDATE words SET bookcaseId = :bookcaseId WHERE id = :wordId")
    abstract suspend fun moveToBookcase(wordId: Long, bookcaseId: Long)

    @Query("UPDATE words SET createdAt = :millis WHERE id = :wordId")
    abstract suspend fun setCreatedAt(wordId: Long, millis: Long)

    /** Đảo đã thuộc / chưa thuộc trong 1 câu lệnh (không cần đọc rồi ghi). */
    @Query("UPDATE words SET isMastered = NOT isMastered WHERE id = :wordId")
    abstract suspend fun toggleMastered(wordId: Long)

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
