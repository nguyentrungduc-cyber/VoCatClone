package com.vocatclone.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Một từ vựng thuộc về một Bookcase.
 * Chứa đầy đủ thông tin cho tính năng "Quản lý từ vựng" (nhóm lõi, mục 1)
 * và "Theo dõi tiến độ học" (nhóm ôn tập, mục 9).
 */
@Entity(
    tableName = "words",
    foreignKeys = [
        ForeignKey(
            entity = BookcaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookcaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(index = true)
    val bookcaseId: Long,

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
