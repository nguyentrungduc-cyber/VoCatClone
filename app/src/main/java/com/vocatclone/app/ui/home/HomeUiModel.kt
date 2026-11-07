package com.vocatclone.app.ui.home

/**
 * Một bộ từ vựng, VoCat gốc gọi là "Vocabulary" (ví dụ "Household items").
 * Tương ứng [com.vocatclone.app.data.local.entity.BookcaseEntity] trong database.
 *
 * Đây là model cho GIAO DIỆN: chỉ chứa những gì màn hình cần vẽ. Số từ và số từ đã thuộc
 * sẽ do Repository tính từ bảng words khi nối dữ liệu thật.
 */
data class BookcaseUi(
    val id: Long,
    val name: String,
    val sourceLang: String,
    val targetLang: String,
    val wordCount: Int,
    val masteredCount: Int
) {
    /** Tỉ lệ đã thuộc, từ 0f đến 1f (bộ trống thì là 0). */
    val progress: Float
        get() = if (wordCount == 0) 0f else (masteredCount.toFloat() / wordCount).coerceIn(0f, 1f)
}

/**
 * Một tủ sách, VoCat gốc gọi là "Bookcase" (ví dụ "English vlog-home"), chứa nhiều bộ từ vựng.
 * Tương ứng [com.vocatclone.app.data.local.entity.BookshelfEntity] trong database.
 *
 * Lưu ý tên gọi: trong code của nhóm Bookshelf là tủ sách, Bookcase là bộ từ vựng;
 * còn trên giao diện VoCat gốc thì ngược lại cách gọi (Bookcase = tủ sách, Vocabulary = bộ từ vựng).
 */
data class BookshelfUi(
    val id: Long,
    val name: String,
    val bookcases: List<BookcaseUi>
) {
    /** Tổng số từ của cả tủ sách, hiện trong ngoặc cạnh tên. */
    val wordCount: Int
        get() = bookcases.sumOf { it.wordCount }
}
