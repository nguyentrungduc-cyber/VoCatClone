package com.vocatclone.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Một "bộ từ" / bookcase — nhóm các từ vựng theo chủ đề.
 * Ví dụ: "Từ vựng TOEIC Unit 1", "Chủ đề Gia đình"...
 */
@Entity(tableName = "bookcases")
data class BookcaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
