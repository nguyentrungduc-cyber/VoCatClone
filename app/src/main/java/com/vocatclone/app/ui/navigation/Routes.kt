package com.vocatclone.app.ui.navigation

/** Giá trị "không có id" cho tham số tùy chọn của route (ví dụ form Thêm: chưa có wordId). */
const val NO_ID = -1L

/**
 * Tất cả route của app ở một chỗ. Màn nào cần tham số thì có hàm dựng đường dẫn tương ứng,
 * không tự ghép chuỗi ở nơi gọi.
 *
 * Luồng chính: SPLASH -> (WELCOME lần đầu) -> LOGIN nếu chưa đăng nhập -> MAIN (5 tab, mở ở tab Vocabulary/Home).
 */
object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"

    // Tên tham số
    const val ARG_BOOKCASE_ID = "bookcaseId"
    const val ARG_WORD_ID = "wordId"

    // Mẫu route (dùng khi khai báo trong NavHost)
    const val BOOKCASE_DETAIL = "bookcase_detail/{$ARG_BOOKCASE_ID}"
    const val BOOKCASE_FORM = "bookcase_form?$ARG_BOOKCASE_ID={$ARG_BOOKCASE_ID}"
    const val WORD_DETAIL = "word_detail/{$ARG_WORD_ID}"
    const val WORD_FORM = "word_form/{$ARG_BOOKCASE_ID}?$ARG_WORD_ID={$ARG_WORD_ID}"

    // Hàm dựng đường dẫn thật để navigate()
    fun bookcaseDetail(bookcaseId: Long) = "bookcase_detail/$bookcaseId"

    /** [bookcaseId] = null: form Tạo mới. Khác null: form Sửa bộ từ vựng đó. */
    fun bookcaseForm(bookcaseId: Long? = null) =
        if (bookcaseId == null) "bookcase_form" else "bookcase_form?$ARG_BOOKCASE_ID=$bookcaseId"

    fun wordDetail(wordId: Long) = "word_detail/$wordId"

    /** Thêm từ vào [bookcaseId]; truyền [wordId] để Sửa từ đó. */
    fun wordForm(bookcaseId: Long, wordId: Long? = null) =
        if (wordId == null) "word_form/$bookcaseId" else "word_form/$bookcaseId?$ARG_WORD_ID=$wordId"
}
