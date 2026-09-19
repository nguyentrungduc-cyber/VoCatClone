package com.vocatclone.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Bảng nối many-to-many giữa Word và Bookcase.
 * Một từ (Word) có thể thuộc nhiều bộ từ (Bookcase) cùng lúc, và ngược lại.
 */
@Entity(
    tableName = "word_bookcase_cross_ref",
    primaryKeys = ["wordId", "bookcaseId"],
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BookcaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookcaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId"), Index("bookcaseId")]
)
data class WordBookcaseCrossRef(
    val wordId: Long,
    val bookcaseId: Long
)
