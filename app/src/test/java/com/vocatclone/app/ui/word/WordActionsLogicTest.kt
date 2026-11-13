package com.vocatclone.app.ui.word

import java.util.Calendar
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

/** Kiểm thử hai hàm ngày giờ của menu thao tác trên từ. Chạy được trên máy tính, không cần emulator. */
class WordActionsLogicTest {

    private val zones = listOf(
        "Asia/Ho_Chi_Minh", "America/Los_Angeles", "Pacific/Kiritimati", "Pacific/Pago_Pago", "UTC", "Asia/Kolkata"
    )

    private fun localMillis(zone: TimeZone, y: Int, month: Int, d: Int, h: Int, min: Int): Long =
        Calendar.getInstance(zone).apply { clear(); set(y, month - 1, d, h, min, 0) }.timeInMillis

    @Test
    fun mo_o_chon_ngay_roi_chon_lai_dung_ngay_gio_cu_thi_ra_lai_mon_thoi_gian_cu() {
        for (id in zones) {
            val zone = TimeZone.getTimeZone(id)
            val original = localMillis(zone, 2026, 11, 13, 22, 45)
            val pickerDate = localMillisToPickerUtcDate(original, zone)
            assertEquals(id, original, combineDateAndTime(pickerDate, 22, 45, zone))
        }
    }

    @Test
    fun ngay_nguoi_dung_chon_luon_roi_dung_ngay_theo_gio_dia_phuong() {
        for (id in zones) {
            val zone = TimeZone.getTimeZone(id)
            // Ô chọn ngày trả về nửa đêm UTC của ngày 31/12/2026
            val picked = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { clear(); set(2026, 11, 31) }.timeInMillis
            val result = Calendar.getInstance(zone).apply { timeInMillis = combineDateAndTime(picked, 8, 5, zone) }
            assertEquals(id, 2026, result.get(Calendar.YEAR))
            assertEquals(id, 11, result.get(Calendar.MONTH))
            assertEquals(id, 31, result.get(Calendar.DAY_OF_MONTH))
            assertEquals(id, 8, result.get(Calendar.HOUR_OF_DAY))
            assertEquals(id, 5, result.get(Calendar.MINUTE))
            assertEquals(id, 0, result.get(Calendar.SECOND))
        }
    }
}
