package com.vocatclone.app.ui.bookcase

/** Số ký tự tối đa của tên bộ từ vựng (khớp bộ đếm "0 / 40" của VoCat gốc). */
const val BOOKCASE_NAME_MAX = 40

/** Danh sách ngôn ngữ mặc định cho hai ô chọn Word và Meaning. Thêm hoặc bớt tùy ý. */
val DEFAULT_LANGUAGES = listOf(
    "English (US)", "English (UK)", "Vietnamese", "Korean", "Japanese",
    "Chinese (Simplified)", "French", "German", "Spanish"
)

/**
 * Một tủ sách để chọn trong ô "Bookcase" của form.
 * Tương ứng [com.vocatclone.app.data.local.entity.BookshelfEntity] (tủ sách; VoCat gốc gọi là "Bookcase").
 */
data class ShelfOption(
    val id: Long,
    val name: String
)

/**
 * Giá trị người dùng nhập vào form tạo/sửa bộ từ vựng.
 * Tương ứng [com.vocatclone.app.data.local.entity.BookcaseEntity] (bộ từ vựng; VoCat gốc gọi là "Vocabulary"):
 * shelfId -> bookshelfId, name -> name, sourceLang/targetLang -> hai cột ngôn ngữ cùng tên.
 */
data class BookcaseFormValues(
    val shelfId: Long? = null,
    val name: String = "",
    val sourceLang: String = "English (US)",   // ngôn ngữ của từ (ô Word)
    val targetLang: String = "Vietnamese"      // ngôn ngữ của nghĩa (ô Meaning)
)
