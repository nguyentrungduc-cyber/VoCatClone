package com.vocatclone.app.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

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

    private const val TAG = "TranslationApi"
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
        } catch (e: CancellationException) {
            // Người dùng bấm tra lần nữa: để coroutine hủy bình thường, không biến thành lỗi mạng.
            throw e
        } catch (e: LookupException) {
            throw e
        } catch (e: UnknownHostException) {
            Log.w(TAG, "Không phân giải được tên miền", e)
            throw LookupException(LookupError.NO_NETWORK)
        } catch (e: SocketTimeoutException) {
            Log.w(TAG, "Hết thời gian chờ", e)
            throw LookupException(LookupError.TIMEOUT)
        } catch (e: IOException) {
            Log.w(TAG, "Lỗi mạng khi tra nghĩa", e)
            throw LookupException(LookupError.NO_NETWORK)
        } catch (e: Exception) {
            // Không phải lỗi mạng (thiếu quyền, URL sai...): báo lỗi dịch vụ và ghi log để còn tìm ra.
            Log.e(TAG, "Lỗi không lường trước khi tra nghĩa", e)
            throw LookupException(LookupError.SERVER)
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

        val source = original.trim()
        val cleaned = results
            .map { it.cleanUp() }
            .filter { it.isNotEmpty() && !it.equals(source, ignoreCase = true) }
            .distinctBy { it.lowercase() }

        // Bản dịch chính (phần tử đầu) thường đúng nhất, giữ lại kể cả khi dài.
        val best = cleaned.firstOrNull()
        val shortlist = cleaned.filter { it.looksLikeMeaningOf(source) }.take(MAX_SUGGESTIONS)

        val suggestions = when {
            shortlist.isNotEmpty() -> shortlist
            best != null -> listOf(best)
            else -> emptyList()
        }
        if (suggestions.isEmpty()) throw LookupException(LookupError.NO_RESULT)
        return suggestions
    }

    /**
     * MyMemory trả về các đoạn khớp trong bộ nhớ dịch, còn nguyên thẻ đánh dấu vị trí
     * (`<g x=1 id="9">...</g>`) và thực thể HTML. Bỏ hết để chỉ còn chữ.
     */
    private fun String.cleanUp(): String = this
        .replace(TAG_REGEX, " ")
        .decodeHtmlEntities()
        .replace(WHITESPACE_REGEX, " ")
        .trim()
        .trim('.', ',', ';', ':', '"', '\'')
        .trim()

    private fun String.decodeHtmlEntities(): String {
        var text = this
        for ((entity, char) in HTML_ENTITIES) text = text.replace(entity, char)
        // Thực thể dạng số, ví dụ &#39; là dấu nháy đơn.
        return NUMERIC_ENTITY_REGEX.replace(text) { m ->
            m.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: m.value
        }
    }

    /**
     * Một NGHĨA thì ngắn; đoạn dài là cả câu lấy từ bộ nhớ dịch, không dùng làm nghĩa được.
     * Ngưỡng co giãn theo độ dài từ gốc, để tra cụm từ vẫn nhận được bản dịch dài hơn.
     */
    private fun String.looksLikeMeaningOf(source: String): Boolean {
        val sourceWords = source.split(WHITESPACE_REGEX).count { it.isNotBlank() }.coerceAtLeast(1)
        val words = split(WHITESPACE_REGEX).count { it.isNotBlank() }
        return words <= sourceWords * 3 + 3
    }

    private val TAG_REGEX = Regex("<[^>]*>")
    private val WHITESPACE_REGEX = Regex("\\s+")
    private val NUMERIC_ENTITY_REGEX = Regex("&#(\\d+);")
    private val HTML_ENTITIES = listOf(
        "&amp;" to "&", "&lt;" to "<", "&gt;" to ">",
        "&quot;" to "\"", "&apos;" to "'", "&nbsp;" to " "
    )
}
