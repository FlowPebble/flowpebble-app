package com.flowpebble.microguides.data.repository

import com.flowpebble.microguides.data.local.AppDatabase
import com.flowpebble.microguides.data.local.MicroGuideDao
import com.flowpebble.microguides.data.local.MicroGuideEntity
import com.flowpebble.microguides.data.preferences.UserPreferencesRepository
import com.flowpebble.microguides.data.preferences.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class GuideRepository(
    private val dao: MicroGuideDao,
    private val prefsRepo: UserPreferencesRepository
) {
    val standardGuidesFlow: Flow<List<MicroGuideEntity>> = dao.observeStandardGuides()
    val bonusGuidesFlow: Flow<List<MicroGuideEntity>> = dao.observeBonusGuides()
    val savedGuidesFlow: Flow<List<MicroGuideEntity>> = dao.observeSavedGuides()
    val completedCountFlow: Flow<Int> = dao.observeCompletedCount()
    val userPrefsFlow: Flow<UserPrefs> = prefsRepo.userPrefsFlow

    fun observeGuideById(guideId: String): Flow<MicroGuideEntity?> = dao.observeGuideById(guideId)

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (dao.countAll() == 0) {
            dao.insertAll(AppDatabase.buildStarterGuides())
        }
    }

    suspend fun toggleBookmark(guide: MicroGuideEntity) = withContext(Dispatchers.IO) {
        dao.setSaved(guide.id, !guide.isSaved)
    }

    suspend fun setBookmarkExplicit(guideId: String, saved: Boolean) = withContext(Dispatchers.IO) {
        dao.setSaved(guideId, saved)
    }

    suspend fun toggleStepChecked(guide: MicroGuideEntity, stepIndex: Int) = withContext(Dispatchers.IO) {
        val updated = guide.withToggledStep(stepIndex)
        dao.updateGuide(updated)
    }

    suspend fun markGuideCompleted(guide: MicroGuideEntity) = withContext(Dispatchers.IO) {
        val stepCount = guide.getStepsList().size
        val fullMask = if (stepCount > 0) (1 shl stepCount) - 1 else 0
        dao.setCompleted(guide.id, true, fullMask)
    }

    suspend fun createCustomGuide(
        title: String,
        summary: String,
        category: String,
        emoji: String,
        steps: List<String>
    ) = withContext(Dispatchers.IO) {
        val entity = MicroGuideEntity(
            id = "custom-${UUID.randomUUID()}",
            title = title.trim(),
            category = category,
            emoji = emoji,
            summary = summary.trim().ifEmpty { "A practical custom micro-guide created in FlowPebble." },
            stepsJson = MicroGuideEntity.encodeSteps(steps),
            isSaved = true,
            isCustom = true,
            createdAt = System.currentTimeMillis()
        )
        dao.insertGuide(entity)
    }

    suspend fun setThemeMode(mode: Int) = withContext(Dispatchers.IO) {
        prefsRepo.setThemeMode(mode)
    }

    suspend fun setHapticsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        prefsRepo.setHapticsEnabled(enabled)
    }

    suspend fun unlockBonusVault() = withContext(Dispatchers.IO) {
        prefsRepo.unlockBonusVault()
    }

    suspend fun setOnboardingCompleted() = withContext(Dispatchers.IO) {
        prefsRepo.setOnboardingCompleted()
    }
}
