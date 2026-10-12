package com.vocatclone.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Một dòng nghĩa tiếng Việt của 1 từ.
 * Quan hệ 1-n với Words — 1 Word có nhiều WordMeaning.
 * orderIndex giữ đúng thứ tự hiển thị như trên app gốc.
 */
@Entity(
    tableName = "word_meanings",
    foreignKeys = [
        ForeignKey(
            entity = WordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("wordId")]
)
data class WordMeaningEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val wordId: Long,
    val meaning: String,
    val orderIndex: Int = 0
)
