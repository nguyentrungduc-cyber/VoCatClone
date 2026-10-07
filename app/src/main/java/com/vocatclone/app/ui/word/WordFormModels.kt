package com.vocatclone.app.ui.word

/** Danh sách loại từ để chọn trong form (cột partOfSpeech). Thêm hoặc bớt tùy ý. */
val PART_OF_SPEECH_OPTIONS = listOf(
    "noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "interjection"
)

/**
 * Các trường TÙY CHỌN của form, bật/tắt trong hộp thoại "Editor Settings".
 * (Từ và nghĩa luôn hiện vì bắt buộc.) VoCat gốc còn có giới tính, phái sinh, từ tương tự,
 * từ trái nghĩa; database của nhóm chưa có các cột đó nên chưa đưa vào.
 */
enum class WordField { IMAGE, PRONUNCIATION, PART_OF_SPEECH, EXAMPLE, SYNONYM, DESCRIPTION }

/** Các trường hiện sẵn khi mới mở (giống ảnh VoCat gốc: ảnh, phát âm, mô tả). */
val DEFAULT_VISIBLE_FIELDS: Set<WordField> =
    setOf(WordField.IMAGE, WordField.PRONUNCIATION, WordField.DESCRIPTION)

/**
 * Giá trị người dùng nhập trong form Thêm/Sửa từ vựng.
 *
 * Khi lưu, ViewModel đổi sang entity:
 * - term, pronunciation, partOfSpeech, exampleEn, exampleVi, synonym, description, imageUri
 *   -> các cột cùng tên của WordEntity (chuỗi rỗng đổi thành null)
 * - meanings -> truyền vào WordDao.insertWithMeanings / updateWithMeanings (dùng [cleanMeanings])
 */
data class WordFormValues(
    val term: String = "",
    val meanings: List<String> = listOf(""),     // luôn có ít nhất 1 ô; ô đầu là bắt buộc
    val pronunciation: String = "",
    val partOfSpeech: String? = null,
    val exampleEn: String = "",                  // câu ví dụ (ngôn ngữ của từ)
    val exampleVi: String = "",                  // bản dịch câu ví dụ (ngôn ngữ của nghĩa)
    val synonym: String = "",
    val description: String = "",
    val imageUri: String? = null
)

/** Chỉ bấm Add/Save được khi đã nhập từ và ít nhất một nghĩa. */
val WordFormValues.canSubmit: Boolean
    get() = term.isNotBlank() && meanings.any { it.isNotBlank() }

/** Danh sách nghĩa sau khi bỏ ô trống, cắt khoảng trắng và bỏ trùng (giữ thứ tự nhập). */
fun WordFormValues.cleanMeanings(): List<String> =
    meanings.map { it.trim() }.filter { it.isNotEmpty() }.distinctBy { it.lowercase() }

/** Trường này đã có dữ liệu chưa. Trường có dữ liệu thì luôn hiện, kể cả khi bị tắt trong cài đặt. */
fun WordFormValues.hasValue(field: WordField): Boolean = when (field) {
    WordField.IMAGE -> imageUri != null
    WordField.PRONUNCIATION -> pronunciation.isNotBlank()
    WordField.PART_OF_SPEECH -> partOfSpeech != null
    WordField.EXAMPLE -> exampleEn.isNotBlank() || exampleVi.isNotBlank()
    WordField.SYNONYM -> synonym.isNotBlank()
    WordField.DESCRIPTION -> description.isNotBlank()
}

/**
 * Thêm một nghĩa gợi ý vào danh sách: bỏ qua nếu đã có, điền vào ô trống đầu tiên nếu có,
 * không thì thêm ô mới ở cuối.
 */
fun WordFormValues.withMeaning(text: String): WordFormValues {
    val clean = text.trim()
    if (clean.isEmpty() || meanings.any { it.trim().equals(clean, ignoreCase = true) }) return this
    val blank = meanings.indexOfFirst { it.isBlank() }
    return if (blank >= 0) {
        copy(meanings = meanings.mapIndexed { i, m -> if (i == blank) clean else m })
    } else {
        copy(meanings = meanings + clean)
    }
}
