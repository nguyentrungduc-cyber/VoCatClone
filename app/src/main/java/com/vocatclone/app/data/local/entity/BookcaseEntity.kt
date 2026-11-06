package com.vocatclone.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bộ từ vựng theo chủ đề, thuộc về đúng 1 Bookshelf.
 * Ví dụ: "Household items", "Supermarket", "Hotel".
 * Mỗi bộ có cặp ngôn ngữ riêng (khớp form tạo Từ vựng của VoCat gốc):
 * sourceLang = ngôn ngữ của từ, targetLang = ngôn ngữ của nghĩa.
 */
@Entity(
    tableName = "bookcases",
    foreignKeys = [
        ForeignKey(
            entity = BookshelfEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookshelfId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("bookshelfId")]
)
data class BookcaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bookshelfId: Long,
    val name: String,
    val sourceLang: String = "Tiếng Anh (Mỹ)",   // ngôn ngữ của từ
    val targetLang: String = "Tiếng Việt",       // ngôn ngữ của nghĩa
    val createdAt: Long = System.currentTimeMillis()
)
