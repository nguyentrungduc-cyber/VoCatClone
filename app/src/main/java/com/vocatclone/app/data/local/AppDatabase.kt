package com.vocatclone.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vocatclone.app.data.local.dao.BookcaseDao
import com.vocatclone.app.data.local.dao.WordDao
import com.vocatclone.app.data.local.entity.BookcaseEntity
import com.vocatclone.app.data.local.entity.WordEntity

@Database(
    entities = [BookcaseEntity::class, WordEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
