package com.vocatclone.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vocatclone.app.data.local.dao.BookcaseDao
import com.vocatclone.app.data.local.dao.BookshelfDao
import com.vocatclone.app.data.local.dao.WordDao
import com.vocatclone.app.data.local.entity.BookcaseEntity
import com.vocatclone.app.data.local.entity.BookshelfEntity
import com.vocatclone.app.data.local.entity.WordEntity
import com.vocatclone.app.data.local.entity.WordMeaningEntity

/**
 * Database cục bộ (local-first).
 * Xác thực người dùng do Firebase Auth đảm nhận — không có bảng Users ở đây.
 * Lọc dữ liệu theo người dùng qua cột firebaseUid trong bảng bookshelves.
 *
 * Quan hệ (toàn bộ 1-n):
 * bookshelves 1-n bookcases 1-n words 1-n word_meanings
 */
@Database(
    entities = [
        BookshelfEntity::class,
        BookcaseEntity::class,
        WordEntity::class,
        WordMeaningEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookshelfDao(): BookshelfDao
    abstract fun bookcaseDao(): BookcaseDao
    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vocatclone.db"
                )
                    // Schema thay đổi nhiều (bỏ Users, thêm Bookshelves, WordMeanings).
                    // App chưa phát hành → xóa DB cũ và tạo lại.
                    // Trước khi release: bỏ dòng này, viết Migration thật.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
