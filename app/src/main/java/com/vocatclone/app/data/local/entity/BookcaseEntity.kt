package com.vocatclone.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bộ từ vựng theo chủ đề, thuộc về đúng 1 Bookshelf.
 * Ví dụ: "Household items", "Supermarket", "Hotel".
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
    val createdAt: Long = System.currentTimeMillis()
)
