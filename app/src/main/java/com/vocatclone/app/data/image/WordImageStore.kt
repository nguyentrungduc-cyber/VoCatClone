package com.vocatclone.app.data.image

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Nơi giữ ảnh minh họa của từ vựng.
 *
 * Ảnh luôn được COPY vào bộ nhớ riêng của app (`filesDir/word_images`) rồi mới lưu đường dẫn vào
 * database. Lý do: URI do bộ chọn ảnh trả về chỉ có quyền đọc tạm thời, mở lại app là hỏng;
 * còn app này là local-first nên ảnh cũng nên nằm cạnh dữ liệu, không phụ thuộc thư viện ngoài.
 */
object WordImageStore {

    private const val DIR_NAME = "word_images"

    private fun dir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    /**
     * Tạo sẵn một file rỗng cho app camera ghi ảnh vào, trả về URI dạng content:// qua FileProvider
     * (từ Android 7 trở đi không được đưa file:// cho app khác).
     */
    fun newCameraTarget(context: Context): Uri {
        val file = File(dir(context), "cam_${UUID.randomUUID()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    /** Ảnh chụp đã nằm sẵn trong thư mục của app: chỉ cần ghi lại đường dẫn. */
    fun pathOf(context: Context, cameraUri: Uri): String? {
        val name = cameraUri.lastPathSegment?.substringAfterLast('/') ?: return null
        val file = File(dir(context), name)
        return if (file.exists() && file.length() > 0) file.absolutePath else null
    }

    /**
     * Chép ảnh người dùng chọn từ thư viện vào thư mục của app.
     * Trả về đường dẫn file, hoặc null nếu không đọc được.
     */
    suspend fun importFrom(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
        val target = File(dir(context), "img_${UUID.randomUUID()}.jpg")
        runCatching {
            val input = context.contentResolver.openInputStream(source) ?: error("Không mở được ảnh")
            input.use { stream -> target.outputStream().use { output -> stream.copyTo(output) } }
            target.absolutePath
        }.getOrElse {
            target.delete()
            null
        }
    }

    /** Xóa file ảnh khi người dùng bỏ ảnh hoặc chọn ảnh khác, để không bỏ rác lại trong máy. */
    fun delete(path: String?) {
        val file = path?.let(::File) ?: return
        if (file.exists() && file.parentFile?.name == DIR_NAME) file.delete()
    }
}
