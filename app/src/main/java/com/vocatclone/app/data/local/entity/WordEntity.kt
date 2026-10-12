package com.vocatclone.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Từ vựng — thuộc về đúng 1 Bookcase (quan hệ 1-n).
 * KHÔNG có cột "meaning" — nghĩa tiếng Việt nằm ở [WordMeaningEntity]
 * vì 1 từ có thể có nhiều nghĩa (vd: "cook" = đầu bếp / nấu / chín).
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
    ],
    indices = [Index("bookcaseId")]
)
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookcaseId: Long,

    val term: String,                   // Từ tiếng Anh, vd: "toothpaste"
    val pronunciation: String? = null,  // IPA, vd: "/ˈtuːθ.peɪst/"
    val partOfSpeech: String? = null,   // noun, verb, adjective...
    val exampleEn: String? = null,      // Câu ví dụ nguyên văn (Anh)
    val exampleVi: String? = null,      // Bản dịch câu ví dụ (Việt)
    val synonym: String? = null,        // Từ đồng nghĩa
    val description: String? = null,    // Định nghĩa tiếng Anh
    val imageUri: String? = null,       // Đường dẫn ảnh minh họa

    val isMastered: Boolean = false,
    val lastReviewedAt: Long? = null,
    val reviewCount: Int = 0,

    val createdAt: Long = System.currentTimeMillis()
)
