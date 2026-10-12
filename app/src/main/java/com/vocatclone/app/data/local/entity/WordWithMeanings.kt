package com.vocatclone.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 1 Word kèm toàn bộ nghĩa — dùng khi hiển thị danh sách/chi tiết từ.
 * Room không đảm bảo thứ tự của [meanings]: dùng [sortedMeanings] khi hiển thị.
 */
data class WordWithMeanings(
    @Embedded val word: WordEntity,
    @Relation(parentColumn = "id", entityColumn = "wordId")
    val meanings: List<WordMeaningEntity>
) {
    val sortedMeanings: List<WordMeaningEntity>
        get() = meanings.sortedBy { it.orderIndex }

    val primaryMeaning: String
        get() = sortedMeanings.firstOrNull()?.meaning ?: ""
}
