package com.vocatclone.app.ui.word

import java.util.Calendar
import java.util.TimeZone

/** Giá trị cho "chưa biết đang ở bộ từ vựng nào" (mọi bộ đều được liệt kê làm đích chuyển). */
const val NO_BOOKCASE = -1L

/**
 * Đổi một mốc thời gian (mili giây, theo giờ địa phương) thành giá trị để mở sẵn ô chọn ngày.
 *
 * Vì sao cần hàm này: ô chọn ngày của Material3 (DatePicker) làm việc với **nửa đêm UTC** của ngày được chọn,
 * không phải nửa đêm theo giờ địa phương. Nếu đưa thẳng mốc thời gian vào, ở múi giờ âm (ví dụ Mỹ) ô sẽ hiện
 * lùi một ngày. Hàm này lấy ngày-tháng-năm THEO GIỜ ĐỊA PHƯƠNG rồi dựng lại nửa đêm UTC của đúng ngày đó.
 */
fun localMillisToPickerUtcDate(localMillis: Long, zone: TimeZone = TimeZone.getDefault()): Long {
    val local = Calendar.getInstance(zone).apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/**
 * Ghép ngày người dùng chọn (giá trị ô chọn ngày trả về, là nửa đêm UTC) với giờ và phút người dùng chọn,
 * thành mốc thời gian theo GIỜ ĐỊA PHƯƠNG (đây là giá trị lưu vào cột createdAt).
 */
fun combineDateAndTime(
    pickerUtcDateMillis: Long,
    hour: Int,
    minute: Int,
    zone: TimeZone = TimeZone.getDefault()
): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = pickerUtcDateMillis }
    return Calendar.getInstance(zone).apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hour, minute, 0)
    }.timeInMillis
}
