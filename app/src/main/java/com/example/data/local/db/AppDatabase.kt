package com.example.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.promptpro.data.local.PromptTemplateDao
import com.example.promptpro.data.local.PromptTemplateEntity

@Database(
    entities = [PromptEntity::class, PromptTemplateEntity::class],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun promptDao(): PromptDao
    abstract fun promptTemplateDao(): PromptTemplateDao

    companion object {

        /**
         * v1 -> v2: adds the prompt_templates table (new feature, no data migration needed).
         * Column types must match what Room expects from PromptTemplateEntity:
         *  - id TEXT PRIMARY KEY NOT NULL
         *  - name TEXT NOT NULL
         *  - description TEXT (nullable)
         *  - slotsJson / defaultValuesJson / examplesJson TEXT NOT NULL
         *  - version TEXT NOT NULL
         *  - createdAt INTEGER NOT NULL
         */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `prompt_templates` (" +
                        "`id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`description` TEXT, " +
                        "`slotsJson` TEXT NOT NULL, " +
                        "`defaultValuesJson` TEXT NOT NULL, " +
                        "`examplesJson` TEXT NOT NULL, " +
                        "`version` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prompt_generator_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}