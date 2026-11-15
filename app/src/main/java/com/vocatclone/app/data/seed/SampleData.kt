package com.vocatclone.app.data.seed

import androidx.room.withTransaction
import com.vocatclone.app.data.local.AppDatabase
import com.vocatclone.app.data.local.entity.BookcaseEntity
import com.vocatclone.app.data.local.entity.BookshelfEntity
import com.vocatclone.app.data.local.entity.WordEntity

/**
 * Dữ liệu mẫu để cả nhóm có cái mà làm/test Home, Flashcard, Quiz... khi chưa có form nhập thật.
 * Chỉ dùng khi phát triển; app gọi nó qua nút "seed" chỉ hiện ở bản debug (xem HomeRoute).
 */
object SampleData {

    private class W(
        val term: String,
        val pronunciation: String,
        val partOfSpeech: String,
        val meanings: List<String>,
        val mastered: Boolean = false
    )

    private class WordSet(val name: String, val words: List<W>)
    private class Shelf(val name: String, val sets: List<WordSet>)

    private val shelves = listOf(
        Shelf(
            "English vlog-home",
            listOf(
                WordSet(
                    "Household items",
                    listOf(
                        W("toothpaste", "/ˈtuːθ.peɪst/", "noun", listOf("kem đánh răng"), mastered = true),
                        W("pillow", "/ˈpɪl.oʊ/", "noun", listOf("cái gối")),
                        W("curtain", "/ˈkɝː.tən/", "noun", listOf("rèm cửa", "màn"), mastered = true),
                        W("vacuum cleaner", "/ˈvæk.juːm ˌkliː.nɚ/", "noun", listOf("máy hút bụi"))
                    )
                ),
                WordSet(
                    "Kitchen",
                    listOf(
                        W("spoon", "/spuːn/", "noun", listOf("cái thìa", "cái muỗng")),
                        W("frying pan", "/ˈfraɪ.ɪŋ ˌpæn/", "noun", listOf("chảo rán")),
                        W("kettle", "/ˈket̬.əl/", "noun", listOf("ấm đun nước"))
                    )
                )
            )
        ),
        Shelf(
            "FULL ENGLISH",
            listOf(
                WordSet(
                    "Travel",
                    listOf(
                        W("luggage", "/ˈlʌɡ.ɪdʒ/", "noun", listOf("hành lý"), mastered = true),
                        W("boarding pass", "/ˈbɔːr.dɪŋ ˌpæs/", "noun", listOf("thẻ lên máy bay")),
                        W("itinerary", "/aɪˈtɪn.ə.rer.i/", "noun", listOf("lịch trình"))
                    )
                ),
                WordSet(
                    "Hotel",
                    listOf(
                        W("reception", "/rɪˈsep.ʃən/", "noun", listOf("quầy lễ tân")),
                        W("check in", "/ˈtʃek ɪn/", "verb", listOf("nhận phòng", "làm thủ tục"))
                    )
                )
            )
        )
    )

    /**
     * Thêm dữ liệu mẫu cho [uid]. Chỉ làm khi tài khoản này chưa có tủ sách nào
     * (để bấm nhiều lần không bị nhân đôi). Trả về true nếu đã thêm.
     */
    suspend fun insertFor(db: AppDatabase, uid: String): Boolean {
        if (db.bookshelfDao().getFirstByUser(uid) != null) return false
        val now = System.currentTimeMillis()
        var tick = 0L
        db.withTransaction {
            for (shelf in shelves) {
                val shelfId = db.bookshelfDao().insert(BookshelfEntity(firebaseUid = uid, name = shelf.name, createdAt = now - (tick++) * 1_000))
                for (set in shelf.sets) {
                    val bookcaseId = db.bookcaseDao().insert(
                        BookcaseEntity(
                            bookshelfId = shelfId,
                            name = set.name,
                            sourceLang = "English (US)",
                            targetLang = "Vietnamese",
                            createdAt = now - (tick++) * 1_000
                        )
                    )
                    for (w in set.words) {
                        db.wordDao().insertWithMeanings(
                            WordEntity(
                                bookcaseId = bookcaseId,
                                term = w.term,
                                pronunciation = w.pronunciation,
                                partOfSpeech = w.partOfSpeech,
                                isMastered = w.mastered,
                                createdAt = now - (tick++) * 60_000
                            ),
                            w.meanings
                        )
                    }
                }
            }
        }
        return true
    }
}
