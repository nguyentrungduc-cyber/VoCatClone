package com.vocatclone.app.ui.home

/**
 * Một từ khớp ô tìm kiếm trên màn Home. Khác [com.vocatclone.app.ui.bookcase.WordItemUi] ở chỗ
 * có kèm tên bộ từ vựng, vì kết quả tìm kiếm trải trên nhiều bộ nên người dùng cần biết từ nằm ở đâu.
 */
data class WordSearchUi(
    val id: Long,
    val term: String,
    val meanings: List<String>,
    val bookcaseId: Long,
    val bookcaseName: String
)
