package com.vocatclone.app.ui.bookcase

/**
 * Một từ trong danh sách của bộ từ vựng (màn Vocabulary Detail), dạng dữ liệu cho GIAO DIỆN.
 *
 * Khi nối dữ liệu thật, Repository đổi từ [com.vocatclone.app.data.local.entity.WordWithMeanings]:
 * - term, pronunciation, partOfSpeech, description, imageUri, isMastered, createdAt: lấy từ WordEntity
 * - meanings: lấy từ WordWithMeanings.sortedMeanings (đã sắp theo orderIndex), chỉ cần chuỗi nghĩa
 */
data class WordItemUi(
    val id: Long,
    val term: String,
    val pronunciation: String? = null,    // IPA, ví dụ "/ˈkʊk/"
    val partOfSpeech: String? = null,     // noun, verb...
    val meanings: List<String>,           // các nghĩa tiếng Việt, đã sắp thứ tự
    val description: String? = null,      // định nghĩa tiếng Anh
    val imageUri: String? = null,         // có ảnh hay không (xem ghi chú ở BookcaseDetailScreen)
    val isMastered: Boolean = false,
    val createdAt: Long                   // mili giây, hiện thành ngày giờ trên thẻ
)
