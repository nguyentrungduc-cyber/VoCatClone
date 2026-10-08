package com.vocatclone.app.ui.word

/**
 * Đầy đủ thông tin của MỘT từ để màn "Xem chi tiết từ vựng" hiển thị, dạng dữ liệu cho GIAO DIỆN.
 *
 * Khi nối dữ liệu thật, Repository đổi từ [com.vocatclone.app.data.local.entity.WordWithMeanings]:
 * - các cột cùng tên của WordEntity: term, pronunciation, partOfSpeech, exampleEn, exampleVi, synonym,
 *   description, imageUri, isMastered, reviewCount, lastReviewedAt, createdAt
 * - meanings: WordWithMeanings.sortedMeanings (đã sắp theo orderIndex), chỉ lấy chuỗi nghĩa
 */
data class WordDetailUi(
    val id: Long,
    val term: String,
    val pronunciation: String? = null,
    val partOfSpeech: String? = null,
    val meanings: List<String>,
    val exampleEn: String? = null,       // câu ví dụ (ngôn ngữ của từ)
    val exampleVi: String? = null,       // bản dịch câu ví dụ (ngôn ngữ của nghĩa)
    val synonym: String? = null,
    val description: String? = null,
    val imageUri: String? = null,
    val isMastered: Boolean = false,
    val reviewCount: Int = 0,
    val lastReviewedAt: Long? = null,    // null nghĩa là chưa ôn lần nào
    val createdAt: Long                  // mili giây
)
