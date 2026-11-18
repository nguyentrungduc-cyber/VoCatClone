package com.vocatclone.app.ui.word

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.vocatclone.app.data.image.WordImageStore
import kotlinx.coroutines.launch

/** Hai cách lấy ảnh cho từ vựng. [onPicked] nhận đường dẫn file đã nằm trong bộ nhớ riêng của app. */
class WordImagePicker internal constructor(
    val takePhoto: () -> Unit,
    val pickFromGallery: () -> Unit
)

/**
 * Chuẩn bị hai bộ chọn ảnh dùng cho form từ vựng.
 *
 * - Thư viện: dùng Photo Picker của Android nên KHÔNG cần xin quyền đọc bộ nhớ.
 * - Máy ảnh: mở app camera có sẵn, ảnh ghi thẳng vào thư mục của app qua FileProvider.
 *   Không khai báo quyền CAMERA trong manifest nên cũng không phải xin quyền lúc chạy.
 *
 * Người dùng bấm hủy thì không gọi [onPicked].
 */
@Composable
fun rememberWordImagePicker(onPicked: (path: String) -> Unit): WordImagePicker {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Giữ URI của ảnh sắp chụp, vì contract chỉ báo thành công/thất bại chứ không trả URI về.
    val pendingCameraUri = remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        val uri = pendingCameraUri.value
        pendingCameraUri.value = null
        if (saved && uri != null) WordImageStore.pathOf(context, uri)?.let(onPicked)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch { WordImageStore.importFrom(context, uri)?.let(onPicked) }
        }
    }

    return remember(cameraLauncher, galleryLauncher) {
        WordImagePicker(
            takePhoto = {
                val uri = WordImageStore.newCameraTarget(context)
                pendingCameraUri.value = uri
                cameraLauncher.launch(uri)
            },
            pickFromGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }
}
