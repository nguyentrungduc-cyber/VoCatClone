package com.vocatclone.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tủ sách — nhóm lớn nhất, chứa nhiều Bookcase.
 * Ví dụ: "English vlog-home", "FULL ENGLISH".
 * Tủ sách chỉ có tên; cặp ngôn ngữ nằm ở từng Bookcase (xem [BookcaseEntity]).
 * firebaseUid: UID từ Firebase Auth — mỗi user chỉ thấy tủ sách của mình.
 */
@Entity(
    tableName = "bookshelves",
    indices = [Index("firebaseUid")]
)
data class BookshelfEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firebaseUid: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
