package com.vocatclone.app.data.repository

import com.vocatclone.app.data.remote.LookupError
import com.vocatclone.app.data.remote.LookupException
import com.vocatclone.app.data.remote.TranslationApi

/**
 * Gợi ý nghĩa cho một từ. Đứng giữa form từ vựng và [TranslationApi]: đổi tên ngôn ngữ hiển thị
 * trên giao diện ("English (US)") sang mã mà dịch vụ hiểu ("en"), và gói lỗi vào Result để
 * ViewModel không phải bắt ngoại lệ.
 */
class TranslationRepository(private val api: TranslationApi = TranslationApi) {

    suspend fun suggestMeanings(
        term: String,
        wordLanguage: String,
        meaningLanguage: String
    ): Result<List<String>> {
        val from = languageCode(wordLanguage)
        val to = languageCode(meaningLanguage)
        if (from == null || to == null) {
            return Result.failure(LookupException(LookupError.UNSUPPORTED_LANGUAGE))
        }
        if (from == to) {
            return Result.failure(LookupException(LookupError.UNSUPPORTED_LANGUAGE))
        }
        return runCatching { api.translate(term, from, to) }
    }

    /**
     * Đổi tên ngôn ngữ trên giao diện sang mã ISO. null nghĩa là chưa hỗ trợ ngôn ngữ đó,
     * khi đó người dùng vẫn gõ nghĩa tay như thường.
     */
    private fun languageCode(displayName: String): String? = when (displayName.trim()) {
        "English (US)", "English (UK)" -> "en"
        "Vietnamese" -> "vi"
        "Korean" -> "ko"
        "Japanese" -> "ja"
        "Chinese (Simplified)" -> "zh"
        "French" -> "fr"
        "German" -> "de"
        "Spanish" -> "es"
        else -> null
    }
}
