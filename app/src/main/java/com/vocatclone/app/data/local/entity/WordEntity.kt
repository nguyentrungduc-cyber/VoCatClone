package com.vocatclone.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Một từ vựng — KHÔNG còn gắn cứng vào 1 Bookcase.
 * Một từ có thể thuộc nhiều Bookcase cùng lúc (many-to-many), xem [WordBookcaseCrossRef].
 * Chứa đầy đủ thông tin cho tính năng "Quản lý từ vựng" (nhóm lõi, mục 1)
 * và "Theo dõi tiến độ học" (nhóm ôn tập, mục 9).
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val term: String,           // Từ tiếng Anh
    val meaning: String,        // Nghĩa tiếng Việt
    val partOfSpeech: String? = null, // Loại từ: noun, verb, adjective...
    val example: String? = null,      // Câu ví dụ
    val imageUri: String? = null,     // Đường dẫn ảnh minh họa (tùy chọn)

    // Theo dõi tiến độ học (nhóm ôn tập, mục 9)
    val isMastered: Boolean = false,
    val lastReviewedAt: Long? = null,
    val reviewCount: Int = 0,

    val createdAt: Long = System.currentTimeMillis()
)
