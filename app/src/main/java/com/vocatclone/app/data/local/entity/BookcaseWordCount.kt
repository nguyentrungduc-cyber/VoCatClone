package com.vocatclone.app.data.local.entity

/** Kết quả truy vấn đếm: số từ và số từ đã thuộc của 1 bộ từ vựng (không phải bảng). */
data class BookcaseWordCount(
    val bookcaseId: Long,
    val total: Int,
    val mastered: Int
)
