package com.vocatclone.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

/** Vì sao tra nghĩa không xong. Màn hình dựa vào đây để chọn câu nhắc, nên không ghép sẵn chuỗi ở đây. */
enum class LookupError {
    NO_NETWORK,
    TIMEOUT,
    RATE_LIMITED,
    SERVER,
    NO_RESULT,
    UNSUPPORTED_LANGUAGE
}

/** Lỗi tra nghĩa kèm lý do đã phân loại. */
class LookupException(val reason: LookupError) : Exception(reason.name)

/**
 * Gọi MyMemory để gợi ý nghĩa. Chọn dịch vụ này vì dùng được ngay, không cần khóa API và không
 * cần đăng ký, hợp với một đồ án môn học.
 *
 * Dùng HttpURLConnection của Android để khỏi thêm thư viện mạng; phần gọi mạng chạy trên
 * Dispatchers.IO nên không chặn giao diện. KHÔNG gửi kèm email người dùng (MyMemory cho hạn mức
 * cao hơn nếu gửi), vì đó là dữ liệu cá nhân và việc tra nghĩa không cần tới.
 */
object TranslationApi {

    private const val ENDPOINT = "https://api.mymemory.translated.net/get"
    private const val TIMEOUT_MS = 8_000
    private const val MAX_SUGGESTIONS = 5

    /**
     * Trả về danh sách nghĩa gợi ý cho [text], dịch từ [from] sang [to] (mã ngôn ngữ, ví dụ en, vi).
     * Danh sách đã bỏ trùng và bỏ những kết quả trùng chính từ gốc. Ném [LookupException] nếu lỗi.
     */
    suspend fun translate(text: String, from: String, to: String): List<String> = withContext(Dispatchers.IO) {
        val query = URLEncoder.encode(text, "UTF-8")
        val pair = URLEncoder.encode("$from|$to", "UTF-8")
        val url = URL("$ENDPOINT?q=$query&langpair=$pair")

        var connection: HttpURLConnection? = null
        val body = try {
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                setRequestProperty("Accept", "application/json")
            }
            when (connection.responseCode) {
                in 200..299 -> connection.inputStream.bufferedReader().use { it.readText() }
                429 -> throw LookupException(LookupError.RATE_LIMITED)
                else -> throw LookupException(LookupError.SERVER)
            }
        } catch (e: LookupException) {
            throw e
        } catch (e: UnknownHostException) {
            throw LookupException(LookupError.NO_NETWORK)
        } catch (e: SocketTimeoutException) {
            throw LookupException(LookupError.TIMEOUT)
        } catch (e: Exception) {
            // Mọi lỗi mạng còn lại (mất sóng giữa chừng, DNS hỏng, TLS...) đều coi là không có mạng.
            throw LookupException(LookupError.NO_NETWORK)
        } finally {
            connection?.disconnect()
        }

        parse(body, original = text)
    }

    /** MyMemory trả về một bản dịch chính (responseData) và nhiều bản khớp (matches). Gộp lại, bỏ trùng. */
    private fun parse(body: String, original: String): List<String> {
        val json = runCatching { JSONObject(body) }.getOrElse { throw LookupException(LookupError.SERVER) }

        // responseStatus có thể là số hoặc chuỗi tùy trường hợp.
        val status = json.opt("responseStatus")?.toString()?.toIntOrNull()
        if (status != null && status !in 200..299) {
            throw LookupException(if (status == 429) LookupError.RATE_LIMITED else LookupError.SERVER)
        }

        val results = mutableListOf<String>()
        json.optJSONObject("responseData")?.optString("translatedText")?.let { results.add(it) }
        json.optJSONArray("matches")?.let { matches ->
            for (i in 0 until matches.length()) {
                matches.optJSONObject(i)?.optString("translation")?.let { results.add(it) }
            }
        }

        val cleaned = results
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.equals(original.trim(), ignoreCase = true) }
            .distinctBy { it.lowercase() }
            .take(MAX_SUGGESTIONS)

        if (cleaned.isEmpty()) throw LookupException(LookupError.NO_RESULT)
        return cleaned
    }
}
