package com.flowpebble.microguides.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [MicroGuideEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun microGuideDao(): MicroGuideDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flowpebble_offline.db"
                )
                    .addCallback(SeedDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun buildStarterGuides(): List<MicroGuideEntity> {
            val baseTime = 1730000000000L
            return listOf(
                MicroGuideEntity(
                    id = "phone-space",
                    title = "Review your phone storage safely",
                    category = "Phone & tech",
                    emoji = "📱",
                    summary = "Find large files before deleting anything important.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "Open your phone Settings and navigate to Storage.",
                            "Review large videos, downloads, and files you clearly recognize.",
                            "Back up anything important before deleting it from your device.",
                            "Empty the trash or recently deleted folder only after double-checking."
                        )
                    ),
                    timerSeconds = 0,
                    isSaved = true,
                    createdAt = baseTime + 1L
                ),
                MicroGuideEntity(
                    id = "focus-15",
                    title = "Set up a 15-minute focus session",
                    category = "Study & work",
                    emoji = "⏱️",
                    summary = "Turn one vague task into a short, clear 15-minute sprint.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "Write down one concrete outcome you can finish in 15 minutes.",
                            "Close unrelated apps and silence non-essential notifications.",
                            "Start the built-in 15-minute timer below and work only on that outcome.",
                            "Write the single next physical action before ending your session."
                        )
                    ),
                    timerSeconds = 15 * 60,
                    isSaved = true,
                    createdAt = baseTime + 2L
                ),
                MicroGuideEntity(
                    id = "packing-list",
                    title = "Build a reusable packing list",
                    category = "Everyday",
                    emoji = "🧳",
                    summary = "Create a calm, repeatable checklist you can reuse for future trips.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "Check your destination, trip length, and expected weather forecast.",
                            "List essential IDs, chargers, medications, and daily clothing.",
                            "Add two or three specialty items specific to this trip.",
                            "Check off each item as it enters your bag and bookmark the list."
                        )
                    ),
                    timerSeconds = 0,
                    createdAt = baseTime + 3L
                ),
                MicroGuideEntity(
                    id = "clean-downloads",
                    title = "Organize a crowded Downloads folder",
                    category = "Phone & tech",
                    emoji = "🗂️",
                    summary = "Sort first and delete only files you can positively identify.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "Open your Files app and navigate to the Downloads directory.",
                            "Sort files by largest size or oldest date first.",
                            "Move receipts, tickets, or PDFs you still need into named folders.",
                            "Remove duplicate installers and obsolete temporary files."
                        )
                    ),
                    timerSeconds = 0,
                    createdAt = baseTime + 4L
                ),
                MicroGuideEntity(
                    id = "three-step-plan",
                    title = "Break a big task into three actions",
                    category = "Study & work",
                    emoji = "🧩",
                    summary = "Make the next move obvious instead of planning forever.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "Describe what 'done' looks like in one clear sentence.",
                            "Pick a first step small enough to finish in under 10 minutes.",
                            "Write the next two follow-up actions in plain action verbs.",
                            "Begin step one immediately and adjust as you learn."
                        )
                    ),
                    timerSeconds = 0,
                    createdAt = baseTime + 5L
                ),
                MicroGuideEntity(
                    id = "desk-reset",
                    title = "Reset a cluttered desk in five minutes",
                    category = "Everyday",
                    emoji = "🧼",
                    summary = "A rapid 5-minute physical reset for a calmer workspace.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "Clear away mugs, dishes, wrappers, and items belonging in another room.",
                            "Stack loose papers into a single 'review today' pile.",
                            "Place your keyboard, notebook, and pen in their primary spots.",
                            "Wipe the main surface so you have one clean zone to start working."
                        )
                    ),
                    timerSeconds = 5 * 60,
                    createdAt = baseTime + 6L
                ),
                // Pro / Bonus Vault Guides (Unlocked via voluntary Rewarded Ad)
                MicroGuideEntity(
                    id = "bonus-reuse",
                    title = "Turn a useful tip into a shareable guide",
                    category = "Create & share",
                    emoji = "✨",
                    summary = "Capture practical knowledge in a format a friend can immediately reuse.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "State the exact problem the guide helps someone solve in one line.",
                            "Break the solution into 3 to 5 sequential actions in chronological order.",
                            "Start every step with a strong verb and trim filler words.",
                            "Test the steps yourself, then share the clean checklist with a friend."
                        )
                    ),
                    timerSeconds = 0,
                    isBonusVault = true,
                    createdAt = baseTime + 7L
                ),
                MicroGuideEntity(
                    id = "bonus-detox",
                    title = "Deep Work Digital Detox Protocol",
                    category = "Study & work",
                    emoji = "🛡️",
                    summary = "Eliminate cognitive context-switching before high-leverage engineering or writing.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "Enable Do Not Disturb and place your phone out of arm's reach.",
                            "Close all messaging tabs, email clients, and social feeds.",
                            "Open only the single document, IDE, or notebook required for your task.",
                            "Commit to a 25-minute uninterrupted block before checking any inbox."
                        )
                    ),
                    timerSeconds = 25 * 60,
                    isBonusVault = true,
                    createdAt = baseTime + 8L
                ),
                MicroGuideEntity(
                    id = "bonus-morning",
                    title = "7-Minute Morning Clarity Routine",
                    category = "Wellness & focus",
                    emoji = "🌿",
                    summary = "Ground your attention before opening notifications or news feeds.",
                    stepsJson = MicroGuideEntity.encodeSteps(
                        listOf(
                            "Drink a full glass of water and take five slow, deep breaths.",
                            "Stretch your neck, shoulders, and spine for two minutes.",
                            "Write down the single most meaningful small win for today.",
                            "Begin your first priority for 5 minutes before checking messages."
                        )
                    ),
                    timerSeconds = 7 * 60,
                    isBonusVault = true,
                    createdAt = baseTime + 9L
                )
            )
        }
    }

    private class SeedDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.microGuideDao()
                    if (dao.countAll() == 0) {
                        dao.insertAll(buildStarterGuides())
                    }
                }
            }
        }
    }
}
